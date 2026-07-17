package thezowi.foxwall.proxy.bukkit;

import java.lang.reflect.Method;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import thezowi.foxwall.utils.FoxWallAPI;
import thezowi.foxwall.utils.PlatformType;

public class PlaceholderAPI {
	private static FoxWallAPI api = FoxWallAPI.INSTANCE;
	
    public static String apply(final Object user, final String message) {
    	if (PlatformType.getPlatform() != PlatformType.T.BUKKIT) { return message; }
    	if (user == null || message == null || ((Server) api.getProxy()).getPluginManager().getPlugin("PlaceholderAPI") == null) { return message; }
    	
        try {
            final Class<?> placeholderApi = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            final Method method = placeholderApi.getMethod("setPlaceholders", Player.class, String.class);
            return (String) method.invoke(null, user, message);
            
        } catch (Throwable ig) { return message; }
    }
    
}