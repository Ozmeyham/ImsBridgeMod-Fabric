package ozmeyham.imsbridge.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.Set;

public class TextUtils {
    // Simple in-game chat print because the command is so long for some reason
    public static void printToChat(String msg, boolean prefix) {
        Text message;
        if (prefix) message = Text.literal("§6IMS-Bridge Mod > §r" + msg);
        else message = Text.literal(msg);
        MinecraftClient.getInstance().execute(() ->
                MinecraftClient.getInstance().player.sendMessage(message, false)
        );
    }

    public static void printToChat(String msg) {
        printToChat(msg, true);
    }

    public static String sanitizeMessage (String msg) {
        return msg.replace("\"","''").replace("\\","\\\\");
    }

    // Lightweight JSON string escaper
    public static String quote(String s) {
        return "\"" + sanitizeMessage(s) + "\"";
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
