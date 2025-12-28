package ozmeyham.imsbridge.commands;

import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

import static ozmeyham.imsbridge.IMSBridge.combinedBridgeEnabled;
import static ozmeyham.imsbridge.ImsWebSocketClient.wsClient;
import static ozmeyham.imsbridge.utils.TextUtils.printToChat;

public final class CombinedBridgePartyCommand {

    public static long lastParty = -1;
    public static int partySpotsLeft = -1;

    public static void bridgePartyCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("cbridge")
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("party")
                        .then(RequiredArgumentBuilder.<FabricClientCommandSource, Integer>argument("playerCap", IntegerArgumentType.integer(1))
                                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("reason", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            int partyCap = IntegerArgumentType.getInteger(ctx, "playerCap");
                                            partySpotsLeft = partyCap;
                                            String reason = StringArgumentType.getString(ctx, "reason");
                                            String message = reason + ". Do !join " + MinecraftClient.getInstance().player.getName().getString() + " to join! (" + partyCap + " spots)";
                                            if (combinedBridgeEnabled && wsClient.isOpen() && wsClient != null) {
                                                JsonObject payload = new JsonObject();
                                                payload.addProperty("from","mc");
                                                payload.addProperty("msg",message);
                                                payload.addProperty("combinedbridge","true");
                                                wsClient.send(payload.toString());
                                                lastParty = System.currentTimeMillis();
                                            } else if (wsClient == null || wsClient.isClosed()) {
                                                printToChat("§cYou are not connected to the bridge websocket server!");
                                            } else {
                                                printToChat("§cYou need to enable combined bridge messages to use this command! §6§o/cbridge toggle");
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
        );
    }
}
