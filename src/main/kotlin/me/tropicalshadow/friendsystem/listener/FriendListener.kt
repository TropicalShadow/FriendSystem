package me.tropicalshadow.friendsystem.listener

import me.tropicalshadow.friendsystem.FriendSystem
import me.tropicalshadow.friendsystem.utils.Message
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class FriendListener(plugin: FriendSystem) : ShadowListener(plugin) {

    @EventHandler
    fun showFriendsOnJoin(event: PlayerJoinEvent){
        val shadowPlayer = plugin.playerManager.getPlayer(event.player)
        val friendsOnline = shadowPlayer.getOnlineFriends()

        if(friendsOnline.isEmpty())
            Message.NO_FRIENDS_ONLINE_JOIN_MESSAGE.send(event.player)
        else
            Message.FRIENDS_ONLINE_JOIN_MESSAGE.send(event.player, Pair("%friends%", friendsOnline.size.toString()))

        friendsOnline.forEach {
            Message.FRIEND_JOINED.send(it, Pair("%other%", event.player.name))
        }
    }

    @EventHandler
    fun showFriendsOnLeave(event: PlayerQuitEvent){
        val shadowPlayer = plugin.playerManager.getPlayer(event.player)
        val friendsOnline = shadowPlayer.getOnlineFriends()

        friendsOnline.forEach {
            Message.FRIEND_LEAVE.send(it, Pair("%other%", event.player.name))
        }
    }

}