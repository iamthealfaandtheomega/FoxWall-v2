package thezowi.foxwall.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import thezowi.foxwall.libs.net.kyori.adventure.text.Component;
import thezowi.foxwall.libs.net.kyori.adventure.text.event.ClickEvent;
import thezowi.foxwall.libs.net.kyori.adventure.text.event.HoverEvent;

public class CommandBuild {
	private final FoxWallAPI api = FoxWallAPI.INSTANCE;
  	private static final String version = SharedFunctions.ver;
  	private static final AtomicBoolean fileDownloading = new AtomicBoolean(false);
  	private static final AtomicBoolean reloading = new AtomicBoolean(false);
  	private static final FilesManager file = SharedFunctions.file;
  	private static List<cmdEntries> CMDS;
  	
  	private static final Component HELP_HEADER_EMPTY = ColorAPI.component("");
  	private static final Component HELP_HEADER_1 = ColorAPI.component("     <green><bold>FoxWall</bold></green> by <yellow><bold>NovaCraft254</bold></yellow>");
  	private static final Component HELP_HEADER_2 = ColorAPI.component("     <gray>Protect your backend with a great way - Free Edition</gray>");
  	private static final Component HELP_HEADER_VERSION = ColorAPI.component("                <dark_gray>[v"+version+"]</dark_gray>\"");

  	private static Component LANG_HELP_DEBUG;
  	private static Component LANG_HELP_RELOAD;
  	private static Component LANG_HELP_UPDATE;
  	
  	private static Component LANG_DEBUG_ENABLE;
  	private static Component LANG_DEBUG_DISABLE;
  	
  	private static Component LANG_RELOAD_MESSAGE;
  	
  	private static Component LANG_UPDATE_INVALIDUSAGE;
  	private static Component LANG_UPDATE_ALREADY;
  	private static Component LANG_UPDATE_BACKENDDOWN;
  	private static Component LANG_UPDATE_LICENSE;
  	private static Component LANG_UPDATE_DJAR;
  	private static Component LANG_UPDATE_DLJAR;
  	
  	public CommandBuild() { loadCache(); }
  	
  	record cmdEntries(String syntax, Supplier<Component> hover, String permission) {}
  	
  	public static void loadCache() {
  		LANG_HELP_DEBUG = file.getComp(file.getLanguage().message.help.debug);
  		LANG_HELP_RELOAD = file.getComp(file.getLanguage().message.help.reload);
  		LANG_HELP_UPDATE = file.getComp(file.getLanguage().message.help.update);
  		
  		LANG_DEBUG_ENABLE = file.getComp(file.getLanguage().message.debug.enable);
  		LANG_DEBUG_DISABLE = file.getComp(file.getLanguage().message.debug.disable);
  		
  		LANG_RELOAD_MESSAGE = file.getComp(file.getLanguage().message.reload.message);
  		
  		LANG_UPDATE_INVALIDUSAGE = file.getComp(file.getLanguage().message.update.invalid_usage);
  		LANG_UPDATE_ALREADY = file.getComp(file.getLanguage().message.update.already_downloading);
  		LANG_UPDATE_BACKENDDOWN = file.getComp(file.getLanguage().message.update.backend_down);
  		LANG_UPDATE_LICENSE = file.getComp(file.getLanguage().message.update.invalid_license);
  		LANG_UPDATE_DJAR = file.getComp(file.getLanguage().message.update.downloading_jar);
  		LANG_UPDATE_DLJAR = file.getComp(file.getLanguage().message.update.downloaded_jar);
  		
		if (CMDS != null) { CMDS.clear(); }
		CMDS = new ArrayList<>(Arrays.asList(
			new cmdEntries("<gray>/foxwall</gray> <white>debug", () -> LANG_HELP_DEBUG, "foxwall.command.debug"),
			new cmdEntries("<gray>/foxwall</gray> <white>reload", () -> LANG_HELP_RELOAD, "foxwall.command.reload"),
			new cmdEntries("<gray>/foxwall</gray> <white>update <green><plugin></green>", ()-> LANG_HELP_UPDATE, "foxwall.command.update")
		));
  	}
  	
  	public CompletableFuture<Void> commandBuilder(Object sender, String[] args) {
  		return CompletableFuture.runAsync(() -> {
	  		int length = args.length;
	  		if (length < 1 || args[0].equalsIgnoreCase("help")) {
	  			sendHelp(sender);
	  			return;
	  		}
	  		
	  		/*
	  		 * Reload command.
	  		 */
	  		if (args[0].equalsIgnoreCase("reload") && this.hasPermission(sender, "foxwall.command.reload")) {
	  			
	  			if (!reloading.compareAndSet(false, true)) { return; }
	  			
	  			try { file.reload(); } catch (Throwable ex) { ex.printStackTrace(); }
	  			try { loadCache(); } catch (Throwable ex) { ex.printStackTrace(); }
	  			final String setting_ph = api.getFiles().getSettings().getPacketHandler().getType(); //, setting_tr = api.getFiles().getSettings().getTokenResolver().getType();
	  			//try { thezowi.foxwall.proxy.bukkit.Loader.loadTokenHandler(setting_tr); } catch (Throwable ig) { ig.printStackTrace(); }
	  			try { thezowi.foxwall.proxy.bukkit.Loader.loadPacketHandler(setting_ph); } catch (Throwable ig) { ig.printStackTrace(); }
	  			
	  			reloading.set(false);
	  			this.api.sendMessage(sender, LANG_RELOAD_MESSAGE);
	  			return;
	  		
	  		/*
	  		 * Debug command.
	  		 */
	  	    } else if (args[0].equalsIgnoreCase("debug") && this.hasPermission(sender, "foxwall.command.debug")) {
	  			if (!SharedFunctions.DEBUG) {
	  				SharedFunctions.DEBUG = true;
	  				this.api.sendMessage(sender, LANG_DEBUG_ENABLE);
	  			} else {
	  				SharedFunctions.DEBUG = false;
	  				this.api.sendMessage(sender, LANG_DEBUG_DISABLE);
	  			}
	  			return;
	  			
		    /*
		    * Update Command.
		    */
	        } else if (args[0].equalsIgnoreCase("update") && this.hasPermission(sender, "foxwall.command.update")) {
		    	if (length < 2) {
		    		this.api.sendMessage(sender, LANG_UPDATE_INVALIDUSAGE);
		    		return;
		    	}
		    	if (!fileDownloading.compareAndSet(false, true)) {
		    		this.api.sendMessage(sender, LANG_UPDATE_ALREADY);
		    		return;
		    	}
		    	
		    	if (args[1].equalsIgnoreCase("plugin")) {
		    	    this.api.sendMessage(sender, LANG_UPDATE_DJAR);
		    	    
		    	    AutoUpdater.update(true).whenComplete((response, exception) -> {
		    	        try {
		    	            if (exception != null) {
		    	                String parse = exception.getMessage();
		    	                if (exception.getCause() != null) { parse = exception.getCause().getMessage(); }
		    	                
		    	                if (parse.equals("503")) {
		    	                	this.api.sendMessage(sender, LANG_UPDATE_LICENSE);
		    	                } else {
		    	                	this.api.sendMessage(sender, LANG_UPDATE_BACKENDDOWN);
		    	                }
		    	            } else {
		    	            	this.api.sendMessage(sender, LANG_UPDATE_DLJAR);
		    	            }
		    	        } finally {
		    	        	fileDownloading.set(false);
		    	        }
		    	    });
		    	    return;
		    	    
		    	} else {
		    		fileDownloading.set(false);
		    		this.api.sendMessage(sender, LANG_UPDATE_INVALIDUSAGE);
		    		return;
		    	}
		    	
	        } else {
	        	this.sendHelp(sender);
	        	return;
	        }
  		});
  	}
  	
    private boolean hasPermission(Object user, String permission) {
    	try {
		    switch (PlatformType.getPlatform()) {
				case BUKKIT: {
					return thezowi.foxwall.proxy.bukkit.Loader.INSTANCE.hasPermission(user, permission);
				}
				default: {
					return false;
				}
		    }
    	} catch (Throwable ig) { return true; }
    }
      
    public void sendHelp(Object sender) {
    	CompletableFuture.runAsync(() -> {
	    	this.api.sendMessage(sender, HELP_HEADER_EMPTY);
	        this.api.sendMessage(sender, HELP_HEADER_1);
	        this.api.sendMessage(sender, HELP_HEADER_2);
	        this.api.sendMessage(sender, HELP_HEADER_EMPTY);

	        boolean show_version = false;
	        for (cmdEntries cmd : CMDS) {
	            if (this.hasPermission(sender, cmd.permission())) {
		            String commandList = cmd.syntax;
	                String commandBuild = commandList.split(" ")[0]+" "+commandList.split(" ")[1];
	                Component message = ColorAPI.component(" <dark_gray>▪ "+commandList, HoverEvent.showText(cmd.hover().get()), ClickEvent.suggestCommand(ColorAPI.stripColor(commandBuild)));
	               
	                this.api.sendMessage(sender, message);
	                show_version = true;
	            }
	        }
	        
	        if (show_version) {
	        	this.api.sendMessage(sender, HELP_HEADER_EMPTY);
	        	this.api.sendMessage(sender, HELP_HEADER_VERSION);
	        }
	        this.api.sendMessage(sender, HELP_HEADER_EMPTY);
    	});
    }
}