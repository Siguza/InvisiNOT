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
        if((event == null) || event.isCancelled() || (event.getPacket() == null) || (event.getPlayer() == null) || (!event.getPlayer().hasPermission("invisinot.see")))
        {
            return;
        }
        try
        {
            boolean found = false;
            StructureModifier<Object> mod = event.getPacket().getModifier();
            for(int i = 0; i < mod.size(); i++)
            {
                Object o = mod.readSafely(i);
                if(o instanceof List)
                {
                    found = true;
                    for(Object abc : (List)o)
                    {
                        WrappedWatchableObject data = new WrappedWatchableObject(abc);
                        if(data.getIndex() == 0)
                        {
                            data.setValue(Byte.valueOf((byte)(((Byte)data.getValue()).byteValue() & 0xffffffdf)));
                        }
                    }
                }
            }
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