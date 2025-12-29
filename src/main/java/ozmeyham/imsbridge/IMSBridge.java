package ozmeyham.imsbridge;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ozmeyham.imsbridge.utils.ConfigUtils.loadConfig;
import static ozmeyham.imsbridge.EventHandler.eventListener;

public class IMSBridge implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("imsbridge");
	public static Boolean bridgeEnabled = false; // enable/disable seeing bridge messages
	public static Boolean combinedBridgeEnabled = true; // enable/disable seeing cbridge messages
	public static Boolean combinedBridgeChatEnabled = false; // enable/disable sending cbridge messages with no command prefix (like /chat guild)
	public static Boolean firstLogin = true; //default value when first downloading the mod
	public static Boolean checkedForUpdate = false;

	@Override
	public void onInitializeClient() {
		loadConfig();
		eventListener();
	}
}