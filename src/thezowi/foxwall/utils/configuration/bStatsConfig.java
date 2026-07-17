package thezowi.foxwall.utils.configuration;

import java.util.UUID;

import thezowi.foxwall.libs.eu.okaeri.configs.OkaeriConfig;
import thezowi.foxwall.libs.eu.okaeri.configs.annotation.Comment;
import thezowi.foxwall.libs.eu.okaeri.configs.annotation.Exclude;
import thezowi.foxwall.libs.eu.okaeri.configs.annotation.Header;
import thezowi.foxwall.libs.eu.okaeri.configs.annotation.ReadOnly;
import thezowi.foxwall.libs.eu.okaeri.validator.annotation.NotBlank;

@Header({
	" _      __  _          _        ",
	"| |__  / _\\| |_  __ _ | |_  ___ ",
	"| '_ \\ \\ \\ | __|/ _` || __|/ __|",
	"| |_) |_\\ \\| |_| (_| || |_ \\__ \\",
	"|_.__/ \\__/ \\__|\\__,_| \\__||___/",
	" ",
    "bStats (https://bStats.org) collects some basic information for plugin authors, like how",
    "many people use their plugin and their total player count. It's recommended to keep bStats",
    "enabled, but if you're not comfortable with this, you can turn this setting off. There is no",
    "performance penalty associated with having metrics enabled, and data sent to bStats is fully",
    "anonymous.",
    "",
    " 📍 All pages of FoxWall in bStats:",
    " - Bukkit: https://bstats.org/plugin/bukkit/FoxWall-Bukkit/26704",
    "",
    "📚 Note: To disable usage of bStats in the plugin FoxWall, please check the 'config.yml'",
    "file for it, option with the name 'metrics'. Read any details required in the comments from that",
    "option.",
    "⚠️ Warning! This file exists for manage the usage of metrics in this plugin, the code of bStats",
    "still the same (including the changes). If you disable the bStats in the folder for all plugins,",
    "you need to disable this too."
})
public class bStatsConfig extends OkaeriConfig {
	@Exclude
	@ReadOnly
	@NotBlank
    private String serverUuid = UUID.randomUUID().toString();
	@Comment({
		"",
		"📍 Logger.",
		" - Logs to console any type of data about bStats. In",
		"   production it's recommended to disable for avoid flood",
		"   in your console. (probably bStats will ratelimit",
		"   for others plugins that uses bStats too)"
	})
    private boolean logFailedRequests = false;
    private boolean logSentData = false;
    private boolean logResponseStatusText = false;

    public String getServerUUID() { return serverUuid != null ? serverUuid : UUID.randomUUID().toString(); }
    public boolean getLogFailedRequests() { return logFailedRequests; }
    public boolean getLogSentData() { return logSentData; }
    public boolean getLogResponseStatusText() { return logResponseStatusText; }
}
