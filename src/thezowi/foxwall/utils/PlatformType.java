package thezowi.foxwall.utils;

public final class PlatformType {
 	private static T type = null;
 	public enum T { BUKKIT, BUNGEECORD, NUKKIT, VELOCITY, WATERDOGPE; }
  
 	public static T getPlatform() { return (type == null) ? T.BUKKIT : type; }
 	public static void setPlatform(T type) { PlatformType.type = type; }
}