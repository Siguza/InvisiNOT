// Bukkit Plugin "InvisiNOT" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.invisinot;

import org.bukkit.configuration.file.*;
import static net.drgnome.invisinot.Global.*;

public class Config
{    
    private static FileConfiguration config;
    
    public static void reload()
    {
        config = _plugin.getConfig();
        setDefs();
    }
    
    private static void setDefs()
    {
        setDef("check-update", "true");
    }
    
    private static void setDef(String path, Object value)
    {
        if(!config.isSet(path))
        {
            config.set(path, value);
        }
    }
    
    public static boolean bool(String path)
    {
        return config.getString(path).equalsIgnoreCase("true");
    }
}