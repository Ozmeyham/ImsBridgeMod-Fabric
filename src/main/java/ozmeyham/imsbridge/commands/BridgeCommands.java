package ozmeyham.imsbridge.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static ozmeyham.imsbridge.IMSBridge.*;
import static ozmeyham.imsbridge.ImsWebSocketClient.connectWebSocket;
import static ozmeyham.imsbridge.ImsWebSocketClient.wsClient;
import static ozmeyham.imsbridge.utils.BridgeKeyUtils.bridgeKey;
import static ozmeyham.imsbridge.utils.BridgeKeyUtils.isValidBridgeKey;
import static ozmeyham.imsbridge.utils.ConfigUtils.loadConfig;
import static ozmeyham.imsbridge.utils.ConfigUtils.saveConfigValue;
import static ozmeyham.imsbridge.utils.TextUtils.printToChat;

public final class BridgeCommands {
    public static void bridgeHelpCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("bridge")
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("help")
                        .executes(ctx -> {
                            printToChat("""
                                   §9§l- Bridge Help -
                                   §9/bridge key <key>: §7Sets your bridge key; obtain key via discord bot.
                                   §9/bridge toggle: §7Enables/disables bridge message rendering.
                                   §9/bridge colour <colour1> <colour2> <colour3>: §7Sets the colour formatting of rendered bridge messages.
                                   §9/bridge colour: §7Sets the colour formatting back to default.
                                   §9/bridge online: §6(alias /bl) §7Shows a list of online guildmates using this mod.
                                   """
                            );
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }

    public static void bridgeKeyCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("bridge")
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("key")
                        .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("key", StringArgumentType.word())
                                .executes(ctx -> {
                                    String key = StringArgumentType.getString(ctx, "key");
                                    bridgeKey = key;
                                    if (isValidBridgeKey()) {
                                        saveConfigValue("bridgeKey", bridgeKey);
                                        loadConfig();
                                        LOGGER.info("Bridge key set to " + key);
                                        printToChat("§cBridge key saved as: §f" + bridgeKey);
                                        connectWebSocket();
                                    } else {
                                        printToChat("§cInvalid bridge key format! Check you pasted correctly.");
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
        );
    }

    public static void bridgeToggleCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("bridge")
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("toggle")
                        .executes(ctx -> {
                            if (bridgeEnabled == true) {
                                bridgeEnabled = false;
                                saveConfigValue("bridgeEnabled", "false");
                                printToChat("§cDisabled guild bridge messages!");
                            } else {
                                bridgeEnabled = true;
                                saveConfigValue("bridgeEnabled", "true");
                                printToChat("§aEnabled guild bridge messages!");
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                ));
    }

    public static void bridgeOnlineCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("bridge")
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("online")
                        .executes(ctx -> {
                            if (wsClient != null && wsClient.isOpen()) {
                                wsClient.send("{\"from\":\"mc\",\"request\":\"getOnlinePlayers\"}");
                            } else {
                                printToChat("§cYou are not connected to the bridge server!");
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                ));
    }

    public static void bridgeOnlineCommandShort(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("bl")
                .executes(ctx -> {
                    if (wsClient != null && wsClient.isOpen()) {
                        wsClient.send("{\"from\":\"mc\",\"request\":\"getOnlinePlayers\"}");
                    } else {
                        printToChat("§cYou are not connected to the bridge server!");
                    }
                    return Command.SINGLE_SUCCESS;
                })
        );
    }


}