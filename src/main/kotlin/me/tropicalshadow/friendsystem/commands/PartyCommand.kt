package me.tropicalshadow.friendsystem.commands

import me.tropicalshadow.friendsystem.FriendSystem
import me.tropicalshadow.friendsystem.commands.utils.ShadowCommand
import me.tropicalshadow.friendsystem.commands.utils.ShadowCommandInfo
import org.bukkit.entity.Player

@ShadowCommandInfo("party", isPlayerOnly = true, permission = "friendsystem.party")
class PartyCommand(plugin: FriendSystem) : ShadowCommand(plugin) {
    // /party [info|invite|create|accept|decline|leave|disband]


    override fun execute(player: Player, args: Array<String>) {
        if(args.isEmpty())player.sendMessage(plugin.getCommand("party")!!.usage)


    }

    override fun tabComplete(player: Player, args: Array<String>): ArrayList<String> {
        return if(args.size <= 1)
            arrayListOf("info","invite","create","accept","decline","leave","disband")
        else
            arrayListOf("?")
    }



}