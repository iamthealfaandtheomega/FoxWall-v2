package thezowi.foxwall.boostrap;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import thezowi.foxwall.proxy.velocity.LoggerWrapper;
import thezowi.foxwall.utils.PlatformType;
import thezowi.foxwall.utils.SharedFunctions;
import thezowi.foxwall.utils.PlatformType.T;

@Plugin(
        id = "foxwall",
        name = "FoxWall",
        version = "1.0.0",
        description = "Protects your backend from being scanned and/or joinable (BungeeGuard and Velocity Modern alternative).",
        url = "https://discord.zowi.gay/",
        authors = {"NovaCraft254"},
        dependencies = {}
)
public final class FoxVelocity {
    private final ProxyServer sv;
    private final Logger logger;
    @SuppressWarnings("unused")
	private final File path;
	@SuppressWarnings("unused")
	private Plugin plugin;
    private String version = "N/A";

    @Inject
    public FoxVelocity(ProxyServer proxy, final org.slf4j.Logger logger, @DataDirectory Path path) {
    	PlatformType.setPlatform(T.VELOCITY);
    	
        this.sv = proxy;
        this.logger = new LoggerWrapper(logger);
        this.path = new File(path.toFile().getParentFile(), "FoxWall");
        
        final Plugin plugin = this.getClass().getDeclaredAnnotation(Plugin.class);
        this.plugin = plugin;
        this.version = plugin.version();
        
        SharedFunctions.logger = this.logger;
        SharedFunctions.ver = this.version;
        SharedFunctions.plat_ver = this.sv.getVersion().getVersion();
        SharedFunctions.path = path.getParent();
    }
  
    @Subscribe(async = true)
    public EventTask onProxyInitialization(ProxyInitializeEvent event) {
    	return EventTask.async(() -> {
    		SharedFunctions.header();
    		
    		CompletableFuture.runAsync(() -> {
    			SharedFunctions.getProxyIP();
    		});
	    });
    }
}
