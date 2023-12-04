package club.endi.endihub

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
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

        event.player.inventory.clear()

        val compassItem = Material.DIRT
        event.player.inventory.setItem(2, ItemStack(compassItem, 1))
        event.player.inventory.setItem(5, ItemStack(compassItem, 1))
        event.player.inventory.setItem(8, ItemStack(compassItem, 1))
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerMoveEvent(event: PlayerMoveEvent) {
        if (event.player.location.x > config.getInt("x") ||
            event.player.location.x < -config.getInt("x") ||
            event.player.location.y > config.getInt("maxy") ||
            event.player.location.y < config.getInt("miny") ||
            event.player.location.z > config.getInt("z") ||
            event.player.location.z < -config.getInt("z")
        ) {
            event.player.fallDistance = 0F
            event.player.teleport(event.player.world.spawnLocation)
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
