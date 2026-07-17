package thezowi.foxwall.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import thezowi.foxwall.libs.eu.okaeri.configs.ConfigManager;
import thezowi.foxwall.libs.eu.okaeri.configs.OkaeriConfig;
import thezowi.foxwall.libs.eu.okaeri.configs.migrate.ConfigMigration;
import thezowi.foxwall.libs.eu.okaeri.configs.serdes.commons.SerdesCommons;
import thezowi.foxwall.libs.eu.okaeri.configs.validator.okaeri.OkaeriValidator;
import thezowi.foxwall.libs.eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import thezowi.foxwall.libs.net.kyori.adventure.text.Component;
import thezowi.foxwall.proxy.bukkit.PlaceholderAPI;
import thezowi.foxwall.utils.configuration.LangConfig;
import thezowi.foxwall.utils.configuration.Settings;

public class FilesManager {
	protected static String PREFIX_STR = null;
	protected static Component PREFIX_COMP = null;
	
    protected final Path path = SharedFunctions.path.resolve("FoxWall");
    protected final Path langs = this.path.resolve("languages");
    
    private Settings config = null;
    private LangConfig lang = null;
    
    public static List<String> ALLOWED_PROXY_IP = null;
    public static List<String> ALLOWED_TOKENS = null;
    
    public static String LANG_KICK_NULL;
    public static String LANG_KICK_SAMEBACKEND;
    
    public FilesManager() {}
    
    public static FilesManager initializeAndWait() throws Throwable {
    	FilesManager manager = new FilesManager();
        manager.setup().join();
        return manager;
    }

    protected CompletableFuture<Void> setup() throws Throwable {
    	SharedFunctions.logger.info("[FILES] Loading configuration files...");
    	if (Files.notExists(this.path)) { Files.createDirectories(this.path); }
	    
	    this.config = this.loadConfig(Settings.class, this.path.resolve("config.yml"), "config.yml");
	    
	    if (ALLOWED_PROXY_IP != null) { ALLOWED_PROXY_IP.clear(); }
	    ALLOWED_PROXY_IP = this.getSettings().getPacketHandler().getIPs();
	    if (ALLOWED_TOKENS != null) { ALLOWED_TOKENS.clear(); }
	    ALLOWED_TOKENS = this.getSettings().getPacketHandler().getIPs();
	    
	    /* Language */
        String language = this.getSettings().getLanguage().toLowerCase();
        Path languageFile = this.langs.resolve("messages_"+language+".yml");
        if (!Files.exists(languageFile)) {
        	try {
        	    CompletableFuture<Void> languageFuture = this.downloadLang(language, languageFile);
        	    if (languageFuture != null) languageFuture.join();
        	} catch (Throwable ig) {
        		language = "en_us";
        		languageFile = this.langs.resolve("messages_"+language+".yml");
        	}
        }
        SharedFunctions.logger.info("[FILES] Using language: "+language);
        try {
            this.lang = this.loadConfig(LangConfig.class, languageFile, "messages_"+language+".yml");
        } catch (Throwable ig) {
            SharedFunctions.logger.severe("[FILES] Failed to load '"+language+"': "+ig.getMessage());
            ig.printStackTrace();
        }
        
        LANG_KICK_NULL = ColorAPI.toLegacy(this.getLanguage().message.kick.null_ip);
        LANG_KICK_SAMEBACKEND = ColorAPI.toLegacy(this.getLanguage().message.kick.samebackend_ip);
        
    	SharedFunctions.logger.info("[FILES] Loaded all configuration files!");
		return CompletableFuture.completedFuture(null);
    }
   
    public Settings getSettings() { return this.config; }
    public LangConfig getLanguage() { return this.lang; }
    
    public String get(String message, Object user) {
        if (PREFIX_STR == null) {
            PREFIX_STR  = (this.getLanguage() != null) ? this.getLanguage().message.prefix : "<green><bold>FW</bold></green><dark_gray> ►<reset>";
            PREFIX_COMP = ColorAPI.component(PREFIX_STR);
        }
        
        if (PlatformType.getPlatform() == PlatformType.T.BUKKIT && user != null) { message = PlaceholderAPI.apply(user, message); }
        return message.replace("\\n", "\n").replace("%nl%", "\n").replace("\\nl", "\n").replace("{prefix}", PREFIX_STR);
    }

    public Component getComp(String message, Object user) {
        if (PREFIX_STR == null) {
            PREFIX_STR  = (this.getLanguage() != null) ? this.getLanguage().message.prefix : "<green><bold>FW</bold></green><dark_gray> ►<reset>";
            PREFIX_COMP = ColorAPI.component(PREFIX_STR);
        }
        
        if (PlatformType.getPlatform() == PlatformType.T.BUKKIT && user != null) { message = PlaceholderAPI.apply(user, message); }
        message = message.replace("\\n", "\n").replace("%nl%", "\n").replace("\\nl", "\n").replace("{prefix}", PREFIX_STR);
        return ColorAPI.component(message);
    }

    public Component getComp(String message) {
        if (PREFIX_STR == null) {
            PREFIX_STR  = (this.getLanguage() != null) ? this.getLanguage().message.prefix : "<green><bold>FW</bold></green><dark_gray> ►<reset>";
            PREFIX_COMP = ColorAPI.component(PREFIX_STR);
        }
        
        message = message.replace("\\n", "\n").replace("%nl%", "\n").replace("\\nl", "\n").replace("{prefix}", PREFIX_STR);
        return ColorAPI.component(message);
    }
    
    public <T extends OkaeriConfig> T loadConfig(Class<T> type, Path file, String name, ConfigMigration... migrations) {
    	try {
        	return ConfigManager.create(type, setup -> {
            	setup.configure(config -> {
                	config.configurer(new YamlSnakeYamlConfigurer(), new SerdesCommons());
                    config.validator(new OkaeriValidator());
                    config.bindFile(file);
                    config.logger(SharedFunctions.logger);
                    config.errorComments(true);
                    config.removeOrphans(false);
                });
                setup.saveDefaults();
                if (Files.exists(file)) {
                    if (migrations != null && migrations.length > 0) {
                    	setup.migrate(migrations);
                    }
                	setup.load(true);
                } else {
                	setup.save();
                }
            });
        } catch (Throwable ig) {
        	SharedFunctions.logger.severe("[FILES] Failed to load '"+name+"': "+ig.getMessage());
            ig.printStackTrace();
            return null;
        }
    }
    
    private CompletableFuture<Void> downloadLang(String language, Path target) {
    	try {
    		HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).version(HttpClient.Version.HTTP_1_1).followRedirects(HttpClient.Redirect.NORMAL).build();
    	    HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.github.com/repos/IDCTeam-Group/FoxWall-Issues/contents/languages/"+language+".yml")).timeout(Duration.ofSeconds(5)).header("Accept", "application/vnd.github.raw+json").GET().build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
            		.thenAccept(response -> {
            			int status = response.statusCode();
            			if (status == 200) {
            				try (var body = response.body()) {
            					long size = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
            					DownloadUtils.copyWithProgress(body, target, size, "LANG");
            				} catch (Throwable e) {
            					SharedFunctions.logger.severe("[LANG] Failed to save language file: "+e.getMessage());
            				}
                        } else if (status == 404) {
                            SharedFunctions.logger.warning("[LANG] Language file not found on GitHub: "+language);
                        } else if (status == 403) {
                            String remaining = response.headers().firstValue("X-RateLimit-Remaining").orElse("?");
                            String reset = response.headers().firstValue("X-RateLimit-Reset").orElse("?");
                            SharedFunctions.logger.warning("[LANG] GitHub rate limit reached. Remaining: "+remaining+", resets at: "+reset);
                        } else {
                            SharedFunctions.logger.warning("[LANG] Could not download language file ("+language+"): HTTP "+status);
                        }
            		})
            		.exceptionally(ex -> {
            			SharedFunctions.logger.warning("[FILES] Error while downloading language file: "+ex.getMessage());
            			return null;
            		});
    	} catch (Throwable e) {
    		SharedFunctions.logger.warning("[FILES] Error while downloading language file: "+e.getMessage());
    	}
		return null;
    }

    public void reload() throws Throwable {
    	this.setup();
    	return;
    }
}