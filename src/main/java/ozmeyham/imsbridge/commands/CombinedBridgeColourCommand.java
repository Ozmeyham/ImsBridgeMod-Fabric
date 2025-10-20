package ozmeyham.imsbridge.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import com.mojang.brigadier.context.CommandContext;

import java.util.Arrays;
import java.util.List;

import static ozmeyham.imsbridge.commands.BridgeColourCommand.COLOR_CODE_MAP;
import static ozmeyham.imsbridge.utils.ConfigUtils.saveConfigValue;
import static ozmeyham.imsbridge.utils.TextUtils.printToChat;

public final class CombinedBridgeColourCommand {

    // Available colours
    private static final List<String> VALID_COLORS = Arrays.asList(
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
            "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple",
            "yellow", "white"
    );
    // Default bridge colour formatting
    public static String cbridgeC1 = "§4";
    public static String cbridgeC2 = "§6";
    public static String cbridgeC3 = "§f";


    public static void combinedBridgeColourCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("cbridge")
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("colour")
                        .executes(context -> {
                            cbridgeC1 = "§4"; cbridgeC2 = "§6"; cbridgeC3 = "§f";
                            cbridgeColourFormat();
                            return 1;
                        })
                        .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("colour4", StringArgumentType.word())
                                .suggests((context, builder) -> CommandSource.suggestMatching(VALID_COLORS, builder))
                                .executes(context -> {
                                    cbridgeC1 = COLOR_CODE_MAP.getOrDefault(StringArgumentType.getString(context, "colour4"), "§4");
                                    cbridgeC2 = cbridgeC1;
                                    cbridgeC3 = cbridgeC1;
                                    cbridgeColourFormat();
                                    return 1;
                                })
                                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("colour5", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(VALID_COLORS, builder))
                                        .executes(context -> {
                                            cbridgeC1 = COLOR_CODE_MAP.getOrDefault(StringArgumentType.getString(context, "colour4"), "§4");
                                            cbridgeC2 = COLOR_CODE_MAP.getOrDefault(StringArgumentType.getString(context, "colour5"), "§6");
                                            cbridgeC3 = cbridgeC2;
                                            cbridgeColourFormat();
                                            return 1;
                                        })
                                        .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("colour6", StringArgumentType.word())
                                                .suggests((context, builder) -> CommandSource.suggestMatching(VALID_COLORS, builder))
                                                .executes(context -> {
                                                    cbridgeC1 = COLOR_CODE_MAP.getOrDefault(StringArgumentType.getString(context, "colour4"), "§4");
                                                    cbridgeC2 = COLOR_CODE_MAP.getOrDefault(StringArgumentType.getString(context, "colour5"), "§6");
                                                    cbridgeC3 = COLOR_CODE_MAP.getOrDefault(StringArgumentType.getString(context, "colour6"), "§f");
                                                    cbridgeColourFormat();
                                                    return 1;
                                                })
                                        )
                                )
                        )
                )
        );
    }

    public static void cbridgeColourFormat() {
        saveConfigValue("cbridge_colour1", cbridgeC1);
        saveConfigValue("cbridge_colour2", cbridgeC2);
        saveConfigValue("cbridge_colour3", cbridgeC3);

        printToChat("§cYou have set the cbridge colour format to: \n" + cbridgeC1 + "CB > " + cbridgeC2 + "Username: " + cbridgeC3 + "Message");
    }
}
