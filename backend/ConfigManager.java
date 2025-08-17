package backend;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import backend.Config;;

public class ConfigManager {
    private static final String CONFIG_FILE = "backend/config.json";
    private static Config config;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static Config getconfig(){
        if (config == null){

            try {
                String json = Files.readString(Path.of(CONFIG_FILE));
                config = gson.fromJson(json , Config.class);
            } catch (Exception e) {
                System.out.println("Config load failed, using defaults: ");
                e.printStackTrace();
                config = new Config();
                config.device_name = "default";
                config.port = 8080;
                config.Machine_ip = "127.0.0.1";
                saveConfig();
            }

        }
        return config;
    }

    public static void saveConfig(){
        try {
            String json = gson.toJson(config);
            Files.writeString(Path.of(CONFIG_FILE), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getinJSON(){
        JsonObject json = gson.toJsonTree(getconfig()).getAsJsonObject();
        json.addProperty("Machine_ip", config.Machine_ip);
        return gson.toJson(json);
    }
}
