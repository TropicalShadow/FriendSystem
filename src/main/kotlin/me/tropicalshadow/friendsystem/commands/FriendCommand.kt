package me.tropicalshadow.friendsystem.commands

import me.tropicalshadow.friendsystem.FriendSystem
import me.tropicalshadow.friendsystem.commands.utils.ShadowCommand
import me.tropicalshadow.friendsystem.commands.utils.ShadowCommandAlias
import me.tropicalshadow.friendsystem.commands.utils.ShadowCommandInfo
import me.tropicalshadow.friendsystem.utils.Message
import me.tropicalshadow.friendsystem.utils.ResultType
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*


@ShadowCommandInfo("friend", isPlayerOnly = true, permission = "friendsystem.friend")
@ShadowCommandAlias(["f","friends"])
class FriendCommand(plugin: FriendSystem) : ShadowCommand(plugin) {
// "/friend [list|gui|add|remove|cancel|accept|decline]..."

    override fun tabComplete(player: Player, args: Array<String>): ArrayList<String> {
        val result = ArrayList<String>()
        val shadowPlayer = plugin.playerManager.getPlayer(player)
        if(args.size <= 1){
            result.addAll(listOf("list","gui","remove","add","accept","cancel"))
        }else if(args.size <= 2 && !listOf("list","gui").contains(args[1])){
            if(args[1].equals("remove",true))
                result.addAll(Bukkit.getOnlinePlayers().filter{ it.uniqueId != player.uniqueId}.filter{ shadowPlayer.isFriendsWith(it) }.map { it.name })
            else if(args[1].equals("add", true))
                result.addAll(Bukkit.getOnlinePlayers().filter{ it.uniqueId != player.uniqueId}.filter{ !shadowPlayer.isFriendsWith(it)}.map { it.name })
            else if(listOf("accept","decline").contains(args[1]))
                result.addAll(ArrayList(shadowPlayer.friendRequests.map { Bukkit.getOfflinePlayer(it).name }))
            else
                result.addAll(Bukkit.getOnlinePlayers().filter{ it.uniqueId != player.uniqueId}.map { it.name })
        }
        return result
    }

    override fun execute(player: Player, args: Array<String>) {
        if(args.isEmpty())return player.sendMessage(plugin.getCommand("friend")!!.usage)

        when(args.first()){
            "list", "gui" -> openGuiCommand(player)//open friends list gui
            "add" -> addFriendCommand(player, args.copyOfRange(1,args.size))
            "remove" -> removeFriendCommand(player, args.copyOfRange(1,args.size))
            "cancel" -> cancelFriendRequestCommand(player, args.copyOfRange(1,args.size))
            "accept" -> acceptFriendRequestCommand(player, args.copyOfRange(1,args.size))
            "decline" -> declineFriendRequestCommand(player, args.copyOfRange(1, args.size))
            "cheese" -> cheese(player, args.copyOfRange(1, args.size))
            else -> player.sendMessage(this.usage)
        }
    }


    private val cheeseCounter = HashMap<UUID, Int>()
    private fun cheese(player: Player, args: Array<String>){
        cheeseCounter[player.uniqueId] = (cheeseCounter[player.uniqueId]?: 0) + 1
        if((cheeseCounter[player.uniqueId] ?: 0) >= 15){
            if(args.isEmpty())return
            val otherPlayer = Bukkit.getPlayer(args.first())?: return
            if(player.uniqueId == otherPlayer.uniqueId)return
            val bossBar = BossBar.bossBar(Component.text("${player.name} is cheesed to meet you!"), 1f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS, setOf(BossBar.Flag.PLAY_BOSS_MUSIC, BossBar.Flag.CREATE_WORLD_FOG, BossBar.Flag.DARKEN_SCREEN))
            player.showBossBar(bossBar)
            cheeseCounter.remove(player.uniqueId)
        }
    }

    private fun openGuiCommand(player: Player){
        plugin.guiManager.openFriendsListGui(player)
    }

    private fun declineFriendRequestCommand(player: Player, args: Array<String>){
        if(args.isEmpty())return Message.UNKNOWN_PLAYER_GIVEN.sendMessage(player)

        val offlinePlayer = Bukkit.getOfflinePlayerIfCached(args.first())?: return Message.UNKNOWN_PLAYER_GIVEN.sendMessage(player)

        val response = plugin.playerManager.declineFriendRequest(plugin.playerManager.getPlayer(player), offlinePlayer.uniqueId)
        response.message.sendMessage(player, Pair("%other%", offlinePlayer.name?:"Unknown"))
        val receiver = offlinePlayer.player?: return
        if(response.resultType != ResultType.SUCCESS)return
        Message.FRIEND_REQUEST_DECLINED_NOTIFICATION.send(receiver, Pair("%other%",player.name))
    }

    private fun acceptFriendRequestCommand(player: Player, args: Array<String>){
        if(args.isEmpty())return Message.UNKNOWN_PLAYER_GIVEN.sendMessage(player)

        val offlinePlayer = Bukkit.getOfflinePlayerIfCached(args.first())?: return Message.UNKNOWN_PLAYER_GIVEN.sendMessage(player)
        val response = plugin.playerManager.acceptFriendRequest(plugin.playerManager.getPlayer(player), offlinePlayer.uniqueId)
        response.message.sendMessage(player, Pair("%other%", offlinePlayer.name?:"Unknown"))
        val receiver = offlinePlayer.player?: return
        if(response.resultType != ResultType.SUCCESS)return
        Message.FRIEND_REQUEST_ACCEPTED_NOTIFICATION.send(receiver, Pair("%other%",player.name))
    }

    private fun cancelFriendRequestCommand(player: Player, args: Array<String>){
        if(args.isEmpty())return Message.UNKNOWN_PLAYER_GIVEN.sendMessage(player)

        val offlinePlayer = Bukkit.getOfflinePlayerIfCached(args.first())?: return Message.UNKNOWN_PLAYER_GIVEN.sendMessage(player)
        val response = plugin.playerManager.cancelFriendRequest(plugin.playerManager.getPlayer(player), offlinePlayer.uniqueId)
        response.message.sendMessage(player, Pair("%other%", offlinePlayer.name?:"Unknown"))
        val receiver = offlinePlayer.player?: return
        if(response.resultType != ResultType.SUCCESS)return
        Message.FRIEND_REQUEST_CANCELLED_NOTIFICATION.send(receiver, Pair("%other%",player.name))
    }

    private fun removeFriendCommand(player: Player, args: Array<String>){
        if(args.isEmpty())return Message.UNKNOWN_PLAYER_GIVEN.sendMessage(player)

        val offlinePlayer = Bukkit.getOfflinePlayerIfCached(args.first())?: return Message.UNKNOWN_PLAYER_GIVEN.sendMessage(player)
        val response = plugin.playerManager.removeFriend(plugin.playerManager.getPlayer(player), offlinePlayer.uniqueId)
        response.message.sendMessage(player, Pair("%other%", offlinePlayer.name?:"Unknown"))
        val receiver = offlinePlayer.player?: return
        if(response.resultType != ResultType.SUCCESS)return
        Message.FRIEND_REMOVED_RECEIVED.send(receiver, Pair("%other%",player.name))
    }

    private fun addFriendCommand(player: Player, args: Array<String>){
        if(args.isEmpty())return Message.UNKNOWN_PLAYER_GIVEN.send(player)

        val offlinePlayer = Bukkit.getOfflinePlayerIfCached(args.first())?: return Message.UNKNOWN_PLAYER_GIVEN.send(player)
        if(player.uniqueId == offlinePlayer.uniqueId)return Message.CAN_NOT_ADD_YOURSELF.send(player)
        val response = plugin.playerManager.sendFriendRequest(plugin.playerManager.getPlayer(player),offlinePlayer.uniqueId)
        response.message.sendMessage(player, Pair("%other%",offlinePlayer.name?:"Unknown"), Pair("%player%", player.name))
        val receiver = offlinePlayer.player?: return
        if(response.resultType != ResultType.SUCCESS )return
        Message.FRIEND_REQUEST_RECEIVED.send(receiver, Pair("%other%",player.name))
    }
}