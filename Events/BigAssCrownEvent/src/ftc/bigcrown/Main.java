package ftc.bigcrown;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.*;

public class Main extends JavaPlugin implements Listener {
	
	public static Main plugin;
	
	public void onEnable() {
		plugin = this;
		
		// Config
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		
		// Events
		getServer().getPluginManager().registerEvents(this, this);
		
		// Commands
		//getServer().getPluginCommand("command").setExecutor(new Command());
		
		// start repeating function to spawn presents
	}

	
	
	
	// Events:
	
	//@EventHandler
    // right click present
	
	
}
