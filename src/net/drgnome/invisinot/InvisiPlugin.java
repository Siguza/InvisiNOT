// Bukkit Plugin "InvisiNOT" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.invisinot;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.entity.Player;
import static net.drgnome.invisinot.Global.*;

public class InvisiPlugin extends JavaPlugin implements Runnable, Listener
{
    private static final String _version = "1.0.0";
    
    private boolean _update = false;
    private int _upTick = 72000;
    
    public InvisiPlugin()
    {
        super();
        _plugin = this;
    }
    
    public void onEnable()
    {
        _log.info("Enabling InvisiNOT v" + _version);
        Config.reload();
        saveConfig();
        EffectListener.register();
        if(Config.bool("check-update"))
        {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, this, 0L, 1L);
            getServer().getPluginManager().registerEvents(this, this);
        }
    }
    
    public void onDisable()
    {
        EffectListener.unregister();
        _log.info("Disabling InvisiNOT v" + _version);
    }
    
    public void reloadConfig()
    {
        super.reloadConfig();
        Config.reload();
        saveConfig();
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handlePlayerLogin(PlayerLoginEvent event)
    {
        Player player = event.getPlayer();
        if(player.hasPermission("invisinot.update"))
        {
            sendMessage(player, "There is an update available for InvisiNOT.", ChatColor.YELLOW);
        }
    }
    
    public void run()
    {
        tick();
    }
    
    public void tick()
    {
        if(!_update)
        {
            _upTick++;
            if(_upTick >= 72000)
            {
                checkUpdate();
            }
        }
    }
    
    public boolean checkUpdate()
    {
        _update = hasUpdate("invisinot", _version);
        _upTick = 0;
        return _update;
    }
}