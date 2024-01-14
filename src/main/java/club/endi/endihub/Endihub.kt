package club.endi.endihub

import club.endi.endihub.util.Text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
<<<<<<< HEAD
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
=======
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
>>>>>>> main
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

        val compassItem = ItemStack(Material.DIRT, 1)
        compassItem.lore(List(1) { Text.md("&e**ᴘʀɪɢʜᴛᴄʟɪᴄᴋ**: Avaa pelaajan valikko").color(NamedTextColor.GRAY) })

        event.player.inventory.setItem(1, compassItem)

        val cosmeticsItem = ItemStack(Material.CHEST, 1)
        cosmeticsItem.lore(List(1) { Component.text("&e**ᴘʀɪɢʜᴛᴄʟɪᴄᴋ**: Avaa kosmeettikka valikko").color(NamedTextColor.GRAY) })

        event.player.inventory.setItem(4, cosmeticsItem)

        event.player.inventory.setItem(7, compassItem)
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

    // cancellers
    @EventHandler
    fun onBlockPlaceEvent(event: BlockPlaceEvent) {
        event.isCancelled = !event.player.hasPermission("endihub.build")
    }

    @EventHandler
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        event.isCancelled = !event.player.hasPermission("endihub.build")
    }

    @EventHandler
    fun onBlockIgniteEvent(event: BlockIgniteEvent) {
        event.isCancelled = false
    }

    @EventHandler
    fun onSignChangeEvent(event: BlockPlaceEvent) {
        event.isCancelled = !event.player.hasPermission("endihub.build")
    }

    @EventHandler
    fun onPlayerDropItemEvent(event: PlayerDropItemEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerSwapHandItemsEvent(event: PlayerSwapHandItemsEvent) {
        event.isCancelled = true
    }

    // rightclick handler
    @EventHandler
    fun onPlayerInteractEvent(event: PlayerInteractEvent) {
        if (event.item?.type == Material.CHEST) {
            event.player.performCommand("kosmeettikka")
        }
    }

    // disallow item dragging
    @EventHandler
    fun onInventoryClickEvent(event: InventoryClickEvent) {
        if (event.inventory.holder is Player) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerDropItemEvent(event: BlockPlaceEvent) {
        event.isCancelled = !event.player.hasPermission("endihub.build")
    }

    @EventHandler
    fun onPlayerProjectileThrowEvent(event: ProjectileLaunchEvent) {
        val shooter = event.entity.shooter
        if (shooter is org.bukkit.entity.Player) {
            if (event.entity.type == org.bukkit.entity.EntityType.ENDER_PEARL) {
                event.entity.addPassenger(shooter)
                shooter.sendMessage("§dWooosshh!")
            }
        }

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

}
