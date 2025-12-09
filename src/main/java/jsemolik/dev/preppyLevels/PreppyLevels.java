package jsemolik.dev.preppyLevels;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;

@Plugin(id = "preppylevels", name = "PreppyLevels", version = "1.0-SNAPSHOT", description = "The main plugin of the Skippy collection, connecting all other plugins together.", url = "jsemolik.dev", authors = {"Oliver Steiner"})
public class PreppyLevels {

    @Inject
    private Logger logger;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
    }
}
