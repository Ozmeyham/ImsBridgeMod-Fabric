package ozmeyham.imsbridge.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static ozmeyham.imsbridge.utils.ConfigUtils.saveConfigValue;
import static ozmeyham.imsbridge.utils.TextUtils.printToChat;

public final class BridgeColourCommand {

    // Available colours
    private static final List<String> VALID_COLORS = Arrays.asList(
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
            "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple",
            "yellow", "white"
    );
    // Default bridge colour formatting
    public static String bridgeC1 = "§9";
    public static String bridgeC2 = "§6";
    public static String bridgeC3 = "§f";


    public static void bridgeColourCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("bridge")
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("colour")
                        .executes(context -> {
                            bridgeC1 = "§9"; bridgeC2 = "§6"; bridgeC3 = "§f";
                            bridgeColourFormat();
                            return 1;
                        })
                        .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("colour1", StringArgumentType.word())
                                .suggests((context, builder) -> CommandSource.suggestMatching(VALID_COLORS, builder))
                                .executes(context -> {
                                    bridgeC1 = COLOR_CODE_MAP.getOrDefault(StringArgumentType.getString(context, "colour1"), "§9");
                                    bridgeC2 = bridgeC1;
                                    bridgeC3 = bridgeC1;
                                    bridgeColourFormat();
                                    return 1;
                                })
                                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("colour2", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(VALID_COLORS, builder))
                                        .executes(context -> {
                                            bridgeC1 = COLOR_CODE_MAP.getOrDefault(StringArgumentType.getString(context, "colour1"), "§9");
                                            bridgeC2 = COLOR_CODE_MAP.getOrDefault(StringArgumentType.getString(context, "colour2"), "§6");
                                            bridgeC3 = bridgeC2;
                                            bridgeColourFormat();
                                            return 1;
                                        })
                                        .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("colour3", StringArgumentType.word())
                                                .suggests((context, builder) -> CommandSource.suggestMatching(VALID_COLORS, builder))
                                                .executes(context -> {
                                                    bridgeC1 = COLOR_CODE_MAP.getOrDefault(StringArgumentType.getString(context, "colour1"), "§9");
                                                    bridgeC2 = COLOR_CODE_MAP.getOrDefault(StringArgumentType.getString(context, "colour2"), "§6");
                                                    bridgeC3 = COLOR_CODE_MAP.getOrDefault(StringArgumentType.getString(context, "colour3"), "§f");
                                                    bridgeColourFormat();
                                                    return 1;
                                                })
                                )
                        )
                )
            )
        );
    }

    public static void bridgeColourFormat() {
        saveConfigValue("bridge_colour1", bridgeC1);
        saveConfigValue("bridge_colour2", bridgeC2);
        saveConfigValue("bridge_colour3", bridgeC3);
        printToChat("§cYou have set the bridge colour format to: \n" + bridgeC1 + "Guild > " + bridgeC2 + "Username" + " §9[DISC]§f: " + bridgeC3 + "Message");
    }



    public static final Map<String, String> COLOR_CODE_MAP = Map.ofEntries(
            Map.entry("black", "§0"),
            Map.entry("dark_blue", "§1"),
            Map.entry("dark_green", "§2"),
            Map.entry("dark_aqua", "§3"),
            Map.entry("dark_red", "§4"),
            Map.entry("dark_purple", "§5"),
            Map.entry("gold", "§6"),
            Map.entry("gray", "§7"),
            Map.entry("dark_gray", "§8"),
            Map.entry("blue", "§9"),
            Map.entry("green", "§a"),
            Map.entry("aqua", "§b"),
            Map.entry("red", "§c"),
            Map.entry("light_purple", "§d"),
            Map.entry("yellow", "§e"),
            Map.entry("white", "§f")
    );


}
