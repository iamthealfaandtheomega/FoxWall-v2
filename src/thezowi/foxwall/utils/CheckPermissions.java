package thezowi.foxwall.utils;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CheckPermissions {
	public CheckPermissions() {}

	public boolean hasPermission(String e, String p) {
	    try { if (Class.forName("net.luckperms.api.LuckPerms") != null) { return this.check("luckperms", e, p);  } } catch (Exception ig) { }
	    try { if (Class.forName("ru.tehkode.permissions.bukkit.PermissionsEx") != null) { return this.check("permissionsex", e, p); } } catch (Exception ignored) { }
	    return false;
	}

	private boolean check(String type, String e, String p) {
		if(type.toLowerCase().equals("luckperms")) {
		    try {
		    	Class<?> lpC = Class.forName("net.luckperms.api.LuckPermsProvider");
		    	Method gM = lpC.getMethod("get");
		    	Object lpP = gM.invoke(null);
		    	Method guM = lpP.getClass().getMethod("getUserManager");
		    	Object uM = guM.invoke(lpP);
		    	Method lookupMethod = uM.getClass().getMethod("lookupUniqueId", String.class);
		    	CompletableFuture<?> uuidFuture = (CompletableFuture<?>) lookupMethod.invoke(uM, e);
		    	UUID uuid = (UUID) uuidFuture.get();
		    	if (uuid != null) {
		    	    Method loadUser = uM.getClass().getMethod("loadUser", UUID.class);
		    	    CompletableFuture<?> userFuture = (CompletableFuture<?>) loadUser.invoke(uM, uuid);
		    	    Object user = userFuture.get();
		    	    Method gcd = user.getClass().getMethod("getCachedData");
		    	    Object cd = gcd.invoke(user);
		    	    Method gPD = cd.getClass().getMethod("getPermissionData");
		    	    Object Pd = gPD.invoke(cd);
		    	    Method checkPermissionMethod = Pd.getClass().getMethod("checkPermission", String.class);
		    	    Object R = checkPermissionMethod.invoke(Pd, p);
		    	    Method A = R.getClass().getMethod("asBoolean");
		    	    return (boolean) A.invoke(R);
		    	}
	            return false;
		    } catch (Exception ig) { return false; }
		} else if(type.toLowerCase().equals("permissionsex")) {
		    try {
		        Class<?> pc = Class.forName("ru.tehkode.permissions.bukkit.PermissionsEx");
		        Object u = pc.getMethod("getUser", String.class).invoke(null, e);
		        if (u == null) return false;
		        Boolean bo = (boolean) u.getClass().getMethod("has", String.class).invoke(u, p);
		        return bo;
		    } catch (Exception ig) { return false; }
		}
		return false;
	}

}