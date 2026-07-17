package thezowi.foxwall.proxy.bukkit;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import thezowi.foxwall.libs.net.kyori.adventure.text.Component;
import thezowi.foxwall.libs.net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import thezowi.foxwall.libs.net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import thezowi.foxwall.proxy.bukkit.listeners.PEListener;
import thezowi.foxwall.proxy.bukkit.listeners.PLListener;
import thezowi.foxwall.utils.AdventureWrapper;
import thezowi.foxwall.utils.ColorAPI;
import thezowi.foxwall.utils.FilesManager;
import thezowi.foxwall.utils.FoxWallAPI;
import thezowi.foxwall.utils.MinecraftVersion;
import thezowi.foxwall.utils.MinecraftVersion.V;
import thezowi.foxwall.utils.SharedFunctions;

public enum Loader {
	INSTANCE;
	private static final FoxWallAPI api = FoxWallAPI.INSTANCE;
	private final static Server server = (Server) api.getProxy();
	private final static JavaPlugin plugin = (JavaPlugin) api.getPlugin();
	
	private static final FilesManager file = SharedFunctions.file;
	
	private static boolean isPHEnabled = false;
	//private static boolean isTKEnabled = false;
	
	private static String packet_handler = "";
	//private static String token_resolver = "";
	private static Metrics metrics = null;
	
	private static boolean ADVENTURE_CHECKED = false;
	private static Method ADVENTURE_METHOD = null;
	private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

	public void loader() throws Throwable {
        ColorAPI.force = false;
		ColorAPI.load();
		
		PluginCommand command = plugin.getCommand("foxwall");
		CommandManager cmdManager = new thezowi.foxwall.proxy.bukkit.CommandManager();
		if (command != null) {
			if (command != null) {
			    command.setExecutor(cmdManager);
			    command.setTabCompleter(cmdManager);
			    command.setAliases(List.of("foxw", "fw"));
			    command.setDescription("View list of commands in FoxWall.");
			}
		}
		
		final String bukkitVersion = server.getBukkitVersion();
		final String versionSplit = bukkitVersion.split("\\-")[0];
		final String[] versions = versionSplit.split("\\.");
		final int value = Integer.parseInt(versions[1]);
		final MinecraftVersion.V current = value <= 3 ? V.v1_3_AND_BELOW : V.parse(value);
		final int subversion = versions.length == 3 ? Integer.parseInt(versions[2]) : 0;
		MinecraftVersion.setVersion(current, subversion);
		
		final String setting_ph = api.getFiles().getSettings().getPacketHandler().getType(); //setting_tr = api.getFiles().getSettings().getTokenResolver().getType();
		//loadTokenHandler(setting_tr);
		loadPacketHandler(setting_ph);

		if (!isPHEnabled) { // if (!isTKEnabled && !isPHEnabled) {
			SharedFunctions.logger.severe("[CORE] ");
			SharedFunctions.logger.severe("[CORE] The plugin has disabled their modules!");
			SharedFunctions.logger.severe("[CORE] Right now isn't enabled, be aware of this and setup");
			SharedFunctions.logger.severe("[CORE] correctly for protect your backend.");
			SharedFunctions.logger.severe("[CORE] ");
		}
		
        try {
        	if (file.getSettings().getMetrics()) {
        		metrics = new Metrics(plugin, 26704);
        	} else {
        		SharedFunctions.logger.info("[CORE] Metrics disabled in config, ignoring it...");
        	}
        } catch (Exception exception) {}
	}
	
	/*
	public static void loadTokenHandler(final String value) throws Throwable {
		if (isTKEnabled && !value.equalsIgnoreCase(token_resolver)) {
			SharedFunctions.logger.warning("[CORE] We found a change of Token-Resolver. Starting migration...");
			isTKEnabled = false;
		} else if (isTKEnabled && value.equalsIgnoreCase(token_resolver)) { return; }
		token_resolver = value;
		switch (token_resolver) {
			case "bungeeguard": {
				if (FilesManager.ALLOWED_TOKENS.isEmpty()) {
					SharedFunctions.logger.severe("[CORE] We're using BungeeGuard resolver for token-resolver");
					SharedFunctions.logger.severe("[CORE] but seems you leave empty the 'Allowed Tokens'. Disabling");
					SharedFunctions.logger.severe("[CORE] feature.");
					break;
				}
				SharedFunctions.logger.info("[CORE] Using 'VelocityModern' for token-resolver.");
				isTKEnabled = true;
				break;
			}
			case "velocitymodern": {
				if (FilesManager.ALLOWED_TOKENS.isEmpty()) {
					SharedFunctions.logger.severe("[CORE] We're using VelocityModern resolver for token-resolver");
					SharedFunctions.logger.severe("[CORE] but seems you leave empty the 'Allowed Tokens'. Disabling");
					SharedFunctions.logger.severe("[CORE] feature.");
					break;
				}
				SharedFunctions.logger.info("[CORE] Using 'VelocityModern' for token-resolver.");
				isTKEnabled = true;
				break;
			}
			case "disable": {
				SharedFunctions.logger.severe("[CORE] You disabled the mechanism of token-resolver!");
				isTKEnabled = false;
				break;
			}
			default:
				SharedFunctions.logger.severe("[CORE] You didn't used a valid option for token-resolver!");
				isTKEnabled = false;
				break;
		}
	}
	*/
	
	public static void loadPacketHandler(final String value) throws Throwable {
		if (isPHEnabled && !value.equalsIgnoreCase(packet_handler)) {
			SharedFunctions.logger.warning("[CORE] We found a change of Packet-Handler. Starting migration...");
			switch (packet_handler) {
				case "protocollib":
					PLListener.unregister();
					break;
				case "packetevents":
					PEListener.unregister();
					break;
				default:
					break;
			}
			isPHEnabled = false;
		} else if (isPHEnabled && value.equalsIgnoreCase(packet_handler)) { return; }
		
		Plugin depend = null;
		boolean enable = false;
		packet_handler = value;
		
		switch (packet_handler) {
			case "auto": {
				depend = server.getPluginManager().getPlugin("ProtocolLib");
				if (depend != null) { enable = depend.isEnabled(); }
				if (!enable) {
					depend = server.getPluginManager().getPlugin("packetevents");
					if (depend != null) { enable = depend.isEnabled(); }
					if (enable) {
						SharedFunctions.logger.info("[CORE] The automatically detector found: PacketEvents");
						server.getPluginManager().registerEvents(new PEListener(), plugin);
						
						SharedFunctions.logger.info("[CORE] Using 'PacketEvents' for packet-handler.");
						isPHEnabled = true;
						break;
					}
				} else {
					SharedFunctions.logger.info("[CORE] The automatically detector found: ProtocolLib (high priority)");
					server.getPluginManager().registerEvents(new PLListener(), plugin);
					
					SharedFunctions.logger.info("[CORE] Using 'ProtocolLib' for packet-handler.");
					isPHEnabled = true;
					break;
				}
				SharedFunctions.logger.severe("[CORE] We can't found a packet-handler! (disabled)");
				SharedFunctions.logger.severe("[CORE] Please, mantains always secure your server with");
				SharedFunctions.logger.severe("[CORE] token-resolver feature or install one supported");
				SharedFunctions.logger.severe("[CORE] packet-handler!");
				break;
			}
			
			case "protocollib": {
				depend = server.getPluginManager().getPlugin("ProtocolLib");
				if (depend != null) { enable = depend.isEnabled(); }
				if (!enable) { SharedFunctions.logger.severe("[CORE] We can't found ProtocolLib, seems doesn't was installed."); SharedFunctions.logger.severe("[CORE] Install ProtocolLib to use this or change the packet-handler to an supported."); SharedFunctions.logger.severe("[CORE] Disabling..."); api.unload(); return; }
				server.getPluginManager().registerEvents(new PLListener(), plugin);
				
				SharedFunctions.logger.info("[CORE] Using 'ProtocolLib' for packet-handler.");
				isPHEnabled = true;
				break;
			}
			
			case "packetevents": {
				depend = server.getPluginManager().getPlugin("packetevents");
				if (depend != null) { enable = depend.isEnabled(); }
				if (!enable) { SharedFunctions.logger.severe("[CORE] We can't found PacketEvents, seems doesn't was installed."); SharedFunctions.logger.severe("[CORE] Install PacketEvents to use this or change the packet-handler to an supported."); SharedFunctions.logger.severe("[CORE] Disabling..."); api.unload(); return; }
				server.getPluginManager().registerEvents(new PEListener(), plugin);
				
				SharedFunctions.logger.info("[CORE] Using 'PacketEvents' for packet-handler.");
				isPHEnabled = true;
				break;
			}
			
			case "disable": {
				SharedFunctions.logger.severe("[CORE] You disabled the mechanism of packet-handler!");
				//SharedFunctions.logger.severe("[CORE] Please, mantains always secure your server with");
				//SharedFunctions.logger.severe("[CORE] token-resolver feature!");
				isPHEnabled = false;
				break;
			}
			
			default:
				SharedFunctions.logger.severe("[CORE] You didn't used a valid option for packet-handler!");
				//SharedFunctions.logger.severe("[CORE] Please, mantains always secure your server with");
				//SharedFunctions.logger.severe("[CORE] token-resolver feature or put a right value!");
				isPHEnabled = false;
				break;
		}
	}
	
	public void unloader() {
	    PluginCommand command = plugin.getCommand("foxwall");
	    if (command != null) {
	        command.setExecutor(null);
	        command.setTabCompleter(null);
	        command.setAliases(null);
	        command.setDescription(null);
	    }
		FoliaAPI.cancelAllTasks(plugin);
		HandlerList.unregisterAll(plugin);
		
		try { if(metrics != null) { metrics.shutdown(); } } catch (Throwable ig) {}
		switch (packet_handler) {
			case "protocollib":
				PLListener.unregister();
				break;
			case "packetevents":
				PEListener.unregister();
				break;
			default:
				break;
		}
	}
	
	public void sendMessage(Object sender, final Component component) {
		if (!(sender instanceof Player player)) {
			final String legacy = LEGACY_SERIALIZER.serialize(component);
			((CommandSender) sender).sendMessage(legacy.isEmpty() ? " " : legacy);
			return;
		}

		if (!ADVENTURE_CHECKED) {
			try {
				Class<?> adventureAPI = Class.forName("net.kyori.adventure.text.Component");
				ADVENTURE_METHOD = Player.class.getMethod("sendMessage", adventureAPI);
			} catch (Throwable ig) {
				ADVENTURE_METHOD = null;
			}
			ADVENTURE_CHECKED = true;
		}
		if (ADVENTURE_METHOD != null) {
	       try {
	    	   ADVENTURE_METHOD.invoke(player, AdventureWrapper.toJson(component));
	    	   return;
	       } catch (Throwable ig) {}
		}
		try {
			player.spigot().sendMessage(MinecraftVersion.atLeast(V.v1_16) ? BungeeComponentSerializer.get().serialize(component) : BungeeComponentSerializer.legacy().serialize(component));
		} catch (Throwable ig2) {
			final String legacy = LEGACY_SERIALIZER.serialize(component);
			player.sendMessage(legacy.isEmpty() ? " " : legacy);
		}
	}
	
	public boolean hasPermission(Object user, String permission) {
	    if (user instanceof ConsoleCommandSender) { return true; }
	    else if (user instanceof Player) { return ((Player) user).hasPermission(permission); }
		else if (user instanceof CommandSender) { return ((CommandSender) user).hasPermission(permission); }
		else return false;
	}
	
	public void sendAllOnline(String message, String permission) {
		var players = server.getOnlinePlayers();
		if (players.isEmpty()) return;
		Component component = ColorAPI.component(message);
		for (Player receiver : players) {
			if (receiver.hasPermission(permission)) {
				this.sendMessage(receiver, component);
			}
		}
	}
	
	public Set<String> getOnlinePlayers() {
		return server.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toUnmodifiableSet());
	}
}