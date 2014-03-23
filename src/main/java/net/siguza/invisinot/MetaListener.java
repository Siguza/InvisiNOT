// Bukkit Plugin "InvisiNOT" by Siguza
// Released under the CC BY-NC-SA 3.0 (CreativeCommons Attribution-NonCommercial-ShareAlike 3.0 Unported) license.
// The full license and a human-readable summary can be found at the following location:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.siguza.invisinot;

import java.util.*;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.*;

public class MetaListener extends PacketAdapter
{
    private final boolean _alterName;
    private final String _namePrefix;

    public static void register(String namePrefix)
    {
        ArrayList<PacketType> list = new ArrayList<PacketType>();
        list.add(PacketType.Play.Server.ENTITY_METADATA);
        ProtocolLibrary.getProtocolManager().addPacketListener(new MetaListener(list, namePrefix));
    }
    
    public static void unregister()
    {
        ProtocolLibrary.getProtocolManager().removePacketListeners(InvisiPlugin.instance());
    }
    
    public MetaListener(Iterable<PacketType> list, String namePrefix)
    {
        super(InvisiPlugin.instance(), ListenerPriority.LOWEST, list);
        _namePrefix = namePrefix;
        _alterName = namePrefix.length() > 0;
    }
    
    public void onPacketSending(PacketEvent event)
    {
        if(!event.getPlayer().hasPermission("invisinot.see"))
        {
            return;
        }
        try
        {
            PacketContainer packet = event.getPacket().deepClone();
            StructureModifier<Object> mod = packet.getModifier();
            int num = 0;
            List list = null;
            if(mod != null)
            {
                for(int i = 0; i < mod.size(); i++)
                {
                    Object o = mod.readSafely(i);
                    if(o instanceof List)
                    {
                        num = i;
                        list = (List)o;
                        break;
                    }
                }
            }
            if(list == null)
            {
                InvisiPlugin._log.warning("[InvisiNOT] Unable to edit outgoing packages.");
                return;
            }
            byte modifiers = 0;
            for(int i = 0; i < list.size(); i++)
            {
                WrappedWatchableObject data = new WrappedWatchableObject(list.get(i));
                if(data.getIndex() == 0)
                {
                    modifiers = (Byte)data.getValue();
                }
            }
            if((modifiers & 0x20) == 0)
            {
                return;
            }
            modifiers &= 0xdf;
            ArrayList<Object> newlist = new ArrayList<Object>();
            newlist.add(new WrappedWatchableObject(0, modifiers).getHandle());
            if(_alterName)
            {
                newlist.add(new WrappedWatchableObject(10, _namePrefix + "Placeholder").getHandle());
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
                        if(_alterName)
                        {
                            break;
                        }
                    default:
                        newlist.add(o);
                        break;
                }
            }
            mod.writeSafely(num, newlist);
            event.setPacket(packet);
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
    }
}