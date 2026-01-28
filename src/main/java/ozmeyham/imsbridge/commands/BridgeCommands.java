package ozmeyham.imsbridge.commands;

import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import ozmeyham.imsbridge.ImsWebSocketClient;
import ozmeyham.imsbridge.utils.TextUtils;

import static ozmeyham.imsbridge.IMSBridge.*;
import static ozmeyham.imsbridge.ImsWebSocketClient.*;
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
                                        disconnectWebSocket();
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

    public static void bridgeShowCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("bridge")
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("show")
                        .executes(ctx -> {
                            try {
                                var stack = MinecraftClient.getInstance().player.getMainHandStack();
                                if (stack == null || stack.isEmpty()) {
                                    TextUtils.printToChat("You must be holding an item");
                                    return Command.SINGLE_SUCCESS;
                                }
                                var world = MinecraftClient.getInstance().world;
                                if (world == null) {
                                    TextUtils.printToChat("world null");
                                    return Command.SINGLE_SUCCESS;
                                }
                                var ops = world.getRegistryManager().getOps(JsonOps.INSTANCE);
                                var jsonStack = ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();
                                var amountStr = "";
                                if (stack.getCount() > 1) amountStr = " x" + stack.getCount();
                                var message = "is holding [" + stack.getName().getString() + amountStr + "]";

                                if (wsClient != null && wsClient.isOpen()) {
                                    JsonObject payload = new JsonObject();
                                    payload.addProperty("from", "show");
                                    payload.addProperty("msg", message);
                                    payload.add("jsonStack", jsonStack);
                                    payload.addProperty("show", "true");
                                    wsClient.send(payload.toString());
                                } else if (wsClient == null || !wsClient.isOpen()) {
                                    printToChat("§cYou are not connected to the bridge websocket server!");
                                } else {
                                    printToChat("§csome error");
                                }
                            } catch (Exception ignored) {
                                printToChat("There was an error processing your item, please report this");
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                ));
    }

    public static void bridgeServerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("bridge")
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("devServer")
                        .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("url", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    disconnectWebSocket();
                                    String url = StringArgumentType.getString(ctx, "url");
                                    ImsWebSocketClient.wsUrl = url;
                                    connectWebSocket();
                                    TextUtils.printToChat("set server url to " + url);

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                ).executes(ctx -> {
                    disconnectWebSocket();
                    ImsWebSocketClient.wsUrl = "wss://ims-bridge.com";
                    connectWebSocket();
                    return Command.SINGLE_SUCCESS;
                })
        );
    }




}