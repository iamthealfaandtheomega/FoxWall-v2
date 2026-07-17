package thezowi.foxwall.utils;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import thezowi.foxwall.libs.net.kyori.adventure.text.Component;

public enum FoxWallAPI {
	INSTANCE;
	private Object plugin = null;
	private Object server = null;
	private FilesManager files = SharedFunctions.file;
	private boolean enabled = false;
	public boolean log_flag = !this.isFlagPresent("-Dfoxwall.enableLibrariesLog");
	private AtomicBoolean inf = new AtomicBoolean(false);
	private static ScheduledExecutorService updater = null;
	
	protected String fw_api_type = "Free";
	protected String fw_api_v = "v1";
	protected String fw_api_key = "";
	
    public File get() throws URISyntaxException {
        CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
        if (codeSource != null) { return new File(codeSource.getLocation().toURI()); }
        return null;
    }
  
	public CompletableFuture<Void> enable(Object plugin, Object server, boolean enableForceColor) throws Exception {
		return CompletableFuture.runAsync(() -> {
			if (this.enabled) { return; }	
			this.plugin = plugin;
			this.server = server;
			
			SharedFunctions.logger.info("[CORE] Checking libraries...");
			DownloadLibraries dL = new DownloadLibraries();
			dL.downloadLibraries(this.log_flag).join();
			try { dL.shutdown(); } catch (Exception ig) {}
			if (dL.failed) { SharedFunctions.logger.severe("[CORE] Could not be loaded/downloaded a dependency."); SharedFunctions.logger.severe("[CORE] The plugin will shutdown. Probably an issue related to"); SharedFunctions.logger.severe("[CORE] your hosting was happened or bad connection."); SharedFunctions.logger.severe("[CORE] Plugin disabled for cause of an unexcepted error."); this.unload(); return; }
			SharedFunctions.logger.info("[CORE] All libraries checked and loaded!");
			ColorAPI.force = enableForceColor;
			ColorAPI.load();
			try { this.files = FilesManager.initializeAndWait(); } catch (Throwable e) { e.printStackTrace(); SharedFunctions.logger.severe("[FILES] Failed to load the configuration file!"); SharedFunctions.logger.severe("[CORE] Plugin disabled for cause of an unexcepted error."); this.unload(); return; }
			SharedFunctions.file = this.files;
			
			try {
				File jar = this.get();
				if (AntiMalware.isInfected(jar)) { throw new Throwable("Plugin seems to be compromised."); }
			} catch (Throwable e) {
				AntiMalware.alert();
				this.inf.set(true);
				this.unload();
				return;
			}
			
		    switch (PlatformType.getPlatform()) {
				case BUKKIT:
					try {
						thezowi.foxwall.proxy.bukkit.Loader.INSTANCE.loader();
					} catch (Throwable e) {
						e.printStackTrace();
						SharedFunctions.logger.severe("[CORE] Plugin disabled for cause of an unexcepted error.");
						this.unload();
						return;
					}
					break;
				default:
					break;
		    }
		    
		    SharedFunctions.checkForUpdates(false);
			try {
				File jar = this.get();
				if (AntiMalware.isInfected(jar)) { throw new Throwable("Plugin seems to be compromised."); }
			} catch (Throwable e) {
				AntiMalware.alert();
				this.inf.set(true);
				this.unload();
				return;
			}
			
			if (updater == null) {
			    updater = Executors.newSingleThreadScheduledExecutor();
			    updater.scheduleWithFixedDelay(() -> {
			    	if (!this.getFiles().getSettings().getUpdater().getAutomatically().getPeriod()) { SharedFunctions.checkForUpdates(true); }
			    }, 1, 1, TimeUnit.HOURS);
			}
	        this.enabled = true;
		});
	}
  
	public void unload() {
		SharedFunctions.logger.info("[CORE] Closing threads of the plugin...");
	    switch (PlatformType.getPlatform()) {
			case BUKKIT:
				try {
					thezowi.foxwall.proxy.bukkit.Loader.INSTANCE.unloader();
				} catch (Throwable e) {}
				break;
			default:
				break;
	    }
	    if (updater != null) {
	    	try { updater.shutdownNow(); } catch (Throwable e) {}
	    }
		SharedFunctions.logger.info("[CORE] Disabled!");
		if (this.inf.get() == true) {
		    new Thread(() -> {
		        try {
		            Thread.sleep(1000);
		            System.exit(1);
		        } catch (InterruptedException e) {
		            Thread.currentThread().interrupt();
		        }
		    }).start();
		}
		this.enabled = false;
	}
	
    public Object getPlugin() { return this.plugin; }
	public Object getProxy() { return this.server; }
	public FilesManager getFiles() { return this.files; }
	public String getVersion() { return SharedFunctions.ver; }
	
    public boolean isFlagPresent(String find) {
    	List<String> getter = ManagementFactory.getRuntimeMXBean().getInputArguments();
    	for (String arg : getter) {
    		if (arg.equalsIgnoreCase(find)) { return false; }
    	}
    	return true;
    }
    
    public void sendMessage(Object sender, Component component) {
	    switch (PlatformType.getPlatform()) {
			case BUKKIT:
				thezowi.foxwall.proxy.bukkit.Loader.INSTANCE.sendMessage(sender, component);
				break;
			default:
				break;
	    }
	}
}