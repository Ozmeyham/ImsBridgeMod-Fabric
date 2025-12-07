package ozmeyham.imsbridge.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static ozmeyham.imsbridge.commands.BridgeCommands.*;
import static ozmeyham.imsbridge.commands.BridgeColourCommand.bridgeColourCommand;
import static ozmeyham.imsbridge.commands.CombinedBridgeCommands.*;
import static ozmeyham.imsbridge.commands.CombinedBridgeColourCommand.combinedBridgeColourCommand;

public class CommandHandler {
    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        // bridge commands
        bridgeOnlineCommand(dispatcher);
        bridgeOnlineCommandShort(dispatcher);
        bridgeHelpCommand(dispatcher);
        bridgeKeyCommand(dispatcher);
        bridgeToggleCommand(dispatcher);
        bridgeColourCommand(dispatcher);
        // cbridge commands
        combinedBridgeToggleCommand(dispatcher);
        combinedBridgeChatCommand(dispatcher);
        combinedBridgeChatCommandShort(dispatcher);
        combinedBridgeChatCommandShort2(dispatcher);
        combinedBridgeMsgCommand(dispatcher);
        combinedBridgeMsgCommand2(dispatcher);
        combinedBridgeColourCommand(dispatcher);
        combinedBridgeHelpCommand(dispatcher);
    }
}