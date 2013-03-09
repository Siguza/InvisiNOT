// Bukkit Plugin "InvisiNOT" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.invisinot;

import java.util.*;
import java.lang.reflect.*;
import org.bukkit.potion.PotionEffectType;
import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.*;
import static net.drgnome.invisinot.Global.*;

public class EffectListener extends PacketAdapter
{
    public static void register()
    {
        HashSet<Integer> set = new HashSet<Integer>();
        //set.add(Packets.Server.NAMED_ENTITY_SPAWN);
        //set.add(Packets.Server.MOB_SPAWN);
        set.add(Packets.Server.ENTITY_METADATA);
        ProtocolLibrary.getProtocolManager().addPacketListener(new EffectListener(set));
    }
    
    public static void unregister()
    {
        ProtocolLibrary.getProtocolManager().removePacketListeners(_plugin);
    }
    
    public EffectListener(Set<Integer> set)
    {
        super(_plugin, ConnectionSide.SERVER_SIDE, ListenerPriority.LOWEST, set);
    }
    
    public void onPacketSending(PacketEvent event)
    {
        if(!event.getPlayer().hasPermission("invisinot.see"))
        {
            return;
        }
        try
        {
            boolean found = false;
            PacketContainer packet = event.getPacket().deepClone();
            StructureModifier<Object> mod = packet.getModifier();
            if(mod != null)
            {
                for(int i = 0; i < mod.size(); i++)
                {
                    Object o = mod.readSafely(i);
                    if(o instanceof List)
                    {
                        found = true;
                        List list = (List)o;
                        ArrayList newlist = new ArrayList();
                        for(int j = 0; j < list.size(); j++)
                        {
                            WrappedWatchableObject data = new WrappedWatchableObject(list.get(j));
                            if(data.getIndex() == 0)
                            {
                                newlist.add((new WrappedWatchableObject(0, Byte.valueOf((byte)(((Byte)data.getValue()).byteValue() & 0xdf)))).getHandle());
                            }
                            else
                            {
                                newlist.add(data.getHandle());
                            }
                        }
                        mod.writeSafely(i, newlist);
                    }
                }
            }
            event.setPacket(packet);
            if(found)
            {
                return;
            }
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
        warn();
        _log.warning("[InvisiNOT] Unable to edit outgoing packages.");
    }
}