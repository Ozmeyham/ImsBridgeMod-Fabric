package ozmeyham.imsbridge.commands;

import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import ozmeyham.imsbridge.utils.TextUtils;

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
                                    §b/cbridge show: §7Sends your held item to cbridge, similar to how /show works.
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

        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("chat")
                .then(argument("type", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String type = StringArgumentType.getString(ctx, "type");
                            if (!type.equalsIgnoreCase("b")) {
                                MinecraftClient.getInstance().player.networkHandler.sendChatMessage("/chat " + type);
                                return Command.SINGLE_SUCCESS;
                            }
                                combinedBridgeChatEnabled = true;
                                saveConfigValue("combinedBridgeChatEnabled", "true");
                                printToChat("§aEntered combined bridge chat!");

                            return Command.SINGLE_SUCCESS;
                        })
                ));
    }

    public static void combinedBridgeMsgCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("cbc")
                .then(argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String message = StringArgumentType.getString(ctx, "message");
                            if (combinedBridgeEnabled && wsClient != null && wsClient.isOpen() ) {
                                JsonObject payload = new JsonObject();
                                payload.addProperty("from","mc");
                                payload.addProperty("msg",message);
                                payload.addProperty("combinedbridge","true");
                                wsClient.send(payload.toString());
                            }
                            else if (wsClient == null || !wsClient.isOpen()){
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

    public static void combinedBridgeShowCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("cbridge")
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

                                if (combinedBridgeEnabled && wsClient != null && wsClient.isOpen()) {
                                    JsonObject payload = new JsonObject();
                                    payload.addProperty("from", "show");
                                    payload.addProperty("msg", message);
                                    payload.add("jsonStack", jsonStack);
                                    payload.addProperty("show", "true");
                                    payload.addProperty("combinedbridge", "true");
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
}
