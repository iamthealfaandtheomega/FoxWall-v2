package thezowi.foxwall.proxy.bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import thezowi.foxwall.utils.CommandBuild;
import thezowi.foxwall.utils.CommandTabBuild;

public class CommandManager implements CommandExecutor, TabCompleter {
  	private final CommandBuild commandBuilder = new CommandBuild();
  	private final CommandTabBuild tabBuilder = new CommandTabBuild();
  
  	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	 	CompletableFuture.runAsync(() -> {
	 		this.commandBuilder.commandBuilder(sender, args);
	 		return;
	 	});
	 	return true;
  	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
	    List<String> list = new ArrayList<>();
	    
	    this.tabBuilder.tabBuilder(sender, args, list::add);
	    return list;
	}
}