// Bukkit Plugin "InvisiNOT" by Siguza
// Released under the CC BY-NC-SA 3.0 (CreativeCommons Attribution-NonCommercial-ShareAlike 3.0 Unported) license.
// The full license and a human-readable summary can be found at the following location:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.siguza.invisinot;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import com.comphenix.protocol.ProtocolLibrary;

public class InvisiPlugin extends JavaPlugin implements Runnable, Listener
{
    public static final String _version = "1.1.0";
    public static final int _projectID = 53546; // Bukkit
    public static final Logger _log = Logger.getLogger("Minecraft");
    private static InvisiPlugin _instance;
    private boolean _update = false;
    
    public static InvisiPlugin instance()
    {
        return _instance;
    }
    
    public InvisiPlugin()
    {
        _instance = this;
    }
    
    public void onEnable()
    {
        _log.info("Enabling InvisiNOT " + _version);
        MetaListener.register(getConfig().getString("name-prefix"));
        if(getConfig().getString("check-update").equalsIgnoreCase("true"))
        {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, this, 0L, 72000L);
            getServer().getPluginManager().registerEvents(this, this);
        }
    }
    
    public void onDisable()
    {
        MetaListener.unregister();
        if(!_update)
        {
            getServer().getScheduler().cancelTasks(this);
        }
        _log.info("Disabled InvisiNOT " + _version);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void handleJoin(PlayerJoinEvent event)
    {
        if(!_update)
        {
            return;
        }
        Player player = event.getPlayer();
        if(player.hasPermission("invisinot.update"))
        {
            player.sendMessage(ChatColor.YELLOW + "There is an update available for InvisiNOT.");
        }
    }
    
    public void run()
    {
        if(checkUpdate())
        {
            getServer().getScheduler().cancelTasks(this);
        }
    }
    
    public boolean checkUpdate()
    {
        _update = Util.hasUpdate(_projectID, _version);
        return _update;
    }
}