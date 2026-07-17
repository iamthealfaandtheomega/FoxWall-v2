package thezowi.foxwall.proxy.bukkit.listeners;

import java.net.InetAddress;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketHandshakeReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketLoginReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketStatusReceiveEvent;
import com.github.retrooper.packetevents.protocol.player.User;

import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import thezowi.foxwall.utils.FilesManager;
import thezowi.foxwall.utils.FoxWallAPI;
import thezowi.foxwall.utils.SharedFunctions;

public class PEListener extends FilesManager implements Listener {
	private static final FoxWallAPI api = FoxWallAPI.INSTANCE;
	private static final JavaPlugin plugin = (JavaPlugin) api.getPlugin();
	private static PacketListenerCommon adapter;
	
	public PEListener() throws Throwable {
		PacketEvents.setAPI(SpigotPacketEventsBuilder.build(plugin));
        PacketEvents.getAPI().getSettings().checkForUpdates(false).fullStackTrace(false).debug(false);
        PacketEvents.getAPI().load();
		this.register();
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        InetAddress address = event.getAddress();
        if (address == null) {
        	if (SharedFunctions.DEBUG) {
        		SharedFunctions.logger.info(() -> "Blocked an attempt of null connection. Event type! (one user knows the original IP of the backend!)");
        	}
        	
        	event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, FilesManager.LANG_KICK_NULL);
            return;
        }
        
        String ip = address.getHostAddress();
        if (ALLOWED_PROXY_IP.contains(ip)) {
        	if (SharedFunctions.DEBUG) {
        		SharedFunctions.logger.info(() -> "Blocked an attempt of connection to "+ip+". Event type! (one user knows the original IP of the backend!)");
        	}
        	
        	event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, FilesManager.LANG_KICK_SAMEBACKEND);
            return;
        }
    }
	
	@EventHandler
	public void onServerPing(ServerListPingEvent event){
		event.setServerIcon(null);
	}
	
    public void register() {
        if (adapter != null) { return; }
        adapter = PacketEvents.getAPI().getEventManager().registerListener(new FoxWallPacketListener());
        PacketEvents.getAPI().init();
    }
    
    public static class FoxWallPacketListener extends PacketListenerCommon {
        @Override
        public PacketListenerPriority getPriority() { return PacketListenerPriority.HIGHEST; }
        public static void onHandshake(PacketHandshakeReceiveEvent event) { handle(event.getSocketAddress().getAddress(), event); }
        public static void onPacketLoginStart(PacketLoginReceiveEvent event) { handle(event.getSocketAddress().getAddress(), event); }
        public static void onPacketStatusPing(PacketStatusReceiveEvent event) { handle(event.getSocketAddress().getAddress(), event); }
        
        private static void handle(InetAddress address, PacketReceiveEvent event) {
            if (address == null) {
            	if (SharedFunctions.DEBUG) {
            		SharedFunctions.logger.info(() -> "Blocked an attempt of null connection. Packet: "+event.getPacketId());
            	}
                event.setCancelled(true);
                try {
                	((User) event.getPlayer()).closeConnection();
                } catch (Throwable ig) {}
                return; 
            }
            
            String ip = address.getHostAddress();
            
            if (!ALLOWED_PROXY_IP.contains(ip)) {
            	if (SharedFunctions.DEBUG) {
            		SharedFunctions.logger.info(() -> "Blocked an attempt of connection to "+ip+". Packet: "+event.getPacketId());
            	}
            	event.setCancelled(true); 
            	try {
            		((User) event.getPlayer()).closeConnection();
            	} catch (Throwable ig) {}
            	return;
            }
        }
    }
    
    public static void unregister() {
        if (adapter != null) {
            PacketEvents.getAPI().getEventManager().unregisterListener(adapter);
            adapter = null;
            SharedFunctions.logger.info("[CORE] Done! Unregistered from PacketEvents handler.");
        }
    }
}