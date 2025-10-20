package ozmeyham.imsbridge.utils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ozmeyham.imsbridge.ImsWebSocketClient.connectWebSocket;
import static ozmeyham.imsbridge.utils.TextUtils.printToChat;

public class BridgeKeyUtils {

    public static String bridgeKey = null;
    public static Boolean shouldCheckKey = true;

    public static boolean uuidValidator(String uuid) {
        String regex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(uuid);
        return matcher.matches();
    }

    public static Boolean isValidBridgeKey() {
        return bridgeKey != null && !bridgeKey.isEmpty() && uuidValidator(bridgeKey);
    }
}
