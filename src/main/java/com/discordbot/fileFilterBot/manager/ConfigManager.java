package com.discordbot.fileFilterBot.manager;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class ConfigManager {
    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());
    private String token;
    private long applicationId;
    private boolean isWhitelist;
    private final Set<String> blacklistedFileTypes;
    private final Set<String> whitelistedFileTypes;
    
    public ConfigManager() {
        blacklistedFileTypes = new HashSet<>();
        whitelistedFileTypes = new HashSet<>();
    }
    
    public boolean load() {
        File configFile = new File("config.json");
        
        if (!configFile.exists()) {
            LOGGER.info("Configuration file not found. Creating a new one.");
            return createDefaultConfig();
        }
        
        try {
            String content = new String(Files.readAllBytes(Paths.get("config.json")));
            JSONObject config = new JSONObject(content);
            
            token = config.getString("token");
            applicationId = config.getLong("application_id");
            isWhitelist = config.getBoolean("isWhitelist");
            
            JSONArray blacklistedFileTypesArray = config.getJSONArray("blacklisted_file_types");
            for (int i = 0; i < blacklistedFileTypesArray.length(); i++) {
                blacklistedFileTypes.add(blacklistedFileTypesArray.getString(i));
            }

            JSONArray whitelistedFileTypesArray = config.getJSONArray("whitelisted_file_types");
            for (int i = 0; i < whitelistedFileTypesArray.length(); i++) {
                whitelistedFileTypes.add(whitelistedFileTypesArray.getString(i));
            }
            
            return true;
        } catch (Exception e) {
            LOGGER.severe("Error loading configuration: " + e.getMessage());
            return false;
        }
    }
    
    private boolean createDefaultConfig() {
        try {
            JSONObject config = new JSONObject();
            config.put("token", "YOUR_BOT_TOKEN_HERE");
            config.put("application_id", "YOUR_APPLICATION_ID_HERE");
            config.put("isWhitelist", true);
            
            JSONArray blacklistedFileTypesArray = new JSONArray();
            blacklistedFileTypesArray.put("pdf");
            blacklistedFileTypesArray.put("exe");
            blacklistedFileTypesArray.put("sh");
            blacklistedFileTypesArray.put("cmd");
            blacklistedFileTypesArray.put("zip");
            blacklistedFileTypesArray.put("7zip");
            blacklistedFileTypesArray.put("tar");
            
            config.put("blacklisted_file_types", blacklistedFileTypesArray);

            JSONArray whitelistedFileTypesArray = new JSONArray();
            whitelistedFileTypesArray.put("mp4");
            whitelistedFileTypesArray.put("mov");
            whitelistedFileTypesArray.put("avi");
            whitelistedFileTypesArray.put("jpeg");
            whitelistedFileTypesArray.put("mp3");
            whitelistedFileTypesArray.put("gif");

            config.put("whitelisted_file_types", whitelistedFileTypesArray);
            
            FileWriter file = new FileWriter("config.json");
            file.write(config.toString(4));
            file.close();
            
            LOGGER.info("Default configuration file created. Please edit it with your bot details.");
            return false;
        } catch (Exception e) {
            LOGGER.severe("Error creating default configuration: " + e.getMessage());
            return false;
        }
    }
    
    private boolean saveConfig() {
        try {
            JSONObject config = new JSONObject();
            config.put("token", this.token);
            config.put("application_id", this.applicationId);
            config.put("isWhitelist", this.isWhitelist);
            
            JSONArray blacklistedFileTypesArray = new JSONArray();
            for (Iterator<String> iterator = blacklistedFileTypes.iterator(); iterator.hasNext();) {
				String fileType = (String) iterator.next();
				blacklistedFileTypesArray.put(fileType);
			}
            
            config.put("blacklisted_file_types", blacklistedFileTypesArray);

            JSONArray whitelistedFileTypesArray = new JSONArray();
            for (Iterator<String> iterator = whitelistedFileTypes.iterator(); iterator.hasNext();) {
				String fileType = (String) iterator.next();
				whitelistedFileTypesArray.put(fileType);
			}

            config.put("whitelisted_file_types", whitelistedFileTypesArray);
            
            FileWriter file = new FileWriter("config.json");
            file.write(config.toString(4));
            file.close();
            
            LOGGER.info("Default configuration file created. Please edit it with your bot details.");
            return false;
        } catch (Exception e) {
            LOGGER.severe("Error creating default configuration: " + e.getMessage());
            return false;
        }
    }
    
    public String getToken() {
        return token;
    }
    
    public long getApplicationId() {
        return applicationId;
    }

    public boolean isWhitelist() {
        return isWhitelist;
    }
    
    public Set<String> getBlackListedFileTypes() {
        return blacklistedFileTypes;
    }
    
    public Set<String> getWhiteListedFileTypes() {
        return whitelistedFileTypes;
    }

    public void addToWhiteList(String... fileTypes) {
        whitelistedFileTypes.addAll(List.of(fileTypes));
        saveConfig();
    }

    public void removeFromWhiteList(String... fileTypes) {
        List<String> fileTypesList = List.of(fileTypes);
        whitelistedFileTypes.removeIf(fileTypesList::contains);
        saveConfig();
    }

    public void addToBlackList(String... fileTypes) {
        blacklistedFileTypes.addAll(List.of(fileTypes));
        saveConfig();
    }

    public void removeFromBlackList(String... fileTypes) {
        List<String> fileTypesList = List.of(fileTypes);
        blacklistedFileTypes.removeIf(fileTypesList::contains);
        saveConfig();
    }
}