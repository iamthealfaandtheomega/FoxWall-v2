package thezowi.foxwall.utils.configuration;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import thezowi.foxwall.libs.eu.okaeri.configs.OkaeriConfig;
import thezowi.foxwall.libs.eu.okaeri.configs.annotation.Comment;
import thezowi.foxwall.libs.eu.okaeri.configs.annotation.CustomKey;
import thezowi.foxwall.libs.eu.okaeri.configs.annotation.Exclude;
import thezowi.foxwall.libs.eu.okaeri.configs.annotation.Header;
import thezowi.foxwall.libs.eu.okaeri.configs.annotation.TargetType;
import thezowi.foxwall.libs.eu.okaeri.validator.annotation.NotBlank;

@Header({
	" __      _   _   _                 ",
	"/ _\\ ___| |_| |_(_)_ __   __ _ ___ ",
	"\\ \\ / _ | __| __| | '_ \\ / _` / __|",
	"_\\ |  __| |_| |_| | | | | (_| \\__ \\",
	"\\__/\\___|\\__|\\__|_|_| |_|\\__, |___/",
	"                         |___/     ",
	" ",
	"Changes aspects related to the functionality of the",
	"plugin.",
	"In this file, you can edit internal plugin functions,",
	"like changing the behavior in how the plugin can manage",
	"their functions/features.",
	" ",
	"❓ Has questions? Join to the discord server:",
	"- https://discord.zowi.gay/",
	"- https://discord.idcteam.xyz/"
})
public class Settings extends OkaeriConfig {
	@Exclude
	private transient static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-+";
	@Exclude
	private transient static final SecureRandom RANDOM = new SecureRandom();

	private static String generateToken(int length) {
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
		}
		return sb.toString();
	}

	@Comment({
		"",
	    "🌎 Plugin language setting.",
	    "- Available options below. You can help in contribute to maintain",
	    "  one translation up-to-date or publish one through our GitHub Issues",
	    "  page!",
	    " ",
	    "  • ar_sa - العربية (السعودية)",
	    "  • cz_cz - čeština (Česká republika)",
	    "  • da_dk - Dansk (Danmark)",
	    "  • de_de - Deutsch (Deutschland)",
	    "  • en_us - English (United States)",
	    "  • es_ar - Español (Argentina)",
	    "  • es_es - Español (España)",
	    "  • fr_fr - Français (France)",
	    "  • he_il - עברית (ישראל)",
	    "  • hu_hu - Magyar (Magyarország)",
	    "  • id_id - Bahasa Indonesia (Indonesia)",
	    "  • it_it - Italiano (Italy)",
	    "  • ja_jp - 日本語 (Japan)",
	    "  • ko_kr - 한국 (Korean)",
	    "  • lt_lt - Lietuvių (Lietuva)",
	    "  • nl_nl - Nederlands (Nederland)",
	    "  • pl_pl - Polski (Poland)",
	    "  • pt_br - Português (Brasil)",
	    "  • ro_ro - Română (România)",
	    "  • ru_ru - Русский (Russia)",
	    "  • sk_sk - Slovenčina (Slovensko)",
	    "  • sv_se - Svenska (Sverige)",
	    "  • th_th - ภาษาไทย (ประเทศไทย)",
	    "  • tr_tr - Türkçe (Turkey)",
	    "  • uk_ua - Українська (Україна)",
	    "  • vi_vn - Tiếng Việt (Vietnam)",
	    "  • zh_cn - 中文 (中国)",
	    " ",
	    "📚 Note: Language files are automatically downloaded from the official",
	    "GitHub repository into the 'languages' folder. This value can't be empty.",
	    "🔎 How to edit the messages? Go into the folder 'languages', and edit the",
	    "currently file in selected language: If this option it's 'en_us', edit",
	    "the file with the name 'messages_en_us.yml'."
	})
    @NotBlank
    private String language = "en_us";
	
    @Comment({
    	"",
        " _____      _                         ",
        "/__   \\___ | | _____ _ __             ",
        "  / /\\/ _ \\| |/ / _ \\ '_ \\            ",
        " / / | (_) |   <  __/ | | |           ",
        " \\/__ \\___/|_|\\_\\___|_| |_|           ",
        "  /__\\ ___ ___  ___ | |_   _____ _ __ ",
        " / \\/// _ Y __|/ _ \\| \\ \\ / / _ \\ '__|",
        "/ _  \\  __|__ \\ (_) | |\\ V /  __/ |   ",
        "\\/ \\_/\\___|___/\\___/|_| \\_/ \\___|_|   ",
        " "
    })
    @CustomKey("token-resolver")
    private TokenResolverSection token_resolver = new TokenResolverSection();
    public TokenResolverSection getTokenResolver() { return token_resolver; }
    public static class TokenResolverSection extends OkaeriConfig {
    		@Comment({
    			"",
    			"🚩 Select token-resolver. (Requires plugin restart)",
    			"[! COMING SOON]",
    			"   - Here, you need to determine the provider of the token resolver",
    			"     for FoxWall detects and handle it. The plugin will use the same",
    			"     provider of packet-handler to work with packets only.",
    			" ",
    			"   📁 Available options in type.",
    			"   ➡ BungeeGuard [from 1.8 to latest]",
    			"   ➡ Rotative (Plus Edition - Soon)",
    			"   ➡ DISABLE",
    			" ",
    			"⭐ Recommended: 'BungeeGuard'",
    			"📚 Note: This works like a extra layer for protect your server in case",
    			"the main function it's disabled or something goes wrong. Read the warning",
    			"for known more limitations of this feature.",
    			"And about 'Rotative'? It's a Plus feature, where the plugin sends through",
    			"all backends a generated Token which rotates every 10 minutes.",
//    			"⚠️ Warning! As explained above too, this function requires more requeriments",
//    			"than the main function. First, if you will chooise 'BungeeGuard', requires to",
//    			"set function 'bungeecord' to enable in 'spigot.yml' (FoxWall will tries to",
//    			"set automatically to true) and put 'BungeeGuard' in proxy; otherwise if",
//    			"you will use 'VelocityModern', disable the main function in Paper (or use it",
//   			"for better integration) and put the token here for working with packets."
    		})
    		@NotBlank
    		private String type = "BungeeGuard";
    		
    		@Comment({
    			"",
    			"📁 Allowed Tokens.",
    			"   - Here, you need to put the token from the system of token",
    			"     to join through the proxy. If the token isn't valid, the plugin",
    			"     will cancel the connection and packet, making a safe-disconnect.",
    			" ",
    			"📚 Note: If you use 'Rotative' option, you can just ignore this option.",
    			"Otherwise, get all tokens from your proxies and put here. The plugin in",
    			"first setup, will generate one for you (you can put an existent one",
    			"instead of this or use the generated one)."
    		})
    		@TargetType(ArrayList.class)
    		private volatile List<String> tokens = new ArrayList<>(Arrays.asList(generateToken(32)));
    		
    		public String getType() { return type != null ? type.toLowerCase() : "BungeeGuard"; }
    		public List<String> getTokens() { return tokens != null ? tokens : Collections.emptyList(); }
    }

    @Comment({
    	"",
        "   ___           _        _          ",
        "  / _ \\__ _  ___| | _____| |_        ",
        " / /_)/ _` |/ __| |/ / _ | __|       ",
        "/ ___| (_| | (__|   |  __| |_        ",
        "\\/    \\__,_|\\___|_|\\_\\____\\__|       ",
        "  /\\  /\\__ _ _ __   __| | | ___ _ __ ",
        " / /_/ / _` | '_ \\ / _` | |/ _ | '__|",
        "/ __  | (_| | | | | (_| | |  __| |   ",
        "\\/ /_/ \\__,_|_| |_|\\__,_|_|\\___|_|   ",
        " "
    })
    @CustomKey("packet-handler")
    private PacketHandlerSection packet_handler = new PacketHandlerSection();
    public PacketHandlerSection getPacketHandler() { return packet_handler; }
    public static class PacketHandlerSection extends OkaeriConfig {
    		@Comment({
    			"",
    			"🚩 Select packet-handler. (Requires plugin restart)",
    			"   - Here, you need to determine which type of handler for packets",
    			"     you need to FoxWall does for start working, you can specify",
    			"     which (in case you need). By default, this uses a ProtocolLib.",
    			" ",
    			"   📁 Available options in type.",
    			"   ➡ AUTO (Automatically find)",
    			"   ➡ ProtocolLib",
    			"   ➡ PacketEvents",
    			"   ➡ DISABLE (read note)",
    			" ",
    			"⭐ Recommended: 'ProtocolLib'",
    			"📚 Note: Use the value 'DISABLE' to disable this mechanism. I don't",
    			"recommend this IN ANY WAY for your security! If you insist, mantain always",
    			"the usage of token-resolver feature."
    		})
    		@NotBlank
    		private String type = "AUTO";
    		
    		@Comment({
    			"",
    			"📁 Allowed IPs.",
    			"   - Here, you need to put your proxy IP for ping and make players",
    			"     to join, otherwise, all packets will be cancelled, because requires",
    			"     original Proxy IP. To get this, put the plugin in your proxy and",
    			"     see their comments through the console.",
    			" ",
    			"📚 Note: Use only YOUR PROXY IP, otherwise, if you put a player's IP, they",
    			"can ping but can't join, because only allows PROXY IP for joining. Use",
    			"list format."
    		})
    		@TargetType(ArrayList.class)
    		private List<String> ips_allowed = new ArrayList<>(Arrays.asList("127.0.0.1"));
    		
    		public String getType() { return type != null ? type.toLowerCase() : "ProtocolLib"; }
    		public List<String> getIPs() { return ips_allowed != null ? ips_allowed : Collections.emptyList(); }
    }
    
    @Comment({
    	"",
    	"💼 Enable automatic update checking?",
    	"- Checks for new updates on our Backend during server startup.",
    	"  Requires an active internet connection to verify updates.",
    	" ",
    	"📚 Note: Since our starts in this plugin (1.0.0), FoxWall uses",
    	"its own update checking system. This implementation uses asynchronous",
    	"methods to scan for updates, save performance. You can safely enable",
    	"this to check for updates without affecting performance."
    })
	private UpdateConfig updates = new UpdateConfig();
	
    public static class UpdateConfig extends OkaeriConfig {
    	@Comment({
    		"",
    		"🚩 Enable checking of updates?",
    		"- The plugin will check for updates when the",
    		"  server starts only, this will send a message",
    		"  in the console and the link to download by",
    		"  default.",
    		"  ",
    		"⭐ Recommended: true",
    		"📚 Note: Your hosting needs to whitelist or allow",
    		"connections to 'central.zowi.gay', our backend."
    	})
    	private boolean enable = true;
    	
    	@Comment({
    		"",
    		"⌛ Automatic actions for updates."
    	})
    	private AutoConfig automatic = new AutoConfig();
        public static class AutoConfig extends OkaeriConfig {
        	@Comment({
        		"",
        		"🪄 Enable automatic updating for the .jar?",
        		"- Toggle this feature for always mantain the latest",
        		"  version. This updates through our backend and",
        		"  works for both editions: Free and Plus.",
        		" ",
        		"⭐ Recommended: true",
        		"⚠️ Warning! Always keep this feature enabled, so if an",
        		"important fix occurs, you will always keep the plugin",
        		"up-to-date."
        	})
        	private boolean updater = true;
        	@Comment({
        		"",
        		"⏳ Enable periodically checking?",
        		"- Enabling this will make the plugin check in silent",
        		"  for a new update. This makes the plugin check every",
        		"  1 hour.",
        		" ",
        		"⭐ Recommended: true",
        		"📚 Note: This feature requires to keep enabled the",
        		"option 'auto_update'."
        	})
        	private boolean periodically = true;
        	
        	public boolean getEnabled() { return updater; }
        	public boolean getPeriod() { return periodically; }
    	}
        
        public boolean getEnable() { return enable; }
        public AutoConfig getAutomatically() { return automatic; }
    }
	
	@Comment({
		"",
	    "📈 Allow the usage of Metrics (bStats) in your server?",
	    "- This is for statistical purposes only.",
	    " ",
	    "  ❝ What data is collected?",
	    "  We use bStats (https://bstats.org/), which does not collect",
	    "  any personal data. Most of the collected data consists of information about",
	    "  the server, such as player count, online mode, Minecraft version,",
	    "  Java version, and more. All data is sent and stored completely",
	    "  anonymously. For more information about this, see \"bStats: Getting Started\"",
	    "  at https://bstats.org/getting-started",
	    " ",
	    "📚 Note: Since our starts in this plugin (1.0.0), FoxWall uses",
	    "its own code base for Metrics, while still following bStats",
	    "guidelines. This implementation uses asynchronous methods to send data and",
	    "replaces Gson for better stability, saving overall performance.",
	    "You can safely enable this to help the creator without affecting performance.",
	    "⚠️ Warning! The plugin has a separated file for using Metrics (from bStats),",
	    "check the file 'bStats.yml' (in the same folder of FoxWall) to configure it."
	})
    private boolean metrics = true;
    
    public String getLanguage() { return language != null ? language.toLowerCase() : "en_us"; }
    public UpdateConfig getUpdater() { return updates; }
    public boolean getMetrics() { return metrics; }
}