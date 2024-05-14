package club.endi.endihub

import club.endi.endihub.util.Text
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.Vector3F
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import io.papermc.paper.event.entity.EntityMoveEvent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.EnderPearl
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
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
import org.bukkit.util.BlockIterator
import org.joml.Vector3f
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
    val playerServerSelections = mutableMapOf<Player, String>()

    var protoman: ProtocolManager? = null
    override fun onEnable() {
        saveDefaultConfig()
        server.pluginManager.registerEvents(this, this)

        this.server.messenger.registerOutgoingPluginChannel(this, "BungeeCord");

        protoman = ProtocolLibrary.getProtocolManager();

        getCommand("survival")?.setExecutor { sender, command, label, args ->
            if (sender !is Player) {
                sender.sendMessage(Text.pre("&cVain pelaajat voivat käyttää tätä komentoa!"))
                return@setExecutor true
            }

            connectToServer(sender, "survival")
            true
        }

        getCommand("setupserver")?.setExecutor { sender, command, label, args ->
            if (sender !is Player) {
                sender.sendMessage(Text.pre("&(err)Vain pelaajat voivat käyttää tätä komentoa!"))
                return@setExecutor true
            }

            val displays = sender.world.getNearbyEntities(sender.location, 10.0, 10.0, 10.0)
                .filter { it.type == EntityType.TEXT_DISPLAY }
                .map { it as TextDisplay }

            if (args.size != 2) {
                for (display in displays) {
                    sender.sendMessage(Text.pre("&(primary)${display.text()} - ${display.x} ${display.y} ${display.z} - ${display.entityId}"))
                }
                return@setExecutor true
            }

            val display = displays.find { it.entityId == args[1].toInt() }

            if (display == null) {
                sender.sendMessage(Text.pre("&(err)Palvelin-nuolta ei löytynyt!"))
                return@setExecutor true
            }

            display.persistentDataContainer.set(
                org.bukkit.NamespacedKey(this, "server"),
                org.bukkit.persistence.PersistentDataType.STRING,
                args[0]
            )

            sender.sendMessage(Text.pre("&(primary-1)Palvelin-nuoli asetettu!"))
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


        val emptyItem = ItemStack(Material.PLAYER_HEAD, 1)
        emptyItem.itemMeta.displayName(Text.md("&7"))
        event.player.inventory.setItem(0, emptyItem)
        event.player.inventory.setItem(2, emptyItem)
        event.player.inventory.setItem(3, emptyItem)
        event.player.inventory.setItem(5, emptyItem)
        event.player.inventory.setItem(6, emptyItem)
        event.player.inventory.setItem(7, emptyItem)
        event.player.inventory.setItem(8, emptyItem)

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

        calculateArrow(event.player)
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

        if (event.item?.type == Material.PLAYER_HEAD) {
            if ((playerServerSelections[event.player] ?: "") == "") return
            connectToServer(event.player, playerServerSelections[event.player] ?: "")
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

        val pearlItem = ItemStack(Material.ENDER_PEARL, 1)
        shooter.inventory.setItem(1, pearlItem)

        event.entity.addPassenger(shooter)
        playerPearls[shooter] = event.entity as EnderPearl
    }

    @EventHandler
    fun onEntityDismountEvent(event: EntityDismountEvent) {
        event.dismounted.remove()
        playerPearls.remove(event.entity)
    }


    private val hitboxLocations = mapOf<String, String>(
        "241, 182, 233" to "survival",
        "241, 181, 233" to "survival",
        "241, 180, 233" to "survival",
        "240, 181, 233" to "survival",
        "242, 181, 233" to "survival",
        "241, 181, 232" to "survival",
        "241, 181, 234" to "survival",
        "241, 182, 234" to "survival",
        "242, 182, 234" to "survival",

        "232, 181, 235" to "event",
        "232, 181, 234" to "event",
        "232, 182, 235" to "event",
        "233, 181, 235" to "event",
        "232, 180, 235" to "event",
        "233, 180, 234" to "event",
    )

    private fun calculateArrow(player: Player) {
        hideArrows(player)
        val bIterator = BlockIterator(player, 30)
        var server = ""

        while (bIterator.hasNext()) {
            val block = bIterator.next()
            if (!hitboxLocations.containsKey("${block.x}, ${block.y}, ${block.z}")) continue
            server = hitboxLocations["${block.x}, ${block.y}, ${block.z}"] ?: ""
            break
        }

        player.sendMessage("Server: $server")

        if (server == "") {
            playerServerSelections[player] = ""

            player.sendTitle(
                "",
                "",
                0,
                1,
                0
            )
        }
        if (playerServerSelections[player] != server) {
            playerServerSelections[player] = server
            // send title
            player.sendTitle(
                "",
                "ꐙ",
                0,
                9999999,
                0
            )
            player.playSound(player.location, org.bukkit.Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.0f)
        }
        val arrow = getArrow(server, player.world, player.location) ?: return
        sendArrow(player, arrow, true)
    }

    private fun hideArrows(player: Player) {
        val nearbyDisplays = player.world.getNearbyEntities(player.location, 50.0, 50.0, 50.0)
            .filter { it.type == EntityType.TEXT_DISPLAY }
            .map { it as TextDisplay }

        for (display in nearbyDisplays) {
            if (display.persistentDataContainer.has(
                    org.bukkit.NamespacedKey(this, "server"),
                    org.bukkit.persistence.PersistentDataType.STRING
                )
            ) {
                sendArrow(player, display, false)
            }
        }
    }

    private fun sendArrow(player: Player, arrow: TextDisplay, visible: Boolean) {
        val fakeMetadata = PacketContainer(PacketType.Play.Server.ENTITY_METADATA)
        fakeMetadata.integers.writeSafely(0, arrow.entityId)
        val watcher = WrappedDataWatcher.getEntityWatcher(arrow).deepClone()
        val scaleSerializer = WrappedDataWatcher.Registry.getVectorSerializer()

        watcher.setObject(8, 0) // Interpolation delay
        watcher.setObject(9, 2) // Transformation interpolation duration
        watcher.setObject(12, scaleSerializer, Vector3f(4.0f, (if (visible) 4.0f else 0.0f), 4.0f)) // Scale

        val wrappedDataValueList = mutableListOf<WrappedDataValue>()
        for (entry in watcher.watchableObjects) {
            if (entry == null) continue
            val watcherObject = entry.watcherObject
            wrappedDataValueList.add(
                WrappedDataValue(
                    watcherObject.index,
                    watcherObject.serializer,
                    entry.rawValue
                )
            )
        }
        fakeMetadata.dataValueCollectionModifier.writeSafely(0, wrappedDataValueList)

        protoman?.sendServerPacket(player, fakeMetadata)
    }

    private fun connectToServer(player: Player, serverName: String) {
        player.addPotionEffect(
            org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.LEVITATION,
                1000000,
                7,
                false,
                false,
                false
            )
        )
        player.addPotionEffect(
            org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.DARKNESS,
                1000000,
                255,
                false,
                false,
                false
            )
        )
        player.addPotionEffect(
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
                out.writeUTF(serverName)
                player.sendPluginMessage(this, "BungeeCord", b.toByteArray())
                b.close()
                out.close()

            } catch (e: Exception) {
                player.sendMessage(Text.pre("&(err)Jotain meni pieleen! Sinua ei siirretty survivaliin."))
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.LEVITATION)
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.DARKNESS)
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY)

                player.teleport(player.world.spawnLocation)
            }
        }, 20L)
    }

    private fun getArrow(server: String, world: World, location: Location): TextDisplay? {
        val nearbyDisplays = world.getNearbyEntities(location, 15.0, 15.0, 15.0)
            .filter { it.type == EntityType.TEXT_DISPLAY }
            .map { it as TextDisplay }

        for (display in nearbyDisplays) {
            val pdc = display.persistentDataContainer.get(
                org.bukkit.NamespacedKey(this, "server"),
                org.bukkit.persistence.PersistentDataType.STRING
            ) == server

            if (pdc) {
                return display
            }
        }

        return null
    }
}
