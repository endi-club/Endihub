package club.endi.endihub

import club.endi.endihub.util.Text
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream


class Endihub : JavaPlugin(), Listener {
    override fun onEnable() {
        saveDefaultConfig()
        server.pluginManager.registerEvents(this, this)

        this.server.messenger.registerOutgoingPluginChannel(this, "BungeeCord");

        getCommand("survival")?.setExecutor { sender, command, label, args ->
            if (sender !is Player) {
                sender.sendMessage(Text.pre("&cVain pelaajat voivat käyttää tätä komentoa!"))
                return@setExecutor true
            }

            // give the player levitation effect
            sender.addPotionEffect(org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.LEVITATION, 1000000, 7, false, false, false))
            sender.addPotionEffect(org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.DARKNESS, 1000000, 255, false, false, false))
            sender.addPotionEffect(org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY, 1000000, 255, false, false, false))

            server.scheduler.scheduleSyncDelayedTask(this, {
                try {
                    val b: ByteArrayOutputStream = ByteArrayOutputStream()
                    val out = DataOutputStream(b)
                    out.writeUTF("Connect")
                    out.writeUTF("survival")
                    sender.sendPluginMessage(this, "BungeeCord", b.toByteArray())
                    b.close()
                    out.close()

                } catch (e: Exception) {
                    sender.sendMessage(Text.pre("&cJotain meni pieleen! Sinua ei siirretty survivaliin."))
                    sender.removePotionEffect(org.bukkit.potion.PotionEffectType.LEVITATION)
                    sender.removePotionEffect(org.bukkit.potion.PotionEffectType.DARKNESS)
                    sender.removePotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY)

                    sender.teleport(sender.world.spawnLocation)
                }
            }, 20L)

            true
        }
    }

    @EventHandler
    fun onBlockFormEvent(event: BlockFormEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        event.player.teleport(event.player.world.spawnLocation)

        event.player.inventory.clear()

        // remove the player's effects
        event.player.activePotionEffects.forEach { effect ->
            event.player.removePotionEffect(effect.type)
        }

        // foodheal the player
        event.player.foodLevel = 20

        event.player.gameMode = org.bukkit.GameMode.ADVENTURE

        val compassItem = ItemStack(Material.ENDER_PEARL, 1)
        compassItem.lore(List(1) { Text.md("&7Testaa ja nauti!")})
        compassItem.asQuantity(99)
        compassItem.itemMeta.displayName(Text.md("&(primary-1)Ender Pearl"))

        event.player.inventory.setItem(1, compassItem)

        val cosmeticsItem = ItemStack(Material.ENDER_CHEST, 1)
        cosmeticsItem.lore(List(1) { Text.md("&e**ᴘʀɪɢʜᴛᴄʟɪᴄᴋ**: Avaa kosmeettikka valikko")})
        cosmeticsItem.itemMeta.displayName(Text.md("&(primary-1)Kosmeettikka"))
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
        if (event.item?.type == Material.ENDER_CHEST) {
            event.player.performCommand("kosmetiikka")
        }
    }

    // disallow item dragging
    @EventHandler
    fun onInventoryClickEvent(event: InventoryClickEvent) {
        if (event.inventory.holder is Player) {
            event.isCancelled = !(event.inventory.holder as Player).hasPermission("endihub.build")
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
                shooter.inventory.setItemInMainHand(shooter.inventory.itemInMainHand.asQuantity(99))
                event.entity.addPassenger(shooter)
                shooter.sendMessage("§dWooosshh!")
            }
        }

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

}
