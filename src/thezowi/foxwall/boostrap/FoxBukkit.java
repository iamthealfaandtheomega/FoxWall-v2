package thezowi.foxwall.boostrap;

import java.lang.reflect.InaccessibleObjectException;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import thezowi.foxwall.utils.FoxWallAPI;
import thezowi.foxwall.utils.MinecraftVersion;
import thezowi.foxwall.utils.MinecraftVersion.V;
import thezowi.foxwall.utils.PlatformType;
import thezowi.foxwall.utils.SharedFunctions;

public final class FoxBukkit extends JavaPlugin {
	private final FoxWallAPI api = FoxWallAPI.INSTANCE;
	private final String plat_ver = Bukkit.getVersion();
 
	public void onLoad() {
		PlatformType.setPlatform(PlatformType.T.BUKKIT);
		final String bukkitVersion = this.getServer().getBukkitVersion();
		final String versionSplit = bukkitVersion.split("\\-")[0];
		final String[] versions = versionSplit.split("\\.");
		final int value = Integer.parseInt(versions[1]);
		final MinecraftVersion.V current = value <= 3 ? V.v1_3_AND_BELOW : V.parse(value);
		final int subversion = versions.length == 3 ? Integer.parseInt(versions[2]) : 0;
		MinecraftVersion.setVersion(current, subversion);
		
		SharedFunctions.logger = this.getLogger();
		try {
			SharedFunctions.ver = this.getDescription().getVersion().replace("-pv", "");
		} catch (Throwable ig) {
			SharedFunctions.ver = "1.6";
		} 
		SharedFunctions.plat_ver = this.plat_ver;
		SharedFunctions.path = this.getDataFolder().getParentFile().toPath();
	}
  
	public void onEnable() {
		CompletableFuture.runAsync(() -> {
			SharedFunctions.header();
			try {
				this.api.enable(this, Bukkit.getServer(), false).join();
			} catch (Throwable ig) {
			    if (ig.getCause() instanceof InaccessibleObjectException) {
			    	SharedFunctions.logger.severe(" ");
			        SharedFunctions.logger.severe("[CORE] Java module access error: use '--add-opens java.base/java.lang=ALL-UNNAMED' in your start script.");
			        SharedFunctions.logger.severe("[CORE] Plugin can't load their dependencies. Disabled.");
			        SharedFunctions.logger.severe("[CORE] USE THIS PLUGIN FROM A TRUST SOURCE!");
			        SharedFunctions.logger.severe(" ");
			    } else { ig.printStackTrace(); }
				SharedFunctions.logger.severe("[CORE] Plugin disabled for cause of an unexcepted error.");
				return;
			}
		});
	}
  
	public void onDisable() { CompletableFuture.runAsync(() -> { HandlerList.unregisterAll(this); this.api.unload(); }); }
}
