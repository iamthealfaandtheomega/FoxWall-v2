package thezowi.foxwall.utils.configuration;

import thezowi.foxwall.libs.eu.okaeri.configs.OkaeriConfig;
import thezowi.foxwall.libs.eu.okaeri.configs.annotation.Comment;
import thezowi.foxwall.libs.eu.okaeri.configs.annotation.Header;

@Header({
	"                                            ",
	"  /\\/\\   ___  ___ ___  __ _  __ _  ___  ___ ",
	" /    \\ / _ \\/ __/ __|/ _` |/ _` |/ _ \\/ __|",
	"/ /\\/\\ \\  __/\\__ \\__ \\ (_| | (_| |  __/\\__ \\",
	"\\/    \\/\\___||___/___/\\__,_|\\__, |\\___||___/",
	"                            |___/           ",
    " ",
    "🔨 Configure the plugins messages.",
    "You can use MiniMessage in this section. This also works with",
    "legacy bukkit colors, but isn't recommended, because of",
    "an incompatibility!",
    "Also, PlaceholderAPI is supported.",
    " ",
    "📷 Viewer:",
    "https://webui.advntr.dev/",
    " ",
    "📚 Format:",
    "https://docs.papermc.io/adventure/minimessage/format/"
})
public class LangConfig extends OkaeriConfig {
    public MessageSection message = new MessageSection();
    public static class MessageSection extends OkaeriConfig {
        @Comment("🪄 Determine the message prefix for the plugin.")
        public String prefix = "<green><bold>FW</bold></green><dark_gray> ►<reset>";
        public HelpSection help = new HelpSection();
        public static class HelpSection extends OkaeriConfig {
            public String debug = "Debug FoxWall in the console.";
            public String reload = "Reload configuration file.";
            public String update = "Automatically updates plugin components for you.";
        }

        public DebugSection debug = new DebugSection();
        public static class DebugSection extends OkaeriConfig {
            public String enable = "{prefix} <green>Enabled debug mode. Check the console.";
            public String disable = "{prefix} <red>Disabled debug mode.";
        }
        
        public KickSection kick = new KickSection();
        public static class KickSection extends OkaeriConfig {
            public String null_ip = "<red>Invalid connection.</red>";
            public String samebackend_ip = "<red>Connection denied in your IP address.</red>";
        }
        
        public ReloadSection reload = new ReloadSection();
        public static class ReloadSection extends OkaeriConfig {
            public String message = "{prefix} <green>The configuration file has been reloaded.";
        }
        
        public UpdateSection update = new UpdateSection();
        public static class UpdateSection extends OkaeriConfig {
        	public String invalid_usage = "{prefix} Use the command <dark_gray>'</dark_gray>/fw update <green><plugin></green><dark_gray>'</dark_gray><reset>.";
            public String already_downloading = "{prefix} <red>We are already downloading a file...";
            public String backend_down = "{prefix} <red>Our backend seems to be down or unstable. We can't update and complete the request.";
            public String invalid_license = "{prefix} <red>Your license isn't valid, put a valid key for this action.";
    
            public String downloading_jar = "{prefix} <yellow>Downloading plugin...";
            public String downloaded_jar = "{prefix} <green>Downloaded latest JAR from our backend/central. Restart your server for apply changes.";
        }
    }
}
