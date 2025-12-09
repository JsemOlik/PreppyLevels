package jsemolik.dev.preppyLevels;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import jsemolik.dev.preppyLevels.config.ConfigLoader;
import jsemolik.dev.preppyLevels.config.PluginConfig;
import jsemolik.dev.preppyLevels.placeholders.PreppyLevelsPlaceholders;
import jsemolik.dev.preppyLevels.storage.StorageProvider;
import jsemolik.dev.preppyLevels.storage.StorageFactory;

import java.nio.file.Path;

public class PreppyLevels extends JavaPlugin {
    
    private PluginConfig config;
    private StorageProvider storageProvider;
    private LevelManager levelManager;
    private PreppyLevelsAPI api;
    private AutoXpHandler autoXpHandler;
    private XpBarUpdater xpBarUpdater;

    @Override
    public void onEnable() {
        getLogger().info("Initializing PreppyLevels...");
        
        try {
            // Load configuration
            Path dataFolder = getDataFolder().toPath();
            ConfigLoader configLoader = new ConfigLoader(this, dataFolder.resolve("config.yml"));
            config = configLoader.loadConfig();
            
            // Initialize storage
            storageProvider = StorageFactory.createStorageProvider(config, this, dataFolder);
            storageProvider.initialize().thenRun(() -> {
                getLogger().info("Storage provider initialized");
                
                // Initialize level manager and API (these are just object creation, safe on any thread)
                levelManager = new LevelManager(this, config);
                api = new PreppyLevelsAPI(this);
                
                // All Bukkit API calls must be on the main thread
                Bukkit.getScheduler().runTask(this, () -> {
                    // Initialize XP bar updater
                    xpBarUpdater = new XpBarUpdater(this);
                    xpBarUpdater.start();
                    
                    // Initialize auto XP handler if enabled
                    if (config.getAutoXpConfig() != null && config.getAutoXpConfig().isEnabled()) {
                        autoXpHandler = new AutoXpHandler(this);
                        getServer().getPluginManager().registerEvents(autoXpHandler, this);
                        getLogger().info("Auto XP handler enabled");
                    }
                    
                    // Register commands
                    LevelCommand levelCommand = new LevelCommand(this);
                    getCommand("level").setExecutor(levelCommand);
                    getCommand("level").setTabCompleter(levelCommand);
                    
                    // Register PlaceholderAPI expansion if available
                    if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                        new PreppyLevelsPlaceholders(this).register();
                        getLogger().info("PlaceholderAPI expansion registered!");
                    }
                    
                    getLogger().info("PreppyLevels has been enabled!");
                });
            }).exceptionally(throwable -> {
                getLogger().severe("Failed to initialize PreppyLevels: " + throwable.getMessage());
                throwable.printStackTrace();
                return null;
            });
        } catch (Exception e) {
            getLogger().severe("Failed to initialize PreppyLevels: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Shutting down PreppyLevels...");
        if (xpBarUpdater != null) {
            xpBarUpdater.stop();
        }
        if (autoXpHandler != null) {
            autoXpHandler.shutdown();
        }
        if (storageProvider != null) {
            storageProvider.shutdown().join();
        }
        if (levelManager != null) {
            levelManager.clearCache();
        }
        getLogger().info("PreppyLevels has been disabled!");
    }

    public PluginConfig getPluginConfig() {
        return config;
    }

    public StorageProvider getStorageProvider() {
        return storageProvider;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public PreppyLevelsAPI getAPI() {
        return api;
    }

    /**
     * Get the PreppyLevelsAPI instance from the plugin
     * This is a convenience method for other plugins to access the API
     * @return The PreppyLevelsAPI instance, or null if the plugin is not enabled
     */
    public static PreppyLevelsAPI getAPIInstance() {
        PreppyLevels plugin = JavaPlugin.getPlugin(PreppyLevels.class);
        if (plugin != null && plugin.isEnabled()) {
            return plugin.getAPI();
        }
        return null;
    }
}

