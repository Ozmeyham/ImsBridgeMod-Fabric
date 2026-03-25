package ozmeyham.imsbridge.utils;

import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class TextUtils {
    // Simple in-game chat print because the command is so long for some reason
    public static void printToChat(String msg, boolean prefix) {
        Component message;
        if (prefix) message = Component.literal("§6IMS-Bridge Mod > §r" + msg);
        else message = Component.literal(msg);
        Minecraft.getInstance().execute(() ->
                Minecraft.getInstance().player.displayClientMessage(message, false)
        );
    }

    public static void printToChat(Component msg, boolean prefix) {
        Component message;
        if (prefix) message = Component.literal("§6IMS-Bridge Mod > §r").append(msg);
        else message = msg;
        Minecraft.getInstance().execute(() ->
                Minecraft.getInstance().player.displayClientMessage(message, false)
        );
    }

    public static void printToChat(String msg) {
        printToChat(msg, true);
    }

    public static Boolean isSkyblockChannelChange(String content) {
        Set<String> validMessages = Set.of(
                "You're already in this channel!",
                "You are now in the GUILD channel",
                "You are now in the ALL channel",
                "You are now in the PARTY channel",
                "You are now in the OFFICER channel",
                "You are now in the SKYBLOCK CO-OP channel");
        return validMessages.contains(content);
    }
}
