package thezowi.foxwall.proxy.bukkit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import thezowi.foxwall.libs.com.google.gson.JsonArray;
import thezowi.foxwall.libs.com.google.gson.JsonObject;
import thezowi.foxwall.libs.com.google.gson.JsonPrimitive;
import thezowi.foxwall.libs.eu.okaeri.configs.ConfigManager;
import thezowi.foxwall.libs.eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import thezowi.foxwall.utils.FilesManager;
import thezowi.foxwall.utils.SharedFunctions;
import thezowi.foxwall.utils.configuration.bStatsConfig;

/*
 * Hello, I'm Zowi!
 * (15/03/2025)
 * 
 * Here is my little fork of Metrics to improve performance and use asynchronous. Here
 * you can use this Metrics class if you want, but, this use Gson library for JSON instead
 * that bStats created; this is for more stability.
 * 
 * This code tries to follow some rules of bStats, with the only objective of improve
 * performance. (This doesn't modify the "Frequency for data submission", doesn't remove the 
 * "option for users to opt-out" and the plugin isn't obfuscated, this only follow an objective)
 */
public class Metrics {
	private final Plugin plugin;
	private final MetricsBase metricsBase;
	private final static FilesManager file = SharedFunctions.file;
	private bStatsConfig config = null;

	public Metrics(Plugin plugin, int serviceId) {
		this.plugin = plugin;
		Path bStatsFolder = SharedFunctions.path.resolve("FoxWall");
		Path cF = bStatsFolder.resolve("bStats.yml");
		if (Files.notExists(bStatsFolder)) { try { Files.createDirectories(bStatsFolder); } catch (IOException ignored) { } }
		if (Files.notExists(cF)) { try { Files.createFile(cF); } catch (IOException ignored) { } }
		/**
		 * This is a class for configuration file, but, why?
		 * The reason, FoxGate uses their own code for running this, and
		 * for make this compatible, I need to use OkaeriConfigs, which
		 * offers stability in configuration. This will add the missing
		 * options in case it's necessary on older versions of the
		 * bStats configuration.
		 */

	    this.config = ConfigManager.create(bStatsConfig.class, (it) -> {
            it.configure(f -> {
		    	f.configurer(new YamlSnakeYamlConfigurer());
		    	f.bindFile(cF);
		    	f.removeOrphans(false);
		    	f.logger(SharedFunctions.logger);
            });
	    	it.saveDefaults();
	    	if (Files.exists(cF)) { it.load(true); } else { it.save(); }
	    });
		boolean enabled = file.getSettings().getMetrics();
		String serverUUID = config.getServerUUID();
		boolean logErrors = config.getLogFailedRequests();
		boolean logSentData = config.getLogSentData();
		boolean logResponseStatusText = config.getLogResponseStatusText();
		boolean isFolia = false;
		try { isFolia = FoliaAPI.isFolia(); } catch (Exception e) {}
		metricsBase = new MetricsBase("bukkit", serverUUID, serviceId, enabled, this::appendPlatformData, this::appendServiceData, isFolia ? null : submitDataTask -> FoliaAPI.runTaskAsync(plugin, submitDataTask), plugin::isEnabled, (message, error) -> this.plugin.getLogger().log(Level.WARNING, message, error), (message) -> this.plugin.getLogger().log(Level.INFO, message), logErrors, logSentData, logResponseStatusText, false);
	}
	
	public void shutdown() { metricsBase.shutdown(); }
	/**
	 * Adds a custom chart.
	 *
	 * @param chart The chart to add.
	 */
	public void addCustomChart(CustomChart chart) { metricsBase.addCustomChart(chart); }
	private void appendPlatformData(JsonObject builder) {
	    builder.addProperty("playerAmount", getPlayerAmount());
	    builder.addProperty("onlineMode", Bukkit.getOnlineMode() ? 1 : 0);
	    builder.addProperty("bukkitVersion", Bukkit.getVersion());
	    builder.addProperty("bukkitName", Bukkit.getName());
	    builder.addProperty("javaVersion", System.getProperty("java.version"));
	    builder.addProperty("osName", System.getProperty("os.name"));
	    builder.addProperty("osArch", System.getProperty("os.arch"));
	    builder.addProperty("osVersion", System.getProperty("os.version"));
	    builder.addProperty("coreCount", Runtime.getRuntime().availableProcessors());
	}
	private void appendServiceData(JsonObject builder) { builder.addProperty("pluginVersion", plugin.getDescription().getVersion()); }
	private int getPlayerAmount() {
		try {
			Method onlinePlayersMethod = Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers");
			return onlinePlayersMethod.getReturnType().equals(Collection.class) ? ((Collection<?>) onlinePlayersMethod.invoke(Bukkit.getServer())).size() : ((Player[]) onlinePlayersMethod.invoke(Bukkit.getServer())).length;
		} catch (Exception e) { return Bukkit.getOnlinePlayers().size(); }
	}

	public static class MetricsBase {
		public static final String METRICS_VERSION = "3.1.0";
		private static final String REPORT_URL = "https://bStats.org/api/v2/data/%s";
		private final ScheduledExecutorService scheduler;
		private final String platform;
		private final String serverUuid;
		private final int serviceId;
		private final Consumer<JsonObject> appendPlatformDataConsumer;
		private final Consumer<JsonObject> appendServiceDataConsumer;
		private final Consumer<Runnable> submitTaskConsumer;
		private final Supplier<Boolean> checkServiceEnabledSupplier;
		private final BiConsumer<String, Throwable> errorLogger;
		private final Consumer<String> infoLogger;
		private final boolean logErrors;
		private final boolean logSentData;
		private final boolean logResponseStatusText;
		private final Set<CustomChart> customCharts = new HashSet<>();
		private final boolean enabled;

		/**
		 * Creates a new MetricsBase class instance.
		 *
		 * @param platform The platform of the service.
		 * @param serviceId The id of the service.
		 * @param serverUuid The server uuid.
		 * @param enabled Whether or not data sending is enabled.
		 * @param appendPlatformDataConsumer A consumer that receives a {@code JsonObjectBuilder} and
		 *     appends all platform-specific data.
		 * @param appendServiceDataConsumer A consumer that receives a {@code JsonObjectBuilder} and
		 *     appends all service-specific data.
		 * @param submitTaskConsumer A consumer that takes a runnable with the submit task. This can be
		 *     used to delegate the data collection to a another thread to prevent errors caused by
		 *     concurrency. Can be {@code null}.
		 * @param checkServiceEnabledSupplier A supplier to check if the service is still enabled.
		 * @param errorLogger A consumer that accepts log message and an error.
		 * @param infoLogger A consumer that accepts info log messages.
		 * @param logErrors Whether or not errors should be logged.
		 * @param logSentData Whether or not the sent data should be logged.
		 * @param logResponseStatusText Whether or not the response status text should be logged.
		 * @param skipRelocateCheck Whether or not the relocate check should be skipped.
		 */
		public MetricsBase(
			String platform,
			String serverUuid,
			int serviceId,
			boolean enabled,
			Consumer<JsonObject> appendPlatformDataConsumer,
			Consumer<JsonObject> appendServiceDataConsumer,
			Consumer<Runnable> submitTaskConsumer,
			Supplier<Boolean> checkServiceEnabledSupplier,
			BiConsumer<String, Throwable> errorLogger,
			Consumer<String> infoLogger,
			boolean logErrors,
			boolean logSentData,
			boolean logResponseStatusText,
			boolean skipRelocateCheck) {
				ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1, task -> { Thread thread = new Thread(task, "bStats-Metrics"); thread.setDaemon(true); return thread; });
				// We want delayed tasks (non-periodic) that will execute in the future to be
				// cancelled when the scheduler is shutdown.
				// Otherwise, we risk preventing the server from shutting down even when
				// MetricsBase#shutdown() is called
				scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
				this.scheduler = scheduler;
				this.platform = platform;
				this.serverUuid = serverUuid;
				this.serviceId = serviceId;
				this.enabled = enabled;
				this.appendPlatformDataConsumer = appendPlatformDataConsumer;
				this.appendServiceDataConsumer = appendServiceDataConsumer;
				this.submitTaskConsumer = submitTaskConsumer;
				this.checkServiceEnabledSupplier = checkServiceEnabledSupplier;
				this.errorLogger = errorLogger;
				this.infoLogger = infoLogger;
				this.logErrors = logErrors;
				this.logSentData = logSentData;
				this.logResponseStatusText = logResponseStatusText;
				if (enabled) {
					// WARNING: Removing the option to opt-out will get your plugin banned from
					// bStats
					startSubmitting();
				}
		}
		public void addCustomChart(CustomChart chart) { this.customCharts.add(chart); }
		public void shutdown() { scheduler.shutdown(); scheduler.shutdownNow(); }
		private void startSubmitting() {
		    final Runnable submitTask = () -> {
		        if (!enabled || !checkServiceEnabledSupplier.get()) { scheduler.shutdown(); return; }
		        if (submitTaskConsumer != null) { submitTaskConsumer.accept(this::submitData); } else { submitData(); }
		    };
		    long initialDelay = (long) (1000 * 60 * (3 + Math.random() * 3));
		    long secondDelay = (long) (1000 * 60 * (Math.random() * 30));
		    scheduler.schedule(submitTask, initialDelay, TimeUnit.MILLISECONDS);
		    scheduler.scheduleAtFixedRate(submitTask, initialDelay + secondDelay, 1000 * 60 * 30, TimeUnit.MILLISECONDS);
		}
		private void submitData() {
		    CompletableFuture.runAsync(() -> {
		        try {
		            JsonObject baseJson = new JsonObject();
		            JsonObject serviceJson = new JsonObject();
		            appendPlatformDataConsumer.accept(baseJson);
		            appendServiceDataConsumer.accept(serviceJson);
		            JsonArray chartDataArray = new JsonArray();
		            customCharts.stream().map(customChart -> customChart.getRequestJsonObject(errorLogger, logErrors)).filter(Objects::nonNull).forEach(chartDataArray::add);
		            serviceJson.addProperty("id", serviceId);
		            serviceJson.add("customCharts", chartDataArray);
		            baseJson.add("service", serviceJson);
		            baseJson.addProperty("serverUUID", serverUuid);
		            baseJson.addProperty("metricsVersion", METRICS_VERSION);
		            sendData(baseJson);
		        } catch (Exception e) { if (logErrors) { errorLogger.accept("Error preparing bStats metrics data", e); } }
		    });
		}
	    private CompletableFuture<Void> sendData(JsonObject data) {
	        return CompletableFuture.runAsync(() -> {
	        	if (logSentData) { infoLogger.accept("Sending bStats metrics data: "+data.toString()); }
	            try {
	                String urlString = String.format(REPORT_URL, platform);
	                URI uri = URI.create(urlString);
	                byte[] compressedData = compress(data.toString());
	                HttpClient client = HttpClient.newHttpClient();
	                HttpRequest request = HttpRequest.newBuilder()
	                    .uri(uri)
	                    .header("Accept", "application/json")
	                    .header("Content-Encoding", "gzip")
	                    .header("Content-Type", "application/json")
	                    .header("User-Agent", "Metrics-Service/1")
	                    .POST(HttpRequest.BodyPublishers.ofByteArray(compressedData))
	                    .timeout(Duration.ofSeconds(3))
	                    .build();
	                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
	                    .thenAccept(response -> {
	                        if (logResponseStatusText) { infoLogger.accept("Sent data to bStats and received response: "+response.body()); }
	                    })
	                    .exceptionally(e -> {
	                        infoLogger.accept("Failed to send data to bStats: "+e.getMessage());
	                        return null;
	                    });
	            } catch (Exception e) { infoLogger.accept("Failed to send data asynchronously: "+e.getMessage()); }
	        });
	    }
        /**
         * Gzips the given string.
         *
         * @param str The string to gzip.
         * @return The gzipped string.
         */
        private byte[] compress(String data) throws Exception {
        	try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
        		gzipStream.write(data.getBytes(StandardCharsets.UTF_8));
        		gzipStream.finish();
        		return byteStream.toByteArray();
        	}
        }
    }

	public static class AdvancedBarChart extends CustomChart {
		private final Callable<Map<String, int[]>> callable;
		/**
		 * Class constructor.
		 *
		 * @param chartId The id of the chart.
		 * @param callable The callable which is used to request the chart data.
		 */
		public AdvancedBarChart(String chartId, Callable<Map<String, int[]>> callable) { super(chartId); this.callable = callable; }

		@Override
		protected JsonObject getChartData() throws Exception {
		    JsonObject valuesObject = new JsonObject();
		    Map<String, int[]> map = callable.call();
		    if (map == null || map.isEmpty()) { return null; }
		    boolean allSkipped = true;
		    for (Map.Entry<String, int[]> entry : map.entrySet()) {
		        if (entry.getValue().length == 0) { continue; }
		        allSkipped = false;
		        JsonArray jsonArray = new JsonArray();
		        for (int value : entry.getValue()) { jsonArray.add(value); }
		        valuesObject.add(entry.getKey(), jsonArray);
		    }
		    if (allSkipped) { return null; }
		    JsonObject rootObject = new JsonObject();
		    rootObject.add("values", valuesObject);
		    return rootObject;
		}
	}

	public static class SimplePie extends CustomChart {
		private final Callable<String> callable;
		/**
		 * Class constructor.
		 *
		 * @param chartId The id of the chart.
		 * @param callable The callable which is used to request the chart data.
		 */
		public SimplePie(String chartId, Callable<String> callable) { super(chartId); this.callable = callable; }

		@Override
		protected JsonObject getChartData() throws Exception {
		    String value = callable.call();
		    if (value == null || value.isEmpty()) { return null; }
		    JsonObject rootObject = new JsonObject();
		    rootObject.addProperty("value", value);
		    return rootObject;
		}
	}

	public static class DrilldownPie extends CustomChart {
		private final Callable<Map<String, Map<String, Integer>>> callable;
		/**
		 * Class constructor.
		 *
		 * @param chartId The id of the chart.
		 * @param callable The callable which is used to request the chart data.
		 */
		public DrilldownPie(String chartId, Callable<Map<String, Map<String, Integer>>> callable) { super(chartId); this.callable = callable; }

		@Override
		public JsonObject getChartData() throws Exception {
		    JsonObject valuesObject = new JsonObject();
		    Map<String, Map<String, Integer>> map = callable.call();
		    if (map == null || map.isEmpty()) { return null; }
		    boolean reallyAllSkipped = true;
		    for (Map.Entry<String, Map<String, Integer>> entryValues : map.entrySet()) {
		        JsonObject valueObject = new JsonObject();
		        boolean allSkipped = true;
		        for (Map.Entry<String, Integer> valueEntry : entryValues.getValue().entrySet()) { valueObject.addProperty(valueEntry.getKey(), valueEntry.getValue()); allSkipped = false; }
		        if (!allSkipped) { reallyAllSkipped = false; valuesObject.add(entryValues.getKey(), valueObject); }
		    }
		    if (reallyAllSkipped) { return null; }
		    JsonObject rootObject = new JsonObject();
		    rootObject.add("values", valuesObject);
		    return rootObject;
		}

	}

	public static class SingleLineChart extends CustomChart {
		private final Callable<Integer> callable;
		/**
		 * Class constructor.
		 *
		 * @param chartId The id of the chart.
		 * @param callable The callable which is used to request the chart data.
		 */
		public SingleLineChart(String chartId, Callable<Integer> callable) { super(chartId); this.callable = callable; }

		@Override
		protected JsonObject getChartData() throws Exception {
		    int value = callable.call();
		    if (value == 0) { return null; }
		    JsonObject rootObject = new JsonObject();
		    rootObject.addProperty("value", value);
		    return rootObject;
		}

	}

	public static class MultiLineChart extends CustomChart {
		private final Callable<Map<String, Integer>> callable;
		/**
		 * Class constructor.
		 *
		 * @param chartId The id of the chart.
		 * @param callable The callable which is used to request the chart data.
     	*/
		public MultiLineChart(String chartId, Callable<Map<String, Integer>> callable) { super(chartId); this.callable = callable; }

		@Override
		protected JsonObject getChartData() throws Exception {
		    JsonObject valuesObject = new JsonObject();
		    Map<String, Integer> map = callable.call();
		    if (map == null || map.isEmpty()) { return null; }
		    boolean allSkipped = true;
		    for (Map.Entry<String, Integer> entry : map.entrySet()) {
		        if (entry.getValue() == 0) { continue; }
		        allSkipped = false;
		        valuesObject.addProperty(entry.getKey(), entry.getValue());
		    }
		    if (allSkipped) { return null; }
		    JsonObject rootObject = new JsonObject();
		    rootObject.add("values", valuesObject);
		    return rootObject;
		}

	}

	public static class AdvancedPie extends CustomChart {
		private final Callable<Map<String, Integer>> callable;
		/**
		 * Class constructor.
		 *
		 * @param chartId The id of the chart.
		 * @param callable The callable which is used to request the chart data.
		 */
		public AdvancedPie(String chartId, Callable<Map<String, Integer>> callable) { super(chartId); this.callable = callable; }

		@Override
		protected JsonObject getChartData() throws Exception {
		    JsonObject valuesObject = new JsonObject();
		    Map<String, Integer> map = callable.call();
		    if (map == null || map.isEmpty()) { return null; }
		    boolean allSkipped = true;
		    for (Map.Entry<String, Integer> entry : map.entrySet()) {
		        if (entry.getValue() == 0) { continue; }
		        allSkipped = false;
		        valuesObject.addProperty(entry.getKey(), entry.getValue());
		    }
		    if (allSkipped) { return null; }
		    JsonObject rootObject = new JsonObject();
		    rootObject.add("values", valuesObject);
		    return rootObject;
		}
	}

	public abstract static class CustomChart {
		private final String chartId;
		protected CustomChart(String chartId) {
			if (chartId == null) { throw new IllegalArgumentException("chartId must not be null"); }
			this.chartId = chartId;
		}
		public JsonObject getRequestJsonObject(BiConsumer<String, Throwable> errorLogger, boolean logErrors) {
			JsonObject builder = new JsonObject();
			builder.addProperty("chartId", chartId);
			try {
				JsonObject data = getChartData();
				if (data == null) { return null; } builder.add("data", data); } catch (Throwable t) { if (logErrors) { errorLogger.accept("Failed to get data for custom chart with id " + chartId, t); } return null;
			}
			return builder;
		}
		protected abstract JsonObject getChartData() throws Exception; {}
		public static class SimpleBarChart extends CustomChart {
			private final Callable<Map<String, Integer>> callable;
			/**
			 * Class constructor.
			 *
			 * @param chartId The id of the chart.
			 * @param callable The callable which is used to request the chart data.
			 */
			public SimpleBarChart(String chartId, Callable<Map<String, Integer>> callable) { super(chartId); this.callable = callable; }
			protected JsonObject getChartData() throws Exception {
				JsonObject valuesObject = new JsonObject();
				Map<String, Integer> map = callable.call();
  				if (map == null || map.isEmpty()) { return null; }
  				for (Map.Entry<String, Integer> entry : map.entrySet()) { valuesObject.add(entry.getKey(), new JsonPrimitive(entry.getValue())); }
  				JsonObject rootObject = new JsonObject();
  				rootObject.add("values", valuesObject);
  				return rootObject;
			}
		}
	}
}