package thezowi.foxwall.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;

public enum LibrariesRelocator {
	INSTANCE;

	private static final String TARGET_PREFIX = "thezowi.foxwall.libs.";

	private static final List<Relocation> RULES = List.of(
		new Relocation("net.kyori", TARGET_PREFIX+"net.kyori"),
		new	Relocation("org.slf4j", TARGET_PREFIX+"org.slf4j"),
		new Relocation("com.maxmind", TARGET_PREFIX+"com.maxmind"),
		new Relocation("org.apache", TARGET_PREFIX+"org.apache"),
		new Relocation("com.google", TARGET_PREFIX+"com.google"),
		new Relocation("org.firebirdsql", TARGET_PREFIX+"org.firebirdsql"),
		new Relocation("lombok", TARGET_PREFIX+"lombok"),
		new Relocation("eu.okaeri", TARGET_PREFIX+"eu.okaeri"),
		new Relocation("zoruafan.foxgate.shared.yaml", TARGET_PREFIX+"org.yaml"),
		new Relocation("org.yaml.snakeyaml", TARGET_PREFIX+"org.yaml.snakeyaml"),
		new Relocation("tools.jackson", TARGET_PREFIX+"tools.jackson"),
		new Relocation("org.xbill.DNS", TARGET_PREFIX+"org.xbill.DNS"),
		new Relocation("com.fasterxml", TARGET_PREFIX+"com.fasterxml"),
		new Relocation("io.github", TARGET_PREFIX+"io.github"),
		new Relocation("zoruafan.foxgate.shared", TARGET_PREFIX)
	);
    private static final Set<String> IGNORED_PREFIXES = Set.of("class", "javax", "META-INF");

    public static String detectBasePackage(File jarFile) {
    	try (JarFile jar = new JarFile(jarFile)) {
    		return jar.stream()
    			.filter(entry -> !entry.isDirectory() && entry.getName().endsWith(".class"))
    			.map(entry -> {
    				String name = entry.getName();
    				int lastSlash = name.lastIndexOf('/');
    				return lastSlash > 0 ? name.substring(0, lastSlash).replace('/', '.') : null;
    			})
    			.filter(pkg -> pkg != null && IGNORED_PREFIXES.stream().noneMatch(pkg::startsWith))
    			.findFirst()
    			.orElse(null);
    	} catch (Throwable ignored) {
    		return null;
    	}
    }

    public File relocateJar(File input, File output, boolean log) throws Exception {
    	String packageBase = detectBasePackage(input);
    	if (packageBase == null) return input;
    	
    	List<Relocation> rules = new ArrayList<>(RULES.size() + 1);
    	rules.add(new Relocation(packageBase, TARGET_PREFIX + packageBase));
    	rules.addAll(RULES);

    	new JarRelocator(input, output, rules).run();
    	return output;
    }
}