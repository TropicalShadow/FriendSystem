package me.tropicalshadow.friendsystem.listener

import me.tropicalshadow.friendsystem.FriendSystem
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class JoinLeaveListener(plugin: FriendSystem) : ShadowListener(plugin) {

    init {
        Bukkit.getOnlinePlayers().forEach {
            plugin.configManager.loadPlayer(it)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onJoin(event: PlayerJoinEvent){
        plugin.configManager.loadPlayer(event.player)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onQuit(event: PlayerQuitEvent){
        plugin.configManager.savePlayerAndRemove(event.player.uniqueId)
    }

}