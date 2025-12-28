package ozmeyham.imsbridge;

import com.google.gson.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.*;
import ozmeyham.imsbridge.commands.CombinedBridgeColourCommand;

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
        ClientReceiveMessageEvents.MODIFY_GAME.register(EventHandler::clickableJoinCommand);
        ClientPlayConnectionEvents.JOIN.register(EventHandler::onWorldJoin);
        ClientPlayConnectionEvents.DISCONNECT.register(EventHandler::onWorldLeave); //wip for disconnecting players when they leave a server/world
    }

    // Checks stuff when you join a world
    private static void onWorldJoin(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        // ClientConnection connection = handler.getConnection();
        // String address = connection.getAddress().toString().toLowerCase();
        // onHypixel = address.contains("hypixel.net");
        clientOnline = true;
        if (isValidBridgeKey() && (wsClient == null || !wsClient.isOpen())) {
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
                JsonObject payload = new JsonObject();
                payload.addProperty("from", "mc");
                payload.addProperty("msg", message);
                payload.addProperty("combinedbridge","true");
                ImsWebSocketClient.wsClient.send(payload.toString());
                return false;
            }
        }
        else if (combinedBridgeChatEnabled && !combinedBridgeEnabled) {
            printToChat("§cYou need to enable cbridge messages before using cbridge! §6§oDo /cbridge toggle");
            return false;
        } else if (combinedBridgeChatEnabled && combinedBridgeEnabled && (wsClient == null || !wsClient.isOpen())) {
            printToChat("§cYou are not connected to the bridge server!");
            return false;
        }
        return true;
    }

    // Listen for outgoing guild messages and channel changes
    private static void handleClientMessages(Text message, boolean overlay) {
        String content = message.getString();
        if (content.contains("§2Guild >")) {
            // Send to websocket
            if (wsClient != null && wsClient.isOpen() && bridgeKey != null) {
                JsonObject payload = new JsonObject();
                payload.addProperty("from","mc");
                payload.addProperty("msg",content);
                wsClient.send(payload.toString());
            }
        } else if (content.endsWith(" joined the guild!") || content.endsWith(" left the guild!")) {
            if (wsClient != null && wsClient.isOpen() && bridgeKey != null) {
                JsonObject payload = new JsonObject();
                payload.addProperty("from","mc");
                payload.addProperty("msg", content);
                payload.addProperty("guildMemberChange", "true");
                wsClient.send(payload.toString());
            }
        } else if (isSkyblockChannelChange(content) && combinedBridgeChatEnabled) {
            combinedBridgeChatEnabled = false;
            saveConfigValue("combinedBridgeChatEnabled", "false");
            printToChat("§cExited cbridge chat!");
        } else if (content.startsWith("Disabled guild chat!")) {
            if (combinedBridgeEnabled) {
                combinedBridgeEnabled = false;
                saveConfigValue("combinedBridgeEnabled", "false");
                printToChat("§cDisabled combined bridge messages!");
            }
            if (bridgeEnabled) {
                bridgeEnabled = false;
                saveConfigValue("bridgeEnabled", "false");
                printToChat("§cDisabled bridge messages!");
            }
        } else if (content.startsWith("Enabled guild chat!")) {
            if (!combinedBridgeEnabled) {
                combinedBridgeEnabled = true;
                saveConfigValue("combinedBridgeEnabled", "true");
                printToChat("§aEnabled combined bridge messages!");
            }
            if (!bridgeEnabled) {
                bridgeEnabled = true;
                saveConfigValue("bridgeEnabled", "true");
                printToChat("§aEnabled bridge messages!");
            }
        }
    }

    private static Text clickableJoinCommand(Text message, boolean overlay) {
        if (overlay) return message;
        String string = message.getString();
        if (!string.startsWith(CombinedBridgeColourCommand.cbridgeC1 + "CB > ")) return message;
        if (!string.contains("Do !join ") || !string.contains(" to join!")) return message;
        MutableText newMessage = (MutableText) message;
        String ignToJoin;
        try {
            ignToJoin = string.split("!join ")[1].split(" ")[0];
        } catch (Exception e) {
            return message;
        }
        newMessage = newMessage.setStyle(newMessage.getStyle().withClickEvent(new ClickEvent.RunCommand("/cbc !join " + ignToJoin)));
        newMessage = newMessage.setStyle(newMessage.getStyle().withHoverEvent(new HoverEvent.ShowText(Text.of("§eRuns /cbc !join " + ignToJoin))));
        newMessage = newMessage.append(" §eClick here to join the party");
        return newMessage;
    }
}
