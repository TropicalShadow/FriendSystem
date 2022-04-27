package me.tropicalshadow.friendsystem.commands

import me.tropicalshadow.friendsystem.FriendSystem
import me.tropicalshadow.friendsystem.commands.utils.ShadowCommand
import me.tropicalshadow.friendsystem.commands.utils.ShadowCommandAlias
import me.tropicalshadow.friendsystem.commands.utils.ShadowCommandInfo
import me.tropicalshadow.friendsystem.utils.Message
import me.tropicalshadow.friendsystem.utils.ResultType
import org.bukkit.Bukkit
import org.bukkit.entity.Player


@ShadowCommandInfo("friend", isPlayerOnly = true)
@ShadowCommandAlias(["f"])
class FriendCommand(plugin: FriendSystem) : ShadowCommand(plugin) {
// "/friend [list|gui|add|remove|cancel|accept]..."

    override fun execute(player: Player, args: Array<String>) {
        if(args.isEmpty())return player.sendMessage(this.usage)

        when(args.first()){
            "list", "gui" -> openGuiCommand(player, args.copyOfRange(1, args.size))//open friends list gui
            "add" -> addFriendCommand(player, args.copyOfRange(1,args.size))
            "remove" -> removeFriendCommand(player, args.copyOfRange(1,args.size))
            "cancel" -> cancelFriendRequestCommand(player, args.copyOfRange(1,args.size))
            "accept" -> acceptFriendRequestCommand(player, args.copyOfRange(1,args.size))
            else -> player.sendMessage(this.usage)
        }
    }

    private fun openGuiCommand(player: Player, args: Array<String>){

    }

    private fun acceptFriendRequestCommand(player: Player, args: Array<String>){
        if(args.isEmpty())return Message.UNKNOWN_PLAYER_GIVEN.sendMessage(player)

        val offlinePlayer = Bukkit.getOfflinePlayerIfCached(args.first())?: return Message.UNKNOWN_PLAYER_GIVEN.sendMessage(player)
        val response = plugin.playerManager.acceptFriendRequest(plugin.playerManager.getPlayer(player), offlinePlayer.uniqueId)
        response.message.sendMessage(player, Pair("%other%", offlinePlayer.name?:"Unknown"))
        val receiver = offlinePlayer.player?: return
        if(response.resultType != ResultType.SUCCESS)return
        val display = plugin.config.getString("friendRequestNotificationPostition","BOSSBAR")!!
        when(display.lowercase()){
            "chat" -> Message.FRIEND_REQUEST_ACCEPTED_NOTIFICATION.sendMessage(receiver, Pair("%other%",player.name))
            "actionbar" -> Message.FRIEND_REQUEST_ACCEPTED_NOTIFICATION.sendActionBar(receiver, Pair("%other%",player.name))
            else -> Message.FRIEND_REQUEST_ACCEPTED_NOTIFICATION.sendActionBar(receiver, Pair("%other%",player.name))
        }
    }

    private fun cancelFriendRequestCommand(player: Player, args: Array<String>){
        if(args.isEmpty())return Message.UNKNOWN_PLAYER_GIVEN.sendMessage(player)

        val offlinePlayer = Bukkit.getOfflinePlayerIfCached(args.first())?: return Message.UNKNOWN_PLAYER_GIVEN.sendMessage(player)
        val response = plugin.playerManager.cancelFriendRequest(plugin.playerManager.getPlayer(player), offlinePlayer.uniqueId)
        response.message.sendMessage(player, Pair("%other%", offlinePlayer.name?:"Unknown"))
        val receiver = offlinePlayer.player?: return
        if(response.resultType != ResultType.SUCCESS)return
        val display = plugin.config.getString("friendRequestNotificationPostition","BOSSBAR")!!
        when(display.lowercase()){
            "chat" -> Message.FRIEND_REQUEST_CANCELLED_NOTIFICATION.sendMessage(receiver, Pair("%other%",player.name))
            "actionbar" -> Message.FRIEND_REQUEST_CANCELLED_NOTIFICATION.sendActionBar(receiver, Pair("%other%",player.name))
            else -> Message.FRIEND_REQUEST_CANCELLED_NOTIFICATION.sendActionBar(receiver, Pair("%other%",player.name))
        }
    }

    private fun removeFriendCommand(player: Player, args: Array<String>){
        if(args.isEmpty())return Message.UNKNOWN_PLAYER_GIVEN.sendMessage(player)

        val offlinePlayer = Bukkit.getOfflinePlayerIfCached(args.first())?: return Message.UNKNOWN_PLAYER_GIVEN.sendMessage(player)
        val response = plugin.playerManager.removeFriend(plugin.playerManager.getPlayer(player), offlinePlayer.uniqueId)
        response.message.sendMessage(player, Pair("%other%", offlinePlayer.name?:"Unknown"))
        val receiver = offlinePlayer.player?: return
        if(response.resultType != ResultType.SUCCESS)return
        val display = plugin.config.getString("friendRequestNotificationPostition","BOSSBAR")!!
        when(display.lowercase()){
            "chat" -> Message.FRIEND_REMOVED_RECEIVED.sendMessage(receiver, Pair("%other%",player.name))
            "actionbar" -> Message.FRIEND_REMOVED_RECEIVED.sendActionBar(receiver, Pair("%other%",player.name))
            else -> Message.FRIEND_REMOVED_RECEIVED.sendActionBar(receiver, Pair("%other%",player.name))
        }
    }

    private fun addFriendCommand(player: Player, args: Array<String>){
        if(args.isEmpty())return Message.UNKNOWN_PLAYER_GIVEN.sendMessage(player)

        val offlinePlayer = Bukkit.getOfflinePlayerIfCached(args.first())?: return Message.UNKNOWN_PLAYER_GIVEN.sendMessage(player)
        val response = plugin.playerManager.sendFriendRequest(plugin.playerManager.getPlayer(player),offlinePlayer.uniqueId)
        response.message.sendMessage(player, Pair("%other%",offlinePlayer.name?:"Unknown"), Pair("%player%", player.name))
        val receiver = offlinePlayer.player?: return
        if(response.resultType != ResultType.SUCCESS )return
        val display = plugin.config.getString("friendRequestNotificationPostition","BOSSBAR")!!
        when (display.lowercase()){
            "chat" -> Message.FRIEND_REQUEST_RECEIVED.sendMessage(receiver, Pair("%other%",player.name))
            "actionbar" -> Message.FRIEND_REQUEST_RECEIVED.sendActionBar(receiver, Pair("%other%", player.name))
            else -> Message.FRIEND_REQUEST_RECEIVED.sendBossBar(receiver, Pair("%other%",player.name))
        }

    }
}