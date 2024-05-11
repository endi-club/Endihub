package club.endi.endihub

import club.endi.endihub.util.Text
import io.papermc.paper.event.entity.EntityMoveEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.EnderPearl
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
import org.spigotmc.event.entity.EntityDismountEvent
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

fun sendActionbar(player: Player, bitcount: Int) {
    // &#4e5c24& <shift:-1>ꑀ<shift:-1>ꑀ<shift:-6>0<shift:-1>ꑀ<shift:-1>ꑀ<shift:-10>ꐻꑀ

    val neg1 = "ꐫ"
    val neg6 = "ꐬꐭꐫ"
    val neg10 = "ꐬꐮꐫ"
    val bg = "ꑀ"
    var actionbar = "&#4e5c24&$neg1$bg"
    val num = bitcount.toString()

    for (c in num) {
        actionbar += "$bg$neg6$c"
    }

    actionbar += "$bg$neg10"
    actionbar += "ꐻ"
    actionbar += bg

    player.sendActionBar(Text.md(actionbar))
}

class Endihub : JavaPlugin(), Listener {
    val playerPearls = mutableMapOf<Player, EnderPearl>()
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
            sender.addPotionEffect(
                org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.LEVITATION,
                    1000000,
                    7,
                    false,
                    false,
                    false
                )
            )
            sender.addPotionEffect(
                org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.DARKNESS,
                    1000000,
                    255,
                    false,
                    false,
                    false
                )
            )
            sender.addPotionEffect(
                org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.INVISIBILITY,
                    1000000,
                    255,
                    false,
                    false,
                    false
                )
            )

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

        getCommand("acbar")?.setExecutor { sender, command, label, args ->
            if (sender !is Player) {
                sender.sendMessage(Text.pre("&cVain pelaajat voivat käyttää tätä komentoa!"))
                return@setExecutor true
            }

            if (args.size != 1) {
                sender.sendMessage(Text.pre("&cKäytä: /acbar <bitcount>"))
                return@setExecutor true
            }

            val bitcount = args[0].toIntOrNull()

            if (bitcount == null) {
                sender.sendMessage(Text.pre("&cKäytä: /acbar <bitcount>"))
                return@setExecutor true
            }

            sendActionbar(sender, bitcount)

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

        val pearlItem = ItemStack(Material.ENDER_PEARL, 1)

        event.player.inventory.setItem(1, pearlItem)

        val cosmeticsItem = ItemStack(Material.ENDER_CHEST, 1)
        cosmeticsItem.lore(List(1) { Text.md("&e**ᴘʀɪɢʜᴛᴄʟɪᴄᴋ**: Avaa kosmeettikka valikko") })
        cosmeticsItem.itemMeta.displayName(Text.md("&(primary-1)Kosmeettikka"))
        event.player.inventory.setItem(4, cosmeticsItem)

        event.player.inventory.setItem(7, pearlItem)

        // send player a bossbar
        Bukkit.createBossBar(
            Text.mdlegacy("&#4e5c24&ꑉ"),
            org.bukkit.boss.BarColor.YELLOW,
            org.bukkit.boss.BarStyle.SOLID
        ).apply {
            addPlayer(event.player)
            isVisible = true
            progress = 0.0
        }
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

    @EventHandler
    fun onEntityMoveEvent(event: EntityMoveEvent) {
        if (event.entity.passengers.size == 0) return
        if (event.entity.passengers[0] !is Player) return
        val player = event.entity.passengers[0] as Player

        if (event.entity.location.x > config.getInt("x") ||
            event.entity.location.x < -config.getInt("x") ||
            event.entity.location.y > config.getInt("maxy") ||
            event.entity.location.y < config.getInt("miny") ||
            event.entity.location.z > config.getInt("z") ||
            event.entity.location.z < -config.getInt("z")
        ) {
            event.entity.fallDistance = 0F
            event.entity.teleport(player.world.spawnLocation)
        }
    }

    // cancellers
    @EventHandler
    fun onBlockPlaceEvent(event: BlockPlaceEvent) {
        event.isCancelled = !event.player.hasPermission("endihub.build")
    }

    @EventHandler
    fun onPlayerpickupItemEvent(event: PlayerPickupItemEvent) {
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
        event.isCancelled = !event.player.hasPermission("endihub.build")
    }

    @EventHandler
    fun onPlayerSwapHandItemsEvent(event: PlayerSwapHandItemsEvent) {
        event.isCancelled = !event.player.hasPermission("endihub.build")
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
        if (shooter !is Player) return
        if (event.entity.type != org.bukkit.entity.EntityType.ENDER_PEARL) return

        if (playerPearls[shooter] != null) {
            playerPearls[shooter]?.remove()
        }

        shooter.inventory.setItemInMainHand(ItemStack(Material.ENDER_PEARL, 1))
        event.entity.addPassenger(shooter)
        playerPearls[shooter] = event.entity as EnderPearl
    }

    @EventHandler
    fun onEntityDismountEvent(event: EntityDismountEvent) {
        event.dismounted.remove()
        playerPearls.remove(event.entity)
    }

}
