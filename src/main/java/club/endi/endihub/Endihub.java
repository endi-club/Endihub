package club.endi.endihub;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Endihub extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Listen for events
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onBlockFormEvent(BlockFormEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
