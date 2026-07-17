package thezowi.foxwall.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import thezowi.foxwall.libs.com.google.gson.JsonObject;
import thezowi.foxwall.libs.com.google.gson.JsonParser;

public class SharedFunctions {
	public static Logger logger;
	public static String ver = "N/A";
	public static String plat_ver = "N/A";
	public static FilesManager file = null;
	public static Path path = null;
	public static Object classloader = null;
	public static boolean DEBUG = false;
  
	public static void header() {
		String banner = """

⠀⠀⠀⠀⠀⠀⢰⠢⣄⠀⠀⠀⠀⠀⠀⠀⠀⣀⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠈⡄⢀⠙⢦⣀⠀⠀⠀⢀⢎⢀⠦⢹⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀FoxWall
⠀⠀⠀⠀⠀⠀⠀⢡⠨⣄⠈⠿⣕⣄⢀⡾⠊⠨⣸⡀⢧⠀⠀⠀⠀⠀⠀⠀⠀⠀"A better alternative to BungeeGuard and Velocity Modern."
⠀⠀⠀⠀⠀⠀⠀⠈⢞⠻⡗⢆⢷⢇⠛⢀⡀⢴⣶⡇⣸⣦⠀⠀⠀⠀⠀⠀⠀⠀                
⠀⠀⠀⠀⠀⠀⠀⠀⠸⠢⠕⠈⠀⠈⠀⠞⢱⠿⠛⢠⢿⡙⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⢐⠆⠀⠀⠀⠀⠀⠀⠁⣄⣄⡼⢎⡀⢣⠀⠀⠀⠀⠀⠀⠀Free Edition (%s)
⠀⠀⠀⠀⠀⠀⠀⠀⠙⢢⠀⠀⢰⣧⣤⠠⣆⡀⠫⡱⡢⡀⢹⠀⠀⠀⠀⠀⠀⠀Give your review and use our discord server for support!
⠀⠀⠀⠀⠀⠀⠀⠀⠀⡟⠀⡤⡏⠈⠀⠐⠊⠙⠢⡸⣞⡌⠘⡀⠀⠀⠀⠀⠀⠀~ Zowi love's you   ႔ ႔
⠀⠀⠀⠀⠀⠀⠀⢀⠎⠀⠀⣴⡷⡤⢂⠁⠀⠀⢠⠈⠟⡞⢆⡇⠀⠀⠀⠀⠀⠀                  ᠸ^ ^ ⸝⸝ 
⠀⠀⠀⠀⠀⠀⠀⢿⣿⠞⠉⢙⣥⠮⠍⠒⠒⠠⠼⡀⠀⠇⡼⠀⠀⠀⠀⠀⠀⠀
							

	""".formatted(ver);
	    logCenteredBlock(banner);
	}
  
	private static void logCenteredBlock(String block) {
		String[] lines = block.split("\n");
		int consoleWidth = 80;
		int prefixWidth = 5; // "[FW] "
		int usableWidth = consoleWidth - prefixWidth;
		int maxLineLength = 0;
		for (String line : lines) {
			int len = line.length();
			if (len > maxLineLength) maxLineLength = len;
		}
		int leftPadding = (usableWidth - maxLineLength) / 2;
		if (leftPadding < 0) leftPadding = 0;
		String padding = " ".repeat(leftPadding);
		for (String line : lines) {
			logger.info(padding + line);
		}
		logger.info("");
	}
	
	public static void getProxyIP() {
		String ip = null;
		try {
			ip = getAddress().get(6, TimeUnit.SECONDS).toString();
		} catch (Throwable ig) {
			 SharedFunctions.logger.severe("[IP] Failed to get IP! Try to contact to your hosting");
			 SharedFunctions.logger.severe("[IP] for receive original IP as alternative!");
			 SharedFunctions.logger.severe("[IP] Tried to target:");
			 SharedFunctions.logger.severe("[IP] https://zowi.gay/ip");
			 SharedFunctions.logger.severe("[IP] https://api.ipify.org/");
			 return;
		}
		if (ip.equalsIgnoreCase("Failed!") || ip == null) { 
			 SharedFunctions.logger.severe("[IP] Failed to get IP! Try to contact to your hosting");
			 SharedFunctions.logger.severe("[IP] for receive original IP as alternative!");
			 SharedFunctions.logger.severe("[IP] Tried to target:");
			 SharedFunctions.logger.severe("[IP] https://central.zowi.gay/ip");
			 SharedFunctions.logger.severe("[IP] https://api.ipify.org/");
			 return;
		}
        SharedFunctions.logger.info("[IP] ");
        SharedFunctions.logger.info("[IP] !!! Here your proxy IP for allowed IPs !!!");
        SharedFunctions.logger.info("[IP] ");
        SharedFunctions.logger.info("[IP] FoxWall detected the next IP, put this in your");
        SharedFunctions.logger.info("[IP] backend with FoxWall for allow proxy makes connections.");
        SharedFunctions.logger.info("[IP] ");
        SharedFunctions.logger.info("[IP] IP: "+ip);
        SharedFunctions.logger.info("[IP] ");
        SharedFunctions.logger.info("[IP] Where I need to put?");
        SharedFunctions.logger.info("[IP] 1. Go to your backend and '/plugins/FoxWall/config.yml' file.");
        SharedFunctions.logger.info("[IP] 2. Check 'packet-handler.ips_allowed' and put the IP in that list.");
        SharedFunctions.logger.info("[IP] 3. Remove the plugin from Proxy and restart backend.");
        SharedFunctions.logger.info("[IP] ");
        SharedFunctions.logger.info("[IP] Join to our discord server for support and leave your");
        SharedFunctions.logger.info("[IP] review for help this plugin! ^^");
        SharedFunctions.logger.info("[IP] - https://discord.zowi.gay/");
        SharedFunctions.logger.info("[IP] - https://discord.idcteam.xyz/");
        SharedFunctions.logger.info("[IP] ");
	}
  
	private static CompletableFuture<String> getAddress() throws Throwable {
		return CompletableFuture.supplyAsync(() -> {
			final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).followRedirects(HttpClient.Redirect.NEVER).connectTimeout(Duration.ofMillis(3000)).build();
			try {
			    HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://central.zowi.gay/ip")).GET().timeout(Duration.ofMillis(3000)).build();
			    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			    return response.body();
			    
			} catch (Throwable ig) {
				try {
					HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.ipify.org/")).GET().timeout(Duration.ofMillis(3000)).build();
				    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				    return response.body();		
				} catch (Throwable ig2) { return "Failed!"; }
			}
		});
	}
	
	public static void checkForUpdates(final boolean periodically) {
		if (!file.getSettings().getUpdater().getEnable()) { return; }
		logger.info(() -> "[UPDATER] Scanning for new versions...");
		
		FoxWallAPI tmp_api = FoxWallAPI.INSTANCE;
		final String type = tmp_api.fw_api_type;
       	final String api_version = tmp_api.fw_api_v;
       	final String key = tmp_api.fw_api_key;
      	final String version = tmp_api.getVersion();
      	
      	HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).connectTimeout(Duration.ofSeconds(3)).followRedirects(HttpClient.Redirect.NEVER).build();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://central.zowi.gay/private/update")).timeout(Duration.ofSeconds(5)).header("User-Agent", "FoxWall ("+type+") - (API"+api_version+") (v"+version+")").header("HWID", AntiMalware.getHWID()).header("Authorization", "Bearer "+key).header("Action", "CHECK").header("Version", version).GET().build();
		
		CompletableFuture<HttpResponse<String>> future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
		future.thenAccept(value -> {
			if (value.statusCode() == 200) {
		        String body = value.body().trim();
		        final String latest;
		        if (body.startsWith("{")) {
		        	String parsed;
		        	try {
		        		JsonObject json = JsonParser.parseString(body).getAsJsonObject();
		        		parsed = json.get("latest").getAsString();
		        	} catch (Exception e) {
		        		parsed = body;
		        	}
		        	latest = parsed;
		        } else {
		        	latest = body;
		        }
		        
		        String current = ver.replace("-pv", "");
		        if (!latest.equals(current)) {
					if (!periodically) {
						logger.info(() -> "[UPDATER] A new version of FoxWall has been found.");
						logger.info(() -> "[UPDATER] Your version is: "+current);
						logger.info(() -> "[UPDATER] Latest version is: "+latest);
					}
					if (file.getSettings().getUpdater().getAutomatically().getEnabled()) { try { AutoUpdater.update(false); } catch (Throwable e) {} }
				} else {
					if (!periodically) { logger.info(() -> "[UPDATER] You are using the latest version of FoxWall."); }
				} 
			} else {
				if (!periodically) { logger.warning(() -> "[UPDATER] Failed to check for updates! HTTP status code: "+value.statusCode()); }
			} 
		}).exceptionally(exception -> {
			if (!periodically) { logger.warning(() -> "[UPDATER] An error occurred while checking for updates. Probably your provider it's blocking our Central (central.zowi.gay)"); }
			return null;
        });
	}
}
