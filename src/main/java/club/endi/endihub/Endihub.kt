package club.endi.endihub

import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin

class Endihub : JavaPlugin(), Listener {
    override fun onEnable() {
        saveDefaultConfig()
        server.pluginManager.registerEvents(this, this)
    }

    @EventHandler
    fun onBlockFormEvent(event: BlockFormEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        event.player.teleport(event.player.world.spawnLocation)
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerMoveEvent(event: PlayerMoveEvent) {
        if (event.player.location.x > config.getInt("x") ||
            event.player.location.x < -config.getInt("x") ||
            event.player.location.y > config.getInt("y") ||
            event.player.location.y < -config.getInt("y") ||
            event.player.location.z > config.getInt("z") ||
            event.player.location.z < -config.getInt("z")
        ) {
            event.player.teleport(event.player.world.spawnLocation)
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
