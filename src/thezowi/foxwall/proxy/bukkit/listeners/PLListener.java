package thezowi.foxwall.proxy.bukkit.listeners;

import java.net.InetAddress;
import java.util.Set;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import thezowi.foxwall.utils.FilesManager;
import thezowi.foxwall.utils.FoxWallAPI;
import thezowi.foxwall.utils.SharedFunctions;

public class PLListener extends FilesManager implements Listener {
	private static final FoxWallAPI api = FoxWallAPI.INSTANCE;
	private static final JavaPlugin plugin = (JavaPlugin) api.getPlugin();
    private static final Set<PacketType> CONNECTION_PACKETS = Set.of(PacketType.Handshake.Client.SET_PROTOCOL, PacketType.Login.Client.START, PacketType.Login.Client.LOGIN_ACK, PacketType.Status.Client.PING);
    private static PacketAdapter packetAdapter;
    
	public PLListener() throws Throwable {
		register();
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
	
	public static void register() {
        if (packetAdapter != null) { return; }
        packetAdapter = new PacketAdapter(plugin, ListenerPriority.HIGHEST,
                PacketType.Handshake.Client.SET_PROTOCOL,
                PacketType.Login.Client.START,
                PacketType.Login.Client.LOGIN_ACK,
                PacketType.Login.Client.CUSTOM_PAYLOAD,
                PacketType.Login.Client.ENCRYPTION_BEGIN,
                PacketType.Login.Client.COOKIE_RESPONSE,
                PacketType.Status.Client.START,
                PacketType.Status.Client.PING
        	) {
            
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (CONNECTION_PACKETS.contains(event.getPacketType())) { handleConnectionPacket(event); }
            }
            
            private void handleConnectionPacket(PacketEvent event) {
                final InetAddress address = event.getPlayer().getAddress().getAddress();
                if (address == null) {
                	if (SharedFunctions.DEBUG) {
                		SharedFunctions.logger.info(() -> "Blocked an attempt of null connection. Packet: "+event.getPacket().getType());
                	}
                	event.setCancelled(true);
                	return;
                }
                
                final String ip = address.getHostAddress();
                if (!ALLOWED_PROXY_IP.contains(ip)) {
                	if (SharedFunctions.DEBUG) {
                		SharedFunctions.logger.info(() -> "Blocked an attempt of connection to "+ip+". Packet: "+event.getPacket().getType());
                	}
                	event.setCancelled(true);
                	return;
                }
            }
        };
        
        ProtocolLibrary.getProtocolManager().addPacketListener(packetAdapter);
    }
	
    public static void unregister() {
        if (packetAdapter != null) {
            ProtocolLibrary.getProtocolManager().removePacketListener(packetAdapter);
            packetAdapter = null;
            SharedFunctions.logger.info("[CORE] Done! Unregistered from ProtocolLib handler.");
        }
    }
}