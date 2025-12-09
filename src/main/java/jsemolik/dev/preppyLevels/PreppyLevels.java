package jsemolik.dev.preppyLevels;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import jsemolik.dev.preppyLevels.config.ConfigLoader;
import jsemolik.dev.preppyLevels.config.PluginConfig;
import jsemolik.dev.preppyLevels.storage.StorageProvider;
import jsemolik.dev.preppyLevels.storage.StorageFactory;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "preppylevels", name = "PreppyLevels", version = "1.0-SNAPSHOT", 
        description = "A leveling plugin for Velocity proxy with customizable XP system", 
        url = "jsemolik.dev", authors = {"Oliver Steiner"})
public class PreppyLevels {

    @Inject
    private Logger logger;
    
    @Inject
    private ProxyServer server;
    
    @Inject
    @com.google.inject.name.Named("dataDirectory")
    private Path dataDirectory;

    private PluginConfig config;
    private StorageProvider storageProvider;
    private LevelManager levelManager;
    private PreppyLevelsAPI api;
    private AutoXpHandler autoXpHandler;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Initializing PreppyLevels...");
        
        try {
            // Load configuration
            ConfigLoader configLoader = new ConfigLoader(logger, dataDirectory.resolve("config.yml"));
            config = configLoader.loadConfig();
            
            // Initialize storage
            storageProvider = StorageFactory.createStorageProvider(config, logger, dataDirectory);
            storageProvider.initialize().thenRun(() -> {
                logger.info("Storage provider initialized");
                
                // Initialize level manager
                levelManager = new LevelManager(this, config);
                
                // Initialize API
                api = new PreppyLevelsAPI(this);
                
                // Initialize auto XP handler if enabled
                if (config.getAutoXpConfig() != null && config.getAutoXpConfig().isEnabled()) {
                    autoXpHandler = new AutoXpHandler(this);
                    server.getEventManager().register(this, autoXpHandler);
                    logger.info("Auto XP handler enabled");
                }
                
                logger.info("PreppyLevels has been enabled!");
            }).exceptionally(throwable -> {
                logger.error("Failed to initialize PreppyLevels", throwable);
                return null;
            });
        } catch (Exception e) {
            logger.error("Failed to initialize PreppyLevels", e);
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("Shutting down PreppyLevels...");
        if (storageProvider != null) {
            storageProvider.shutdown().join();
        }
        if (levelManager != null) {
            levelManager.clearCache();
        }
        logger.info("PreppyLevels has been disabled!");
    }

    public Logger getLogger() {
        return logger;
    }

    public ProxyServer getServer() {
        return server;
    }

    public PluginConfig getConfig() {
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
}
