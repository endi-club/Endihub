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

            // get all blocks in 200 block radius
            val blocks = mutableListOf<Location>()

            for (x in -200..200) {
                for (y in -200..200) {
                    for (z in -200..200) {
                        blocks.add(Location(sender.world, x.toDouble(), y.toDouble(), z.toDouble()))
                    }
                }
            }

            for (block in blocks) {
                if (block.block.type == Material.AIR) continue
                // to the player, send the block as air
                sender.sendBlockChange(block, Material.AIR.createBlockData())
                // to the player, make a falling block at the block's location
                val fallingBlock = sender.world.spawnFallingBlock(block, block.block.blockData)
                fallingBlock.isVisibleByDefault = false
                sender.showEntity(this, fallingBlock)
            }

            // wait 2 seconds (using scheduler)
            server.scheduler.runTaskLater(this, Runnable {
                try {
                    val b: ByteArrayOutputStream = ByteArrayOutputStream()
                    val out = DataOutputStream(b)
                    out.writeUTF("Connect")
                    out.writeUTF("survival")
                    sender.sendPluginMessage(this, "BungeeCord", b.toByteArray())
                    b.close()
                    out.close()

                    server.scheduler.runTaskLater(this, Runnable {
                    }, 20L)

                } catch (e: Exception) {
                    sender.sendMessage(Text.pre("&cJotain meni pieleen! Sinua ei siirretty survivaliin."))
                }
            }, 40L)

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

        val compassItem = ItemStack(Material.DIRT, 1)
        compassItem.lore(List(1) { Text.md("&e**ᴘʀɪɢʜᴛᴄʟɪᴄᴋ**: Avaa pelaajan valikko")})

        event.player.inventory.setItem(1, compassItem)

        val cosmeticsItem = ItemStack(Material.ENDER_CHEST, 1)
        cosmeticsItem.lore(List(1) { Text.md("&e**ᴘʀɪɢʜᴛᴄʟɪᴄᴋ**: Avaa kosmeettikka valikko")})

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
            event.player.performCommand("kosmeetiikka")
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
                event.entity.addPassenger(shooter)
                shooter.sendMessage("§dWooosshh!")
            }
        }

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

}
