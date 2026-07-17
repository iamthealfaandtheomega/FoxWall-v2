package thezowi.foxwall.utils;

public final class MinecraftVersion {
	private static V current = null;
	private static int subversion = -1;
  
	public enum V {
		v1_26(26),
		v1_21(21),
		v1_20(20),
		v1_19(19),
		v1_18(18),
		v1_17(17),
		v1_16(16),
		v1_15(15),
		v1_14(14),
		v1_13(13),
		v1_12(12),
		v1_11(11),
		v1_10(10),
		v1_9(9),
		v1_8(8),
		v1_7(7),
		v1_6(6),
		v1_5(5),
		v1_4(4),
		v1_3_AND_BELOW(3);
    
		private final int minorVersionNumber;
    
		V(int version) {
			this.minorVersionNumber = version;
		}

		public static V parse(int number) {
			V[] arrayOfV;
			for (int j = (arrayOfV = values()).length, i = 0; i < j; ) {
				V v = arrayOfV[i];
				if (v.minorVersionNumber == number) return v; 
				i++;
			} 
			return MinecraftVersion.current;
		}
    
		public String toString() { return "1." + this.minorVersionNumber; }
	}
  
	public static boolean equals(V version) { return (compareWith(version) == 0); }
	public static boolean olderThan(V version) { return (compareWith(version) < 0); }
	public static boolean newerThan(V version) { return (compareWith(version) > 0); }
	public static boolean atLeast(V version) { return !(!equals(version) && !newerThan(version)); }
	private static int compareWith(V version) { try { return (getCurrent()).minorVersionNumber - version.minorVersionNumber; } catch (Throwable t) { return 0; } }
	public static String getFullVersion() { return getCurrent().toString() + (getSubversion() > 0 ? "." + getSubversion() : ""); }
	public static V getCurrent() { return current; }
	public static int getSubversion() { return subversion; }
	public static boolean hasVersion() { return (current != null); }
	public static void setVersion(V current, int subversion) { MinecraftVersion.current = current; MinecraftVersion.subversion = subversion; }
}
