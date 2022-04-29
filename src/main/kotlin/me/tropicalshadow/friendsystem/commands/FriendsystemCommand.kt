package me.tropicalshadow.friendsystem.commands

import me.tropicalshadow.friendsystem.FriendSystem
import me.tropicalshadow.friendsystem.commands.utils.ShadowCommand
import me.tropicalshadow.friendsystem.commands.utils.ShadowCommandInfo
import org.bukkit.command.CommandSender

@ShadowCommandInfo("friendsystem", permission = "friendsystem.op")
class FriendsystemCommand(plugin: FriendSystem) : ShadowCommand(plugin) {


    override fun tabComplete(sender: CommandSender, args: Array<String>): ArrayList<String> {
        val output = ArrayList<String>()
        output.addAll(listOf("reloadplayers"))
        return output
    }

    override fun execute(sender: CommandSender, args: Array<String>) {
        if(args.isEmpty()) return sender.sendMessage("Nope")
        val first = args.first()
        when(first.lowercase()){
            "reloadplayers" -> reloadPlayers(sender)
        }
    }

    private fun reloadPlayers(sender: CommandSender){
        val players = plugin.playerManager.getPlayers()
        plugin.playerManager.getPlayers().keys.forEach {
            plugin.playerManager.removePlayer(it)
        }
        players.keys.forEach {
            plugin.configManager.loadPlayer(it)
        }
        sender.sendMessage("Players reloaded!")
    }

}