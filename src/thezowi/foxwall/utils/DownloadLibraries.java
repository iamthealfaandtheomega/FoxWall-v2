package thezowi.foxwall.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.util.Map.entry;

public class DownloadLibraries extends SharedFunctions {
	private boolean enableLogs = false;
	public static Object classLoader;
	protected boolean failed = false;
	private static volatile HttpClient CLIENT;
	
	public static class LibraryConfig {
	    private final String name;
	    private final String version;
	    private final String sha256;
	    private final String fileName;
	    private final String fallbackURL;
	    private final String downloadURL;
	    public LibraryConfig(String name, String version, String sha256, String fileName, String downloadUrl, String fallback) {
	        this.name = name;
	        this.version = version;
	        this.sha256 = sha256;
	        this.fileName = fileName;
	        this.fallbackURL = fallback;
	        this.downloadURL = downloadUrl;
	    }
	    public String getName() { return this.name; }
	    public String getVersion() { return this.version; }
	    public String getSHA256() { return this.sha256; }
	    public String getFile() { return this.fileName; }
	    public String getFallback() { return this.fallbackURL; }
	    public String getDownloadURL() { return this.downloadURL; }
	}
	
	private static String MMV = "5.1.1";
	private static String EAV = "1.3.0";
	private static String ASV = "9.10.1";
	private static String OCK = "6.1.0-beta.4";
	
	private static String SQL_SHA = "28ACEECFCC9535645BD19FA988385703C7B89982C1506A6855F5942B4032ECA6";
	
	private static String JAR_SHA = "7311ECF48815A4D47BF701BC9E397A186C0C71D9078FC25726904B9242C9D19E";
	private static String ASM_SHA = "ED825D10AB1399C8C0CB669E688CF0C8C82629B4C8399B58352B68E92CA10FCB";
	private static String ASMC_SHA = "6D0ABEFB7CBF972EA16EDB37EC14835372505063A45F976AB7EA889ED9497895";
	
	private static final Map<String, LibraryConfig> LIBRARIES = Map.ofEntries(
		// Libraries
		entry("minimessage", new LibraryConfig("Adventure MiniMessage", MMV, "59D34DD7A41835FF8D758F2215505870FE594286F2841A502D0512F25410E4F4", "minimessage.jar", "https://repo1.maven.org/maven2/net/kyori/adventure-text-minimessage/{v}/adventure-text-minimessage-{v}.jar", "https://cdn.zowi.gay/foxwall/relocated-minimessage.jar")),
		entry("minimessage-api", new LibraryConfig("Adventure API", MMV, "49D02D47EF545AD1058F34A7582CE7D1DD08BB7A4E6E058B6422C16C9B8B0EC9", "minimessage-api.jar", "https://repo1.maven.org/maven2/net/kyori/adventure-api/{v}/adventure-api-{v}.jar", "https://cdn.zowi.gay/foxwall/relocated-minimessage-api.jar")),
		entry("minimessage-gson", new LibraryConfig("Adventure GSON", MMV, "91B1CE71731AD88CB0016A18877A7387B95CF7636876080314C47EA28AB70F7E", "minimessage-gson.jar", "https://repo1.maven.org/maven2/net/kyori/adventure-text-serializer-gson/{v}/adventure-text-serializer-gson-{v}.jar", "https://cdn.zowi.gay/foxwall/relocated-minimessage-gson.jar")),
		entry("minimessage-json", new LibraryConfig("Adventure JSON", MMV, "C553B169745B82EB1650215B9ACEA8D865AB3B45020752B4CD025DC92852115F", "minimessage-json.jar", "https://repo1.maven.org/maven2/net/kyori/adventure-text-serializer-json/{v}/adventure-text-serializer-json-{v}.jar", "https://cdn.zowi.gay/foxwall/relocated-minimessage-json.jar")),
		entry("minimessage-key", new LibraryConfig("Adventure Key", MMV, "AEFCCD80A8B228D23286C0EAE8E33388E9C6A03E27CA0ED5D2557E6BC2104909", "minimessage-key.jar", "https://repo1.maven.org/maven2/net/kyori/adventure-key/{v}/adventure-key-{v}.jar", "https://cdn.zowi.gay/foxwall/relocated-minimessage-key.jar")),
		entry("minimessage-legacy", new LibraryConfig("Adventure Legacy Serializer", MMV, "8395B3B3A594574AE767B24C15E082360BCA1C314DE13AF6F5EB3A7DA6BDF888", "minimessage-legacy.jar", "https://repo1.maven.org/maven2/net/kyori/adventure-text-serializer-legacy/{v}/adventure-text-serializer-legacy-{v}.jar", "https://cdn.zowi.gay/foxwall/relocated-minimessage-legacy.jar")),
		entry("minimessage-plain", new LibraryConfig("Adventure Plain Serializer", MMV, "72D106CF0787402908C491A77C35BCA7ECA4258819E15834F349459E39505B47", "minimessage-plain.jar", "https://repo1.maven.org/maven2/net/kyori/adventure-text-serializer-plain/{v}/adventure-text-serializer-plain-{v}.jar", "https://cdn.zowi.gay/foxwall/relocated-minimessage-plain.jar")),
		entry("minimessage-bungeecord", new LibraryConfig("Adventure BungeeCord Serializer", "4.4.1", "E1BC376C6DC7A2100016015735CE4CCC534D2B26BF5AB82AAC751C5032056C39", "minimessage-bungeecord-serializer.jar", "https://repo1.maven.org/maven2/net/kyori/adventure-text-serializer-bungeecord/{v}/adventure-text-serializer-bungeecord-{v}.jar", "https://cdn.zowi.gay/foxwall/relocated-minimessage-bungeecord-serializer.jar")),
		entry("minimessage-examinationapi", new LibraryConfig("Adventure Examination API", EAV, "C9237FFECB05428F6EFF86216246AC70CE0B47B04C08EA7CA35020FDE57F8492", "minimessage-examinationapi.jar", "https://repo1.maven.org/maven2/net/kyori/examination-api/{v}/examination-api-{v}.jar", "https://cdn.zowi.gay/foxwall/relocated-minimessage-examinationapi.jar")),
		entry("minimessage-examinationstring", new LibraryConfig("Adventure Examination String", EAV, "7D01FC25A4BB3AF0E1662685455F4541FBF4626216EA5846E455C1491E156B8C", "minimessage-examinationstring.jar", "https://repo1.maven.org/maven2/net/kyori/examination-string/{v}/examination-string-{v}.jar", "https://cdn.zowi.gay/foxwall/relocated-minimessage-examinationstring.jar")),
		entry("minimessage-option", new LibraryConfig("Adventure Option", "1.1.0", "97B69B4B17DFE02217C9131AD342564CBC9AEBD04C75EB689639B5F78FD4B11C", "minimessage-option.jar", "https://repo1.maven.org/maven2/net/kyori/option/{v}/option-{v}.jar", "https://cdn.zowi.gay/foxwall/relocated-minimessage-option.jar")),
		entry("talemessage", new LibraryConfig("TaleMessage", "1.0.2", "A8493FDC293E2038DE29E1643EE3D3779E09AA14BD8D00897CD306D2AA0AFA7B", "talemessage.jar", "https://github.com/ImZowi/TaleMessage-FoxGate/releases/download/auto-build/TaleMessage-1.0.2.jar", "https://cdn.zowi.gay/foxwall/relocated-talemessage.jar")),
		
		entry("gson", new LibraryConfig("GSON", "2.14.0", "2CBD119BF1961C28788310963DC80BA65F58CDEEC1DD139C8BDB1240FAA2C36F", "gson.jar", "https://repo1.maven.org/maven2/com/google/code/gson/gson/{v}/gson-{v}.jar", "https://cdn.zowi.gay/foxwall/relocated-gson.jar")),
		
		// Reallocator
		entry("jarr", new LibraryConfig("Jar Relocator Fork", "1.8-FOX", JAR_SHA, "jar-relocator.jar", "https://github.com/ImZowi/jar-relocator/releases/download/fork/jar-relocator-1.8.jar", "https://cdn.zowi.gay/relocator/jar-relocator.jar")),
		entry("asm", new LibraryConfig("ASM", ASV, ASM_SHA, "asm.jar", "https://repo1.maven.org/maven2/org/ow2/asm/asm/{v}/asm-{v}.jar", "https://cdn.zowi.gay/relocator/asm.jar")),
		entry("asm-commons", new LibraryConfig("ASM Commons", ASV, ASMC_SHA, "asm-commons.jar", "https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/{v}/asm-commons-{v}.jar", "https://cdn.zowi.gay/relocator/asm-commons.jar")),
		
		// Configuration
		entry("lombok", new LibraryConfig("Lombok", "1.18.46", "01F7B1A015E33E2B62D5F5F37053306357AB1415FD181FCBA7794F5D198C1126", "lombok.jar", "https://repo1.maven.org/maven2/org/projectlombok/lombok/{v}/lombok-{v}.jar", "https://cdn.zowi.gay/foxwall/relocated-lombok.jar")),
		entry("snakeyaml", new LibraryConfig("SnakeYAML", "2.5", "49A839F7BD844BD80E36160BF95D303BFF9E7F18C5C0A3C9466E1E6C4DAF14C0", "snakeyaml.jar", "https://github.com/IDCTeam-Group/FoxGate-Issues/releases/download/dependency-fork/snakeyaml.jar", "https://cdn.zowi.gay/foxwall/relocated-snakeyaml.jar")),
		
		entry("okaeri-configs", new LibraryConfig("Okaeri Configs Core", OCK, "8316E8FE3823272A7972EB2F2ACF4715F37C2EAFA568CE1C83A45A88CC005B45", "okaericonfigs-core.jar", "https://repo.okaeri.cloud/releases/eu/okaeri/okaeri-configs-core/{v}/okaeri-configs-core-{v}.jar", "https://cdn.zowi.gay/foxwall/relocated-okaericonfigs-core.jar")),
		entry("okaeri-configs-yaml", new LibraryConfig("Okaeri Configs SnakeYAML", OCK, "94C95A010346F4D0D1029B5D0232C9F33AF23EB80B99A7DE088368175EBEDAD5", "okaericonfigs-snakeyaml.jar", "https://repo.okaeri.cloud/releases/eu/okaeri/okaeri-configs-yaml-snakeyaml/{v}/okaeri-configs-yaml-snakeyaml-{v}.jar", "https://cdn.zowi.gay/foxwall/relocated-okaericonfigs-snakeyaml.jar")),
		entry("okaeri-configs-commonserdes", new LibraryConfig("Okaeri Configs CommonSerders", OCK, "DF65560AD00D62EC9B545ECE209352616D804EFDB23E08971934975857B3B39F", "okaericonfigs-commonserders.jar", "https://repo.okaeri.cloud/releases/eu/okaeri/okaeri-configs-serdes-commons/{v}/okaeri-configs-serdes-commons-{v}.jar", "https://cdn.zowi.gay/foxwall/relocated-okaericonfigs-commonserders.jar")),
		entry("okaeri-configs-okaeri-validator", new LibraryConfig("Okaeri Configs Okaeri Validator", OCK, "45932CCFC4AA6DC7751E2C9D21F2A78365F2DA6DBFD1B0A412284C45B04610FB", "okaericonfigs-okaeri-validator.jar", "https://repo.okaeri.cloud/releases/eu/okaeri/okaeri-configs-validator-okaeri/{v}/okaeri-configs-validator-okaeri-{v}.jar", "https://cdn.zowi.gay/foxwall/relocated-okaericonfigs-okaeri-validator.jar")),
		entry("okaeri-configs-Validator", new LibraryConfig("Okaeri Configs Validator", "2.0.5", "3AE19C7A329010DDBEA82A308FC89CA5855D028AC994E029D58FB1EA64493FDE", "okaericonfigs-validator.jar", "https://repo.okaeri.cloud/releases/eu/okaeri/okaeri-validator/{v}/okaeri-validator-{v}.jar", "https://cdn.zowi.gay/foxwall/relocated-okaericonfigs-validator.jar"))
	);
	
	public CompletableFuture<Void> downloadLibraries() { return this.downloadLibraries(this.enableLogs, classLoader); }
	public CompletableFuture<Void> downloadLibraries(final boolean logs) { return this.downloadLibraries(logs, this.getClass().getClassLoader()); }
	public CompletableFuture<Void> downloadLibraries(final boolean logs, final Object classloader) {
	    this.enableLogs = logs;
	    if (CLIENT == null) {
	        synchronized (DownloadLibraries.class) {
	            if (CLIENT == null) {
	                CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(7)).followRedirects(HttpClient.Redirect.NORMAL).version(HttpClient.Version.HTTP_1_1).build();
	            }
	        }
	    }
	    
	    classLoader = (classloader != null) ? classloader : this.getClass().getClassLoader();
	    
		Path coreFolder = path.resolve("FoxCore");
		if (Files.notExists(coreFolder)) { try { Files.createDirectories(coreFolder); } catch (Exception ig) {}}
		coreFolder = coreFolder.resolve("libs");
		if (Files.notExists(coreFolder)) { try { Files.createDirectories(coreFolder); } catch (Exception ig) {}}
		Path reloc = coreFolder.resolve("relocator");
		coreFolder = coreFolder.resolve("FoxWall");
		Path versionFile = coreFolder.resolve(".version");
		if (Files.notExists(coreFolder)) { try { Files.createDirectories(coreFolder); } catch (Exception ig) {}}
		if (Files.notExists(reloc)) { try { Files.createDirectories(reloc); } catch (Exception ig) {}}
		Path relocatedFolder = coreFolder.resolve("relocated");
		coreFolder = coreFolder.resolve("downloaded");
		if (Files.notExists(relocatedFolder)) { try { Files.createDirectories(relocatedFolder); } catch (Exception ig) {}}
		if (Files.notExists(coreFolder)) { try { Files.createDirectories(coreFolder); } catch (Exception ig) {}}
		File fo = coreFolder.toFile();
		File fore = reloc.toFile();

	    try {
	        String current = "1.2";
	        if (!Files.exists(versionFile)) {
	        	deleteFolder(coreFolder);
	        	deleteFolder(relocatedFolder);
	            Files.createDirectories(coreFolder);
	            Files.createDirectories(relocatedFolder);
	            Files.createFile(versionFile);
	            Files.write(versionFile, current.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	        } else {
	        	String sv = new String(Files.readAllBytes(versionFile)).trim();
		        if (!sv.equals(current)) {
		        	deleteFolder(coreFolder);
		        	deleteFolder(relocatedFolder);
		            Files.createDirectories(coreFolder);
		            Files.createDirectories(relocatedFolder);
		            Files.write(versionFile, current.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		            SharedFunctions.logger.info("[CORE] Re-downloading all libraries for having issues about version.");
		        }
	        }
	    } catch (Throwable e) {
	        try {
	        	deleteFolder(coreFolder);
	        	deleteFolder(relocatedFolder);
	            Files.createDirectories(coreFolder);
	            Files.createDirectories(relocatedFolder);
	        } catch (Throwable ex) { ex.printStackTrace(); }
	    }
		
	    return this.downloadRelocationTools(fore).thenCompose(v -> {
	        List<CompletableFuture<Void>> downloadTasks = new ArrayList<>();
	        for (Map.Entry<String, LibraryConfig> entry : LIBRARIES.entrySet()) {
	            if (entry.getKey().equals("jarr") || entry.getKey().equals("asm") || entry.getKey().equals("asm-commons")) {
	                continue;
	            }
	            downloadTasks.add(this.check(entry.getValue(), fo.toPath().resolve(entry.getValue().getFile()), relocatedFolder.toFile(), false));
	        }
	        return CompletableFuture.allOf(downloadTasks.toArray(new CompletableFuture[0]));
	        
	    }).exceptionally(ex -> {
	        this.log("Failed to load libraries:");
	        ex.printStackTrace();
	        this.failed = true;
	        return null;
	    });
	}

	private CompletableFuture<Void> downloadRelocationTools(final File file) {
	    return CompletableFuture.allOf(
	            this.check(LIBRARIES.get("jarr"), new File(file, LIBRARIES.get("jarr").getFile()).toPath(), null, true),
	            this.check(LIBRARIES.get("asm"), new File(file, LIBRARIES.get("asm").getFile()).toPath(), null, true),
	            this.check(LIBRARIES.get("asm-commons"), new File(file, LIBRARIES.get("asm-commons").getFile()).toPath(), null, true)
	    );
	}
	
	private CompletableFuture<Void> check(final LibraryConfig config, final Path target, final File relocated, final boolean relocator) {
	    if (target.toFile().exists() && verifyChecksum(target.toFile(), config.getSHA256())) {
	        this.log(config.getName()+" was already downloaded, loading...");
	        try {
	            if (relocator) {
	                this.loadLibraryDirectly(target.toFile());
	            } else {
	                this.loadLibrary(target.toFile(), config, relocated);
	            }
	            return CompletableFuture.completedFuture(null);
	        } catch (Throwable e) {
	            this.log(config.getName()+" failed to load despite existing, re-downloading...");
	            try { Files.deleteIfExists(target); } catch (Throwable ig) {}
	        }
	    }
		
		this.log("Downloading "+config.getName()+"...");
		
		return this.download(config.getDownloadURL(), config, target, relocated, relocator).exceptionallyCompose(ex -> {
			if (config.getFallback() == null || config.getFallback().isBlank()) {
				this.log(config.getName() + " failed and no fallback available.");
				if (enableLogs) { ex.printStackTrace(); }
				return CompletableFuture.failedFuture(ex);
			}
			this.log(config.getName() + " primary failed, trying fallback...");
			try { Files.deleteIfExists(target); } catch (Throwable ig) {}
			return this.download(config.getFallback(), config, target, relocated, relocator).exceptionallyCompose(ex2 -> {
					this.log(config.getName() + " fallback also failed.");
					if (enableLogs) { ex2.printStackTrace(); }
					return CompletableFuture.failedFuture(ex2);
				});
		});
	}
	
	public void downloadSingle(String url, Path target) throws Throwable {
	    if (CLIENT == null) {
	        CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(7)).followRedirects(HttpClient.Redirect.NORMAL).version(HttpClient.Version.HTTP_1_1).build();
	    }
	    this.request(url, target, "").join();
	}
 
	private CompletableFuture<Void> download(final String url, final LibraryConfig config, final Path target, final File relocated, final boolean relocator) {
		return this.request(url, target, config.getVersion()).thenCompose(path -> {
			if (!verifyChecksum(path.toFile(), config.getSHA256())) {
				try { Files.deleteIfExists(target); } catch (Throwable ig) {}
				return CompletableFuture.failedFuture(new RuntimeException("SHA256 mismatch for: "+config.getName()));
			}
			try {
				if (relocator) {
					this.loadLibraryDirectly(target.toFile());
				} else {
					this.loadLibrary(target.toFile(), config, relocated);
				}
				return CompletableFuture.completedFuture(null);
			} catch (Throwable e) {
				try { Files.deleteIfExists(target); } catch (Throwable ig) {}
				return CompletableFuture.failedFuture(e);
			}
		});
	}
    
    private CompletableFuture<Path> request(final String url, final Path target, final String version) {
    	HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url.replace("{v}", version))).timeout(Duration.ofSeconds(10)).GET().build();
        return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).thenCompose(response -> {
        	
            	if (response.statusCode() != 200) {
            		return CompletableFuture.failedFuture(new RuntimeException("HTTP "+response.statusCode()));
            	}
            	return CompletableFuture.supplyAsync(() -> {
            	    try (InputStream in = response.body()) {
            	    	long size = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
            	        DownloadUtils.copyWithProgress(in, target, size, "LIBRARIES");
            	        return target;
            	    } catch (Exception e) {
            	        throw new CompletionException(e);
            	    }
            	});
        });
    }
    
    private void loadLibrary(final File original, final LibraryConfig config, File fore) throws Throwable {
        boolean noUpdate = true;
        
        if (original.exists()) {
            String sha = calculateSha256(original);
            if (sha.equalsIgnoreCase(config.sha256)) {
            	noUpdate = false;
            } else { Files.deleteIfExists(original.toPath()); }
        }
        
        if (!this.noRelocate(config.sha256)) {
        	ReflectUtil.addFileLibrary(original);
            return;
        }
        
        File relocated = new File(fore, "relocated-"+config.fileName);
        if (noUpdate || !relocated.exists()) {
            Files.deleteIfExists(relocated.toPath());
            this.relocateLibrary(original, relocated, config);
        }
        ReflectUtil.addFileLibrary(relocated);
    }
    
    private void loadLibraryDirectly(final File library) throws Throwable {
        if (!library.exists()) { throw new Exception("Library file not found: "+library.getAbsolutePath()); }
        ReflectUtil.addFileLibrary(library);
    }

    private boolean noRelocate(final String sha) {
    	return !sha.equalsIgnoreCase(ASM_SHA) &&  !sha.equalsIgnoreCase(ASMC_SHA) && !sha.equalsIgnoreCase(JAR_SHA) && !sha.equalsIgnoreCase(SQL_SHA);
    }
    
    private void relocateLibrary(final File fore, final File relocated, final LibraryConfig config) throws Throwable {
        if (!relocated.getParentFile().exists()) { relocated.getParentFile().mkdirs(); }
        
        try {
            File result = LibrariesRelocator.INSTANCE.relocateJar(fore, relocated, this.enableLogs);
            if (!result.exists()) {
                throw new Exception("Relocation failed: output file does not exist.");
            }
            
        } catch (Throwable ig) {
            this.log("Relocation failed for "+fore.getName()+", trying fallback download...");
            if (config.fallbackURL == null || config.fallbackURL.isBlank()) {
                throw new Exception("No fallbackURL provided for: "+config.name, ig);
            }
            
            try {
                this.request(config.fallbackURL, relocated.toPath(), config.version).get();
                this.log("Successfully downloaded fallback relocated jar for: "+config.name);
            } catch (Throwable ex) {
                this.log("Failed to download fallback relocated jar for: "+config.name);
                throw new Exception("Relocation failed and fallback failed", ex);
            }
        }
    }
    
    private static void deleteFolder(final Path path) throws Exception {
    	if (Files.exists(path)) {
    		Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    	}
    }
    
    private boolean verifyChecksum(final File file, final String s256) {
        try {
            String a256 = this.calculateSha256(file);
            return a256.equalsIgnoreCase(s256);
        } catch (Exception e) {}
		return false;
    }
    
    private String calculateSha256(File f) throws Exception {
        try (InputStream is = new FileInputStream(f)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) > 0) { digest.update(buffer, 0, read); }
            byte[] hash = digest.digest();
            StringBuilder build = new StringBuilder();
            for (byte bytes : hash) {
                String hex = Integer.toHexString(0xff & bytes);
                if (hex.length() == 1) build.append('0');
                build.append(hex);
            }
            return build.toString().toUpperCase();
        }
    }
    
    public void log(final String log) {
    	if (this.enableLogs) {
    		logger.info(log);
    	}
    }
    
	public void shutdown() {
	    HttpClient cl = CLIENT;
	    CLIENT = null;
	    if (cl != null) { cl.shutdown(); }
        ReflectUtil.CACHED_LOOKUP = null;
        ReflectUtil.CACHED_ADD_URL_METHOD = null;
	}
    
	public static class ReflectUtil {
		private static volatile MethodHandles.Lookup CACHED_LOOKUP = null;
		private static volatile Method CACHED_ADD_URL_METHOD = null;
		
		public static Class<?> getClass(String name) {
			try { return Class.forName(name); } catch (Exception e) { return null; } 
		}
	    
		public static MethodHandles.Lookup getSuperLookup() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
			if (CACHED_LOOKUP != null) return CACHED_LOOKUP;
		    synchronized (ReflectUtil.class) {
		    	if (CACHED_LOOKUP != null) return CACHED_LOOKUP;
		        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
		        Field theUnsafeField = getField(unsafeClass, unsafeClass, true);
		        Method getObject = getMethod(unsafeClass, "getObject", false, new Class[]{ Object.class, long.class });
		        Method staticFieldOffset = getMethod(unsafeClass, "staticFieldOffset", false, new Class[]{ Field.class });
		        Object theUnsafe = theUnsafeField.get(null);
		        Field implLookup = getField(MethodHandles.Lookup.class, "IMPL_LOOKUP", false);
		        CACHED_LOOKUP = (MethodHandles.Lookup) getObject.invoke(theUnsafe, MethodHandles.Lookup.class, staticFieldOffset.invoke(theUnsafe, implLookup));
		        return CACHED_LOOKUP;
		    }
		}
	    
	    public static void addFileLibrary(File file) throws Throwable {
	        ClassLoader classVar = (classLoader == null) ? ReflectUtil.class.getClassLoader() : (ClassLoader) classLoader;
	        if (CACHED_ADD_URL_METHOD == null) {
	            synchronized (ReflectUtil.class) {
	                if (CACHED_ADD_URL_METHOD == null) {
	                    CACHED_ADD_URL_METHOD = getMethodWithParent(classVar.getClass(), "addURL", false, new Class[]{ URL.class });
	                }
	            }
	        }
	        getSuperLookup().unreflect(CACHED_ADD_URL_METHOD).invoke(classVar, file.toURI().toURL());
	    }
	    
		public static Field getField(Class<?> clazz, String target, boolean handleAccessible) throws NoSuchFieldException {
			try {
				Field field = clazz.getDeclaredField(target);
				if (handleAccessible)  field.setAccessible(true); 
				return field;
			} catch (NoSuchFieldException e) {
				throw new NoSuchFieldException(target + " field in " + target);
			} 
		}
	    
		public static Field getField(Class<?> clazz, Class<?> target, boolean handleAccessible) throws NoSuchFieldException {
			return getField(clazz, clazz, target, handleAccessible);
		}
	    
		private static Field getField(Class<?> source, Class<?> clazz, Class<?> target, boolean handleAccessible) throws NoSuchFieldException {
			Field[] arrayOfField;
			int i;
			byte b;
			for (arrayOfField = clazz.getDeclaredFields(), i = arrayOfField.length, b = 0; b < i; ) {
				Field field = arrayOfField[b];
				if (field.getType() != target) {
					b++;
					continue;
				} 
				if (handleAccessible) field.setAccessible(true); 
				return field;
			} 
			clazz = clazz.getSuperclass();
			if (clazz != null) return getField(clazz, target, handleAccessible);
			throw new NoSuchFieldException(target.getName() + " type in " + target.getName());
		}
	    
		public static Method getMethod(Class<?> clazz, String name, boolean handleAccessible, Class<?>... args) throws NoSuchMethodException {
			Method[] arrayOfMethod;
			int i;
			byte b;
			for (arrayOfMethod = clazz.getDeclaredMethods(), i = arrayOfMethod.length, b = 0; b < i; ) {
				Method method = arrayOfMethod[b];
				if (!method.getName().equalsIgnoreCase(name) || !Arrays.equals((Object[])method.getParameterTypes(), (Object[])args)) {
					b++;
					continue;
				} 
				if (handleAccessible) method.setAccessible(true); 
				return method;
			}
			throw new NoSuchMethodException(name + " method in " + name);
		}
	    
		public static Method getMethodWithParent(Class<?> clazz, String name, boolean handleAccessible, Class<?>... args) throws NoSuchMethodException {
			Method[] arrayOfMethod;
			int i;
			byte b;
			for (arrayOfMethod = clazz.getDeclaredMethods(), i = arrayOfMethod.length, b = 0; b < i; ) {
				Method method = arrayOfMethod[b];
				if (!method.getName().equalsIgnoreCase(name) || !Arrays.equals((Object[])method.getParameterTypes(), (Object[])args)) {
					b++;
					continue;
				} 
				if (handleAccessible) method.setAccessible(true); 
				return method;
			} 
			if (clazz != Object.class) return getMethodWithParent(clazz.getSuperclass(), name, handleAccessible, args); 
			throw new NoSuchMethodException(name + " method in " + name);
		}
	}
}