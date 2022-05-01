package me.tropicalshadow.friendsystem.listener

import me.tropicalshadow.friendsystem.FriendSystem
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerQuitEvent

class PartyListener(plugin: FriendSystem) : ShadowListener(plugin) {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPartyMemberQuit(event: PlayerQuitEvent){
        val shadowPlayer = plugin.playerManager.getPlayer(event.player)
        shadowPlayer.getParty()?: return
        plugin.playerManager.leaveParty(shadowPlayer)
    }

    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent){
        if(!plugin.config.getBoolean("teleportPartyToLeaderOnWorldChange"))return
        val shadowPlayer = plugin.playerManager.getPlayer(event.player)
        val party = shadowPlayer.getParty()?:return
        if(party.owner != shadowPlayer.uniqueId)return
        val toWorldSpawn = plugin.config.getBoolean("teleportPartyToWorldSpawn", true)
        party.members.forEach {
            if(toWorldSpawn)
                Bukkit.getPlayer(it)?.teleportAsync(event.player.world.spawnLocation)
            else
                Bukkit.getPlayer(it)?.teleportAsync(event.player.location)
        }
    }

}