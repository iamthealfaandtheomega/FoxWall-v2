package thezowi.foxwall.boostrap;

import java.util.concurrent.CompletableFuture;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import thezowi.foxwall.utils.PlatformType;
import thezowi.foxwall.utils.SharedFunctions;

public final class FoxBungee extends Plugin {
	private final String plat_ver = ProxyServer.getInstance().getVersion();
	 
	public void onLoad() {
		PlatformType.setPlatform(PlatformType.T.BUNGEECORD);
		
		SharedFunctions.logger = this.getLogger();
		try {
			SharedFunctions.ver = this.getDescription().getVersion().replace("-pv", "");
		} catch (Throwable ig) {
			SharedFunctions.ver = "1.7";
		} 
		SharedFunctions.plat_ver = this.plat_ver;
		SharedFunctions.path = this.getDataFolder().getParentFile().toPath();
	}
  
	public void onEnable() {
		SharedFunctions.header();
		
		CompletableFuture.runAsync(() -> {
			SharedFunctions.getProxyIP();
		});
		return;
	}
	
	public void onDisable() {}
}
