package ozmeyham.imsbridge.commands;

import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static ozmeyham.imsbridge.IMSBridge.combinedBridgeChatEnabled;
import static ozmeyham.imsbridge.IMSBridge.combinedBridgeEnabled;
import static ozmeyham.imsbridge.ImsWebSocketClient.wsClient;
import static ozmeyham.imsbridge.utils.ConfigUtils.saveConfigValue;
import static ozmeyham.imsbridge.utils.TextUtils.printToChat;

public class CombinedBridgeCommands {
    public static void combinedBridgeHelpCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("cbridge")
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("help")
                        .executes(ctx -> {
                            printToChat("""
                                    §b§l- Combined Bridge Help -
                                    §b/cbridge toggle: §7Enables/disables cbridge message rendering.
                                    §b/cbridge colour <colour1> <colour2> <colour3>: §7Sets the colour formatting of rendered cbridge messages.
                                    §b/cbridge colour: §7Sets the colour formatting back to default.
                                    §b/cbridge chat: §6(alias /cbc or /bc) §7Enter/exit cbridge chat (like how \"/chat guild\" works). 
                                    §b/cbridge party [playerCap] [reason]: §7Sends a message in cbridge that you have an open party, and lets people join with !join ign. 
                                    §b/cbc <msg>: §6(or /bc <msg>) §7Sends msg to cbridge.
                                    """
                            );
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }

    public static void combinedBridgeToggleCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("cbridge")
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("toggle")
                        .executes(ctx -> {
                            if (combinedBridgeEnabled == true) {
                                combinedBridgeEnabled = false;
                                saveConfigValue("combinedBridgeEnabled", "false");
                                printToChat("§cDisabled combined bridge messages!");
                            } else {
                                combinedBridgeEnabled = true;
                                saveConfigValue("combinedBridgeEnabled", "true");
                                printToChat("§aEnabled combined bridge messages!");
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                ));
    }

    public static void combinedBridgeMsgCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("cbc")
                .then(argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String message = StringArgumentType.getString(ctx, "message");
                            if (combinedBridgeEnabled && wsClient.isOpen() && wsClient != null) {
                                JsonObject payload = new JsonObject();
                                payload.addProperty("from","mc");
                                payload.addProperty("msg",message);
                                payload.addProperty("combinedbridge","true");
                                wsClient.send(payload.toString());
                            }
                            else if (wsClient == null || wsClient.isClosed()){
                                printToChat("§cYou are not connected to the bridge websocket server!");
                            } else {
                                printToChat("§cYou need to enable combined bridge messages to use this command! §6§o/cbridge toggle");
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                ));
    }

    public static void combinedBridgeChatCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("cbridge")
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("chat")
                        .executes(ctx -> {
                            if (combinedBridgeChatEnabled == false) {
                                combinedBridgeChatEnabled = true;
                                saveConfigValue("combinedBridgeChatEnabled", "true");
                                printToChat("§aEntered combined bridge chat!");
                            } else {
                                combinedBridgeChatEnabled = false;
                                saveConfigValue("combinedBridgeChatEnabled", "false");
                                printToChat("§cExited combined bridge chat!");
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                ));
    }
    public static void combinedBridgeChatCommandShort(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("cbc")
                .executes(ctx -> {
                    if (combinedBridgeChatEnabled == false) {
                        combinedBridgeChatEnabled = true;
                        saveConfigValue("combinedBridgeChatEnabled", "true");
                        printToChat("§aEntered cbridge chat!");
                    } else {
                        combinedBridgeChatEnabled = false;
                        saveConfigValue("combinedBridgeChatEnabled", "false");
                        printToChat("§cExited cbridge chat!");
                    }
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    public static void combinedBridgeMsgCommand2(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("bc")
                .then(argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String message = StringArgumentType.getString(ctx, "message");
                            if (combinedBridgeEnabled && wsClient.isOpen() && wsClient != null) {
                                JsonObject payload = new JsonObject();
                                payload.addProperty("from","mc");
                                payload.addProperty("msg",message);
                                payload.addProperty("combinedbridge","true");
                                wsClient.send(payload.toString());
                            }
                            else if (wsClient == null || wsClient.isClosed()){
                                printToChat("§cYou are not connected to the bridge websocket server!");
                            } else {
                                printToChat("§cYou need to enable combined bridge messages to use this command! §6§o/cbridge toggle");
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                ));
    }

    public static void combinedBridgeChatCommandShort2(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("bc")
                .executes(ctx -> {
                    if (combinedBridgeChatEnabled == false) {
                        combinedBridgeChatEnabled = true;
                        saveConfigValue("combinedBridgeChatEnabled", "true");
                        printToChat("§aEntered cbridge chat!");
                    } else {
                        combinedBridgeChatEnabled = false;
                        saveConfigValue("combinedBridgeChatEnabled", "false");
                        printToChat("§cExited cbridge chat!");
                    }
                    return Command.SINGLE_SUCCESS;
                })
        );
    }
}
