package thezowi.foxwall.utils;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AutoUpdater {
	private static FoxWallAPI api = FoxWallAPI.INSTANCE;
	
	public static CompletableFuture<Void> update(boolean throwabling) {
		return CompletableFuture.runAsync(() -> {
			
			try {	
				String key = api.fw_api_key;
				String type = api.fw_api_type;
	        	
		       	String api_version = api.fw_api_v;
		      	String version = api.getVersion();
		      	
		      	HttpRequest request = HttpRequest.newBuilder()
		      			.uri(URI.create("https://central.zowi.gay/private/update"))
		      			.timeout(Duration.ofSeconds(10))
		      			.header("User-Agent", "FoxWall ("+type+") - (API"+api_version+") (v"+version+")")
		      			.header("Authorization", "Bearer "+key)
		      			.header("API", api_version)
		      			.header("HWID", AntiMalware.getHWID())
		      			.header("Action", "DOWNLOAD")
		      			.header("Version", version)
		      			.GET()
		      			.build();
		      	HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).followRedirects(HttpClient.Redirect.NORMAL).priority(1).connectTimeout(Duration.ofSeconds(5)).build();
		        HttpResponse<InputStream> result = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		        
		        int status = result.statusCode();
		        switch (status) {
		        	case 512: {
		        		throw new Exception("521");
		        	}
		        	case 503: {
		        		throw new Exception("503");
		        	}
		        	case 418: {
		        		throw new Exception("418");
		        	}
		        }
		        
		        SharedFunctions.logger.info("[UPDATER] Updating the plugin using auto-updater...");
		        try (InputStream in = result.body()) {
		        	
		            long size = result.headers().firstValueAsLong("X-File-Size").orElse(result.headers().firstValueAsLong("Content-Length").orElse(-1L));
		            SharedFunctions.logger.info("[UPDATER] Downloading update...");
		            DownloadUtils.copyWithProgress(in, FoxWallAPI.INSTANCE.get().toPath(), size, "UPDATING");
		            SharedFunctions.logger.info("[UPDATER] Update finished, restart your server to apply changes!");
		        }
		        
			} catch (Throwable e) {
				if (throwabling) { throw new RuntimeException(e); }
				else {
					SharedFunctions.logger.warning("[UPDATER] Failed to automatically updating the plugin: "+e.getMessage());
					SharedFunctions.logger.warning("[UPDATER] Manual updating it's required.");
				}
				return;
			}
			
		})
		.orTimeout(30, TimeUnit.SECONDS)
		.exceptionally(ex -> {
			SharedFunctions.logger.warning("[UPDATER] ");
			SharedFunctions.logger.warning("[UPDATER] The server takes 30 seconds in finish update,");
			SharedFunctions.logger.warning("[UPDATER] probably a connectivity issue in your hosting and");
			SharedFunctions.logger.warning("[UPDATER] our backend. We cancelled the operation, please,");
			SharedFunctions.logger.warning("[UPDATER] talk to your hosting about this issue.");
			SharedFunctions.logger.warning("[UPDATER] ");
			SharedFunctions.logger.warning("[UPDATER] Plugin tries to request: central.zowi.gay");
			SharedFunctions.logger.warning("[UPDATER] ");
			return null;
		});
	}
}