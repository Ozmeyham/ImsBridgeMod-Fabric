package ozmeyham.imsbridge.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import ozmeyham.imsbridge.IMSBridge;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

import static ozmeyham.imsbridge.utils.TextUtils.printToChat;

public class UpdateChecker {
    public static String getModVersion() {
        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer("ims-bridge");
        return container.map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
    }
    public static void checkForUpdates() {
        String currentVersion = getModVersion();
        if (currentVersion.equals("unknown")) return;
        new Thread(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL("https://raw.githubusercontent.com/Ozmeyham/ImsBridgeMod-1.21.5/main/gradle.properties").openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                String latestVersion = null;

                while ((line = in.readLine()) != null) {
                    if (line.trim().startsWith("mod_version=")) {
                        latestVersion = line.split("=")[1].trim();
                        break;
                    }
                }
                in.close();

                if (latestVersion == null) {
                    IMSBridge.LOGGER.warn("mod_version not found in gradle.properties");
                    return;
                }

                if (!latestVersion.equalsIgnoreCase(currentVersion)) {
                    printToChat("§bUpdate available! §7Latest version: §b" + latestVersion +
                            "§e (You have §6" + currentVersion + "§e)");
                }

            } catch (Exception e) {
                IMSBridge.LOGGER.warn("Failed to check for updates: " + e.getMessage());
            }
        }).start();
    }
}
