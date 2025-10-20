package ozmeyham.imsbridge;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

import static ozmeyham.imsbridge.IMSBridge.*;
import static ozmeyham.imsbridge.ImsWebSocketClient.*;
import static ozmeyham.imsbridge.commands.CommandHandler.registerCommands;
import static ozmeyham.imsbridge.utils.BridgeKeyUtils.*;
import static ozmeyham.imsbridge.utils.ConfigUtils.saveConfigValue;
import static ozmeyham.imsbridge.utils.TextUtils.*;
import static ozmeyham.imsbridge.utils.TextUtils.isSkyblockChannelChange;
import static ozmeyham.imsbridge.utils.UpdateChecker.checkForUpdates;


public class EventHandler {
    public static void eventListener() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerCommands(dispatcher));
        ClientSendMessageEvents.ALLOW_CHAT.register(EventHandler::allowCBridgeMsg);
        ClientReceiveMessageEvents.GAME.register(EventHandler::handleClientMessages);
        ClientPlayConnectionEvents.JOIN.register(EventHandler::onWorldJoin);
        ClientPlayConnectionEvents.DISCONNECT.register(EventHandler::onWorldLeave); //wip for disconnecting players when they leave a server/world
    }

    // Checks stuff when you join a world
    private static void onWorldJoin(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        // ClientConnection connection = handler.getConnection();
        // String address = connection.getAddress().toString().toLowerCase();
        // onHypixel = address.contains("hypixel.net");
        clientOnline = true;
        if (isValidBridgeKey()) {
            connectWebSocket();
        }
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                if (firstLogin) {
                    firstLogin = false;
                    saveConfigValue("firstLogin", "false");
                    printToChat("§bThanks for installing IMS Bridge Mod.\n§7Use §6/bridge help §7and §6/cbridge help §7to get a list of all commands.");
                }
                Thread.sleep(2000);
                if (!checkedForUpdate) {
                    checkForUpdates();
                    checkedForUpdate =true;}
                if (!isValidBridgeKey()) {
                    printToChat("§cBridge key not set. §7Use §6/key §7on discord to obtain a key, then run §6/bridge key §7in-game and paste your key.");
                }

            } catch (InterruptedException e) {
                printToChat("§cAn error occured: "+e);
                throw new RuntimeException(e);
            }
        }).start();
    }

    private static void onWorldLeave(ClientPlayNetworkHandler handler, MinecraftClient client) {
        clientOnline = false;
        disconnectWebSocket();
    }

    // Redirects chat messages to cbridge when required
    private static boolean allowCBridgeMsg(String message) {
        if (combinedBridgeEnabled && combinedBridgeChatEnabled && wsClient != null && wsClient.isOpen() && bridgeKey != null) {
            if (!message.startsWith("/")) {
                wsClient.send("{\"from\":\"mc\",\"msg\":\"" + sanitizeMessage(message) + "\",\"combinedbridge\":true}");
                return false;
            }
        }
        else if (combinedBridgeChatEnabled && !combinedBridgeEnabled) {
            printToChat("§cYou need to enable cbridge messages before using cbridge! §6§oDo /cbridge toggle");
            return false;
        }
        return true;
    }

    // Listen for outgoing guild messages and channel changes
    private static void handleClientMessages(net.minecraft.text.Text message, boolean overlay) {
        String content = message.getString();
        if (content.contains("§2Guild >")) {
            // Send to websocket
            if (wsClient != null && wsClient.isOpen() && bridgeKey != null) {
                wsClient.send("{\"from\":\"mc\",\"msg\":" + quote(sanitizeMessage(content)) + "}");
            }
        } else if (isSkyblockChannelChange(content) && combinedBridgeChatEnabled) {
            combinedBridgeChatEnabled = false;
            saveConfigValue("combinedBridgeChatEnabled", "false");
            printToChat("§cExited cbridge chat!");
        }
    }
}
