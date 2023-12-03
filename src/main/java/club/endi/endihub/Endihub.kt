package club.endi.endihub

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.plugin.java.JavaPlugin

class Endihub : JavaPlugin(), Listener {
    override fun onEnable() {
        // Listen for events
        server.pluginManager.registerEvents(this, this)
    }

    @EventHandler
    fun onBlockFormEvent(event: BlockFormEvent) {
        event.isCancelled = true
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
