// Bukkit Plugin "InvisiNOT" by Siguza
// Released under the CC BY-NC-SA 3.0 (CreativeCommons Attribution-NonCommercial-ShareAlike 3.0 Unported) license.
// The full license and a human-readable summary can be found at the following location:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.siguza.invisinot;

import java.lang.reflect.*;
import java.util.*;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import com.comphenix.protocol.*;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.*;

public class MetaListener extends PacketAdapter
{
    private static boolean _alterName;
    private static String _namePrefix;
    private static boolean _prefixNonPlayers;
    private static HashMap<Integer, Boolean> _stateCache;
    
    public static void register(String namePrefix, boolean prefixNonPlayers)
    {
        _namePrefix = namePrefix.replace("&", new String(new char[]{ChatColor.COLOR_CHAR}));
        _alterName = namePrefix.length() > 0;
        _prefixNonPlayers = prefixNonPlayers;
        _stateCache = _alterName ? new HashMap<Integer, Boolean>() : null;
        ArrayList<PacketType> list = new ArrayList<PacketType>();
        list.add(PacketType.Play.Server.ENTITY_METADATA);
        ProtocolLibrary.getProtocolManager().addPacketListener(new MetaListener(list));
        if(_alterName)
        {
            list = new ArrayList<PacketType>();
            list.add(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
            ProtocolLibrary.getProtocolManager().addPacketListener(new PlayerListener(list));
        }
    }
    
    public static void unregister()
    {
        ProtocolLibrary.getProtocolManager().removePacketListeners(InvisiPlugin.instance());
    }
    
    private static boolean isPlayer(int entityID)
    {
        for(Player p : Bukkit.getOnlinePlayers())
        {
            if(p.getEntityId() == entityID)
            {
                return true;
            }
        }
        return false;
    }
    
    private static Player getPlayer(int entityID)
    {
        for(Player p : Bukkit.getOnlinePlayers())
        {
            if(p.getEntityId() == entityID)
            {
                return p;
            }
        }
        return null;
    }
    
    public MetaListener(Iterable<PacketType> list)
    {
        super(InvisiPlugin.instance(), ListenerPriority.HIGHEST, list);
    }
    
    public void onPacketSending(PacketEvent event)
    {
        Player player = event.getPlayer();
        if(!player.hasPermission("invisinot.see"))
        {
            return;
        }
        try
        {
            PacketContainer packet = event.getPacket().deepClone();
            StructureModifier<Object> mod = packet.getModifier();
            if(mod == null)
            {
                InvisiPlugin._log.warning("[InvisiNOT] Unable to edit outgoing packages.");
                return;
            }
            int entityID = (Integer)mod.readSafely(0);
            if(entityID == player.getEntityId())
            {
                return;
            }
            List list = (List)mod.readSafely(1);
            byte modifiers = 0;
            for(int i = 0; i < list.size(); i++)
            {
                WrappedWatchableObject data = new WrappedWatchableObject(list.get(i));
                if(data.getIndex() == 0)
                {
                    modifiers = (Byte)data.getValue();
                }
            }
            boolean isPlayer = isPlayer(entityID);
            boolean isVisible = (modifiers & 0x20) == 0;
            if(isPlayer && _alterName)
            {
                if(_stateCache.containsKey(entityID) && (_stateCache.get(entityID) ^ isVisible))
                {
                    _stateCache.put(entityID, isVisible);
                    ArrayList<Player> plist = new ArrayList<Player>();
                    plist.add(player);
                    ProtocolLibrary.getProtocolManager().updateEntity(getPlayer(entityID), plist);
                    return;
                    /*ProtocolManager manager = ProtocolLibrary.getProtocolManager();
                    Player human = getPlayer(entityID);
                    try
                    {
                        manager.sendServerPacket(player, manager.createPacketConstructor(PacketType.Play.Server.ENTITY_DESTROY, human).createPacket(human), true);
                        manager.sendServerPacket(player, manager.createPacketConstructor(PacketType.Play.Server.NAMED_ENTITY_SPAWN, human).createPacket(human), true);
                        event.setCancelled(true);
                        return;
                    }
                    catch(IllegalArgumentException e)
                    {
                        e.printStackTrace();
                    }*/
                }
                else
                {
                    _stateCache.put(entityID, isVisible);
                }
            }
            if(isVisible)
            {
                return;
            }
            ArrayList<Object> newlist = new ArrayList<Object>();
            newlist.add(new WrappedWatchableObject(0, (byte)(modifiers & 0xdf)).getHandle());
            if(_alterName && _prefixNonPlayers && !isPlayer)
            {
                String name = (String)mod.readSafely(10);
                if((name == null) || (name.length() == 0))
                {
                    try
                    {
                        Entity e = ProtocolLibrary.getProtocolManager().getEntityFromID(player.getWorld(), entityID);
                        Method m = e.getClass().getMethod("getHandle");
                        m.setAccessible(true);
                        Object o = m.invoke(e);
                        m = o.getClass().getMethod("getName");
                        m.setAccessible(true);
                        name = (String)m.invoke(o);
                    }
                    catch(Throwable t)
                    {
                        name = "generic";
                    }
                }
                newlist.add(new WrappedWatchableObject(10, _namePrefix + name).getHandle());
                newlist.add(new WrappedWatchableObject(11, (byte)1).getHandle());
            }
            for(int i = 0; i < list.size(); i++)
            {
                Object o = list.get(i);
                WrappedWatchableObject data = new WrappedWatchableObject(o);
                switch(data.getIndex())
                {
                    case 0:
                        break;
                    case 10:
                    case 11:
                        if(_alterName && _prefixNonPlayers && !isPlayer)
                        {
                            break;
                        }
                    default:
                        newlist.add(o);
                        break;
                }
            }
            mod.writeSafely(1, newlist);
            event.setPacket(packet);
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
    }
    
    public static class PlayerListener extends PacketAdapter
    {
        public PlayerListener(Iterable<PacketType> list)
        {
            super(InvisiPlugin.instance(), ListenerPriority.HIGHEST, list);
        }
        
        public void onPacketSending(PacketEvent event)
        {
            Player player = event.getPlayer();
            if(!player.hasPermission("invisinot.see"))
            {
                return;
            }
            try
            {
                PacketContainer packet = event.getPacket().deepClone();
                StructureModifier<Object> mod = packet.getModifier();
                if(mod == null)
                {
                    InvisiPlugin._log.warning("[InvisiNOT] Unable to edit outgoing packages.");
                    return;
                }
                int entityID = (Integer)mod.readSafely(0);
                if((entityID == player.getEntityId()) || !_stateCache.containsKey(entityID) || _stateCache.get(entityID))
                {
                    return;
                }
                GameProfile gp = (GameProfile)mod.readSafely(1);
                mod.writeSafely(1, new GameProfile(gp.getId(), _namePrefix + gp.getName()));
                event.setPacket(packet);
            }
            catch(Throwable t)
            {
                t.printStackTrace();
            }
        }
    }
}