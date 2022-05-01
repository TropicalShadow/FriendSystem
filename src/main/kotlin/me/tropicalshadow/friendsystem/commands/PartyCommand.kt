package me.tropicalshadow.friendsystem.commands

import me.tropicalshadow.friendsystem.FriendSystem
import me.tropicalshadow.friendsystem.commands.utils.ShadowCommand
import me.tropicalshadow.friendsystem.commands.utils.ShadowCommandAlias
import me.tropicalshadow.friendsystem.commands.utils.ShadowCommandInfo
import me.tropicalshadow.friendsystem.party.Party
import me.tropicalshadow.friendsystem.utils.Message
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@ShadowCommandInfo("party", isPlayerOnly = true, permission = "friendsystem.party")
@ShadowCommandAlias(["p"])
class PartyCommand(plugin: FriendSystem) : ShadowCommand(plugin) {
    // /party [gui|info|invite|create|accept|decline|leave|disband|kick]

    private fun allOnlinePlayersNotInParty(party: Party?, excludePlayer: Player? = null): List<String> {
        if(party == null)return Bukkit.getOnlinePlayers().filter { it.uniqueId != excludePlayer?.uniqueId }.map { it.name }
        return Bukkit.getOnlinePlayers().filter { it.uniqueId != excludePlayer?.uniqueId }.filterNot { party.members.contains(it.uniqueId) || party.owner == it.uniqueId }.map { it.name }
    }

    private fun allPlayersInParty(party: Party?, excludePlayer: Player? = null): List<String> {
        if(party == null)return Bukkit.getOnlinePlayers().filter { it.uniqueId != excludePlayer?.uniqueId }.map { it.name }
        return Bukkit.getOnlinePlayers().filter { it.uniqueId != excludePlayer?.uniqueId }.filter { party.members.contains(it.uniqueId) && party.owner == it.uniqueId }.map { it.name }
    }

    override fun tabComplete(player: Player, args: Array<String>): ArrayList<String> {
        val output = ArrayList<String>()
        val shadowPlayer = plugin.playerManager.getPlayer(player)
        if(args.size <= 1) {
            if (shadowPlayer.isInParty())
                output.addAll(arrayListOf("gui", "info", "list", "leave", "transfer"))
            else
                output.addAll(arrayListOf("invites", "create", "accept", "join", "decline"))
            if (shadowPlayer.isPartyOwner())
                output.addAll(arrayListOf("disband", "transfer", "kick", "invite", "kick"))
        }else if(args.size == 2) {
            when (args.first().lowercase()) {
                "invite" -> output.addAll(allOnlinePlayersNotInParty(shadowPlayer.getParty(), player))
                "transfer", "kick" -> output.addAll(allPlayersInParty(shadowPlayer.getParty(), player))
                "leave", "disband", "info", "gui", "list", "create", "invites" -> {
                }
                "join", "accept", "decline" -> plugin.playerManager.findInvitesForPlayer(player.uniqueId)
                    .map { Bukkit.getOfflinePlayer(it.owner).name ?: "Unknown" }
                else -> output.addAll(arrayListOf("?"))
            }
        }
        return output
    }

    override fun execute(player: Player, args: Array<String>) {
        if(args.isEmpty())return player.sendMessage(plugin.getCommand("party")!!.usage.replace("<command>","party"))

        when(args.first().lowercase()){
            "gui" -> partyInfoCommand(player)
            "info", "list" -> partyListCommand(player)
            "invite" -> invitePartyCommand(player, args.copyOfRange(1, args.size))
            "create" -> createPartyCommand(player)
            "accept","join" -> acceptPartyCommand(player, args.copyOfRange(1, args.size))
            "decline" -> declinePartyCommand(player, args.copyOfRange(1, args.size))
            "leave" -> leavePartyCommand(player)
            "disband" -> disbandPartyCommand(player)
            "transfer" -> transferPartyCommand(player, args.copyOfRange(1, args.size))
            "kick" -> kickFromPartyCommand(player, args.copyOfRange(1, args.size))
            "invites" -> partyInvitesCommand(player)
            else -> player.sendMessage(plugin.getCommand("party")!!.usage.replace("<command>","party"))
        }

    }

    private fun partyInvitesCommand(player: Player){
        val shadowPlayer = plugin.playerManager.getPlayer(player)
        if(shadowPlayer.isInParty())return Message.PARTY_ALREADY_IN_PARTY.send(player)
        plugin.guiManager.partyInvites(player)
    }

    private fun kickFromPartyCommand(player: Player, args: Array<String>){
        if(args.isEmpty())return Message.UNKNOWN_PLAYER_GIVEN.send(player)
        val shadowPlayer = plugin.playerManager.getPlayer(player)
        if(!shadowPlayer.isPartyOwner())return Message.PARTY_NOT_OWNER.send(player)
        val other = Bukkit.getOfflinePlayerIfCached(args.first())?: return Message.UNKNOWN_PLAYER_GIVEN.send(player)
        val response = plugin.playerManager.kickPartyMember(shadowPlayer, other.uniqueId)
        response.message.send(player, Pair("%other%", other.name?:"Unknown"))
    }

    private fun transferPartyCommand(player: Player, args: Array<String>){
        if(args.isEmpty())return Message.UNKNOWN_PLAYER_GIVEN.send(player)
        val shadowPlayer = plugin.playerManager.getPlayer(player)
        val other = Bukkit.getOfflinePlayerIfCached(args.first())?: return Message.UNKNOWN_PLAYER_GIVEN.send(player)
        val response = plugin.playerManager.transferPartyToMember(shadowPlayer, other.uniqueId)
        response.message.send(player)
    }

    private fun partyListCommand(player: Player){
        plugin.guiManager.partyListGui(player)
    }

    private fun partyInfoCommand(player: Player){
        plugin.guiManager.partyManagementGui(player)
    }

    private fun invitePartyCommand(player: Player, args: Array<String>){
        if(args.isEmpty())return Message.UNKNOWN_PLAYER_GIVEN.send(player)
        val receiver = Bukkit.getPlayer(args.first())?: return Message.UNKNOWN_PLAYER_GIVEN.send(player)
        val response = plugin.playerManager.inviteToParty(plugin.playerManager.getPlayer(player), receiver.uniqueId)
        response.message.send(player, Pair("%other%", receiver.name))
    }

    private fun createPartyCommand(player: Player){
        val response = plugin.playerManager.createParty(plugin.playerManager.getPlayer(player))
        response.message.send(player)
    }

    private fun acceptPartyCommand(player: Player, args: Array<String>){
        if(args.isEmpty())return Message.PARTY_UNKNOWN_INVITE.send(player)
        val response = plugin.playerManager.acceptPartyInvite(plugin.playerManager.getPlayer(player), args.joinToString(" ").trim())
        response.message.send(player)
    }

    private fun declinePartyCommand(player: Player, args: Array<String>){
        if(args.isEmpty())return Message.PARTY_UNKNOWN_INVITE.send(player)
        val response = plugin.playerManager.declinePartyInvite(plugin.playerManager.getPlayer(player), args.copyOfRange(1, args.size).joinToString(" ").trim())
        response.message.send(player)
    }

    private fun leavePartyCommand(player: Player){
        val response = plugin.playerManager.leaveParty(plugin.playerManager.getPlayer(player))
        response.message.send(player)
    }

    private fun disbandPartyCommand(player: Player){
        val response = plugin.playerManager.disbandParty(plugin.playerManager.getPlayer(player))
        response.message.send(player)

    }
}
