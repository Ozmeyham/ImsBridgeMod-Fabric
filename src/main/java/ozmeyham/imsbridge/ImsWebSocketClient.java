package ozmeyham.imsbridge;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;
import com.google.gson.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ozmeyham.imsbridge.commands.CombinedBridgePartyCommand;
import ozmeyham.imsbridge.utils.TextUtils;

import static com.mojang.text2speech.Narrator.LOGGER;
import static ozmeyham.imsbridge.IMSBridge.*;
import static ozmeyham.imsbridge.commands.BridgeColourCommand.*;
import static ozmeyham.imsbridge.commands.CombinedBridgeColourCommand.*;
import static ozmeyham.imsbridge.utils.BridgeKeyUtils.bridgeKey;
import static ozmeyham.imsbridge.utils.BridgeKeyUtils.isValidBridgeKey;
import static ozmeyham.imsbridge.utils.TextUtils.printToChat;
import static ozmeyham.imsbridge.utils.TextUtils.quote;

public class ImsWebSocketClient extends WebSocketClient {

    public static ImsWebSocketClient wsClient;
    public static Boolean clientOnline = false; //boolean to track whether player is actually in a world/server

    public ImsWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    public static void connectWebSocket() {
        if (wsClient == null || !wsClient.isOpen()) {
            // printToChat("§cConnecting to websocket...");
            try {
                wsClient = new ImsWebSocketClient(new URI("wss://ims-bridge.com"));
                wsClient.connect();
            } catch (URISyntaxException e) {
                LOGGER.error("Invalid WebSocket URI", e);
            }
        }
    }

    public static void disconnectWebSocket() {
        if (wsClient != null && wsClient.isOpen()) {
            try {
                wsClient.close();
            } catch (Exception e) {
                LOGGER.error("Failed to close WebSocket", e);
            }
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        LOGGER.info("WebSocket Connected");
        // printToChat("§2Successfully connected to websocket.");
        // Send bridgeKey immediately after connecting
        if (bridgeKey != null) {
            wsClient.send("{\"from\":\"mc\",\"key\":" + quote(bridgeKey) + "}");
        }
    }

    @Override
    public void onMessage(String message) {
        //printToChat(message);
        if (getJsonValue(message, "response") != null) {
            handleResponse(message);
        } else if (getJsonValue(message, "from") != null) {
            String msg = getJsonValue(message, "msg");
            if (msg == null) return;

            String[] split = msg.split(": ", 2);
            String username = split.length > 0 ? split[0] : "";
            String chatMsg = split.length > 1 ? split[1] : "";
            String guild = GUILD_MAP.get(getJsonValue(message, "guild"));
            String guildColour = GUILD_COLOUR_MAP.get(getJsonValue(message, "guild"));

            if (message.contains("\"combinedbridge\":true")){
                if (message.contains("\"from\":\"discord\"")) {
                    guild = "DISC";
                    guildColour = "§9";
                }
                cbridgeMessage(chatMsg, username, guild, guildColour);
            } else if (message.contains("\"from\":\"discord\"")){
                bridgeMessage(chatMsg, username, guild);
            }
        }

    }

    private void handleResponse(String message) {
        if (Objects.equals(getJsonValue(message, "request"), "getOnlinePlayers")) {
            JsonObject root = JsonParser.parseString(message).getAsJsonObject();
            JsonObject response = root.getAsJsonObject("response");
            int totalPlayers = 0;
            for (String guild : response.keySet()) {
                totalPlayers += response.getAsJsonArray(guild).size();
            }
            StringBuilder messageBuilder = new StringBuilder("§aOnline Players: §e" + totalPlayers + "\n");
            for (String guild : response.keySet()) {
                JsonArray players = response.getAsJsonArray(guild);
                int count = players.size();

                messageBuilder.append(GUILD_COLOUR_MAP.get(guild)).append(guild).append("§7: §e").append(count).append("\n");

                if (players.isEmpty()) {
                    messageBuilder.append("§7None\n");
                } else {
                    for (int i = 0; i < count; i++) {
                        messageBuilder.append("§f").append(players.get(i).getAsString());
                        if (i < count - 1) messageBuilder.append(", ");
                    }
                    messageBuilder.append("\n");
                }
            }
            printToChat(messageBuilder.toString());
        }
    }

    private void bridgeMessage(String chatMsg, String username, String guild) {
        String formattedMsg = bridgeC1 + "Guild > " + bridgeC2 + username + " §9[DISC]§f: " + bridgeC3 + chatMsg;
        // Send formatted message in client chat
        if (bridgeEnabled) {
            TextUtils.printToChat(formattedMsg, false);
        }
    }

    private void cbridgeMessage(String chatMsg, String username, String guild, String guildColour) {
        if (!combinedBridgeEnabled) return;
        String formattedMsg = cbridgeC1 + "CB > " + cbridgeC2 + username + guildColour + " [" + guild + "]§f: " + cbridgeC3 + chatMsg;
        // Send formatted message in client chat
        TextUtils.printToChat(formattedMsg, false);
        if (CombinedBridgePartyCommand.partySpotsLeft <= 0 || System.currentTimeMillis() > CombinedBridgePartyCommand.lastParty + 300000) return;
        String joinCommand = "!join " + MinecraftClient.getInstance().player.getName().getString();
        if (chatMsg.equalsIgnoreCase(joinCommand)) {
            CombinedBridgePartyCommand.partySpotsLeft -= 1;
            MinecraftClient.getInstance().getNetworkHandler().sendChatCommand("/p " + username);
        }
    }

    public static String getJsonValue(String jsonString, String key) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(jsonString);
            JsonNode valueNode = node.get(key);
            return valueNode != null ? valueNode.asText() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static final Map<String, String> GUILD_MAP = Map.ofEntries(
            Map.entry("Ironman Sweats", "IMS"),
            Map.entry("Ironman Casuals", "IMC"),
            Map.entry("Ironman Academy", "IMA")
    );

    public static final Map<String, String> GUILD_COLOUR_MAP = Map.ofEntries(
            Map.entry("Ironman Sweats", "§a"),
            Map.entry("Ironman Casuals", "§3"),
            Map.entry("Ironman Academy", "§2")
    );

    @Override
    public void onClose(int code, String reason, boolean remote) {
        LOGGER.info("WebSocket Closed: {}", reason);

        if ("Invalid bridge key".equals(reason)) {
            printToChat("§4Disconnected from websocket: §cfailed to authenticate bridge key. §7Use §6/bridge key <key>§7 to try again.");
            LOGGER.warn("Not reconnecting due to invalid key.");
            return; // Don't attempt to reconnect
        }
        if (isValidBridgeKey() && clientOnline) {
            tryReconnecting();
        }
    }

    @Override
    public void onError(Exception ex) {
        LOGGER.error("WebSocket Error", ex);
    }

    private void tryReconnecting() {
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                LOGGER.info("Attempting to reconnect...");
                // printToChat("§4Disconnected from websocket. §6Attempting to reconnect...");
                this.reconnect();
            } catch (InterruptedException e) {
                LOGGER.error("Reconnect interrupted", e);
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}