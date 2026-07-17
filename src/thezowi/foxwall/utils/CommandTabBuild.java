package thezowi.foxwall.utils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

public class CommandTabBuild {
    private static final List<CommandPermission> MAIN_COMMANDS = Arrays.asList(
            new CommandPermission("help", "foxwall.command.help"),
            new CommandPermission("reload", "foxwall.command.reload"),
            new CommandPermission("debug", "foxwall.command.verbose"),
            new CommandPermission("update", "foxwall.command.update")
        );
    
    private static final List<String> UPDATE_OPTIONS = Arrays.asList("plugin");

    private boolean hasPermission(Object e, String p) {
    	try {
		    switch (PlatformType.getPlatform()) {
				case BUKKIT: { return thezowi.foxwall.proxy.bukkit.Loader.INSTANCE.hasPermission(e, p); }
				default: { return false; }
		    }
    	} catch (Throwable ig) { return true; }
    }
    
    private static class CommandPermission {
        final String command;
        final String permission;
        
        CommandPermission(String command, String permission) {
            this.command = command;
            this.permission = permission;
        }
    }
    
	public void tabBuilder(Object e, String[] args, Consumer<String> suggest) {
	    if (args.length == 0) return;

	    String ca = args[args.length - 1].toLowerCase(Locale.ENGLISH);
	    Set<String> c = new LinkedHashSet<>();
	    	
	    if (args.length < 2) {
	        MAIN_COMMANDS.stream().filter(cmd -> hasPermission(e, cmd.permission)).map(cmd -> cmd.command).filter(cmd -> cmd.toLowerCase(Locale.ENGLISH).startsWith(ca)).forEach(suggest);
	        return;
	    }
	   	else if (args.length == 2 && args[0].equalsIgnoreCase("update")) {
	    	UPDATE_OPTIONS.stream().filter(o -> o.toLowerCase(Locale.ENGLISH).startsWith(ca)).forEach(suggest);
	    	return;
	    }
        if (c.isEmpty()) {
            suggest.accept("<IP>");
        } else {
            c.forEach(suggest);
        }
        return;
	}
}