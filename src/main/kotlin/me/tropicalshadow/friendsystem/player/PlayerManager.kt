package me.tropicalshadow.friendsystem.player

import com.google.common.collect.ImmutableMap
import me.tropicalshadow.friendsystem.FriendSystem
import me.tropicalshadow.friendsystem.party.Party
import me.tropicalshadow.friendsystem.party.PartyRequestResponse
import me.tropicalshadow.friendsystem.utils.Message
import me.tropicalshadow.friendsystem.utils.ResultType
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.lang.ref.WeakReference
import java.util.*

class PlayerManager(private val plugin: FriendSystem) {

    private val players = HashMap<UUID, ShadowPlayer>()
    private val parties = ArrayList<Party>()

    fun shutdown(){
        val configManager = plugin.configManager
        getPlayers().forEach{ (uuid, shadowPlayer) ->
            configManager.savePlayer(shadowPlayer)
            removePlayer(uuid)
        }
    }

    fun findInvitesForPlayer(player: UUID): List<Party>{
        return parties.filter { it.invites.contains(player) }
    }

    fun getPlayer(bukkitPlayer: Player): ShadowPlayer {
        return this.getPlayerOrTempLoad(bukkitPlayer.uniqueId)
    }

    fun getPlayer(unqiueId: UUID): ShadowPlayer?{
        return players[unqiueId]
    }

    fun getPlayerOrTempLoad(unqiueId: UUID): ShadowPlayer {
        return getPlayer(unqiueId)?: plugin.configManager.readPlayer(unqiueId)
    }

    fun setPlayer(unqiueId: UUID, wonderingPlayer: ShadowPlayer){
        players[unqiueId] = wonderingPlayer
    }

    fun removePlayer(unqiueId: UUID){
        players.remove(unqiueId)
    }

    fun getPlayers(): ImmutableMap<UUID, ShadowPlayer> {
        return ImmutableMap.copyOf(players)
    }

    fun createParty(creator: ShadowPlayer): PartyRequestResponse{
        if(creator.isInParty()) return PartyRequestResponse(ResultType.FAILED, Message.PARTY_ALREADY_IN_PARTY)
        val newParty = Party(creator.uniqueId)
        creator.party = WeakReference(newParty)
        parties.add(newParty)
        return PartyRequestResponse(ResultType.SUCCESS, Message.PARTY_CREATED)
    }

    fun inviteToParty(owner: ShadowPlayer, receiverUUID: UUID): PartyRequestResponse{
        if(!owner.isInParty())return PartyRequestResponse(ResultType.FAILED, Message.PARTY_NOT_IN_PARTY)
        if(!owner.isPartyOwner())return PartyRequestResponse(ResultType.FAILED, Message.PARTY_NOT_OWNER)
        if(owner.uniqueId == receiverUUID)return PartyRequestResponse(ResultType.FAILED, Message.PARTY_INVALID_INTERACTION)
        val party = owner.getParty()?: return PartyRequestResponse(ResultType.FAILED, Message.PARTY_INVITE_FAILED_TO_SEND)
        if(party.invites.contains(receiverUUID))return PartyRequestResponse(ResultType.FAILED, Message.PARTY_ALREADY_HAS_INVITE)
        if(party.members.contains(receiverUUID))return PartyRequestResponse(ResultType.FAILED, Message.PARTY_ALREADY_IN_PARTY)

        val receiver = Bukkit.getPlayer(receiverUUID)?: return PartyRequestResponse(ResultType.FAILED, Message.PARTY_INVITE_FAILED_TO_SEND)
        val receiverShadowPlayer = plugin.playerManager.getPlayer(receiverUUID)?: return PartyRequestResponse(ResultType.FAILED, Message.PARTY_INVITE_FAILED_TO_SEND)
        if(receiverShadowPlayer.getParty() != null)return PartyRequestResponse(ResultType.FAILED, Message.PARTY_THEY_ALREADY_IN_PARTY)

        party.sendInvite(receiverUUID)
        Message.PARTY_INVITE_RECEIVED.send(receiver, Pair("%other%", Bukkit.getOfflinePlayer(owner.uniqueId).name?:"Unknown"))
        return PartyRequestResponse(ResultType.SUCCESS, Message.PARTY_INVITE_SENT)
    }

    fun acceptPartyInvite(player: ShadowPlayer, partyOwner: UUID): PartyRequestResponse{
        return acceptPartyInvite(player, Bukkit.getOfflinePlayer(partyOwner).name?:"Unknown")
    }

    fun acceptPartyInvite(player: ShadowPlayer, partyOwner: String): PartyRequestResponse{
        if(partyOwner.isBlank())return PartyRequestResponse(ResultType.FAILED, Message.UNKNOWN_PLAYER_GIVEN)
        val offlinePartyOwner = Bukkit.getOfflinePlayerIfCached(partyOwner)?: return PartyRequestResponse(ResultType.FAILED, Message.PARTY_UNKNOWN_INVITE)
        if(player.isInParty()) return PartyRequestResponse(ResultType.FAILED, Message.PARTY_ALREADY_IN_PARTY)
        val party = parties.find { it.owner == offlinePartyOwner.uniqueId }?: return PartyRequestResponse(ResultType.FAILED, Message.PARTY_UNKNOWN_INVITE)
        if(!party.invites.contains(player.uniqueId)) return PartyRequestResponse(ResultType.FAILED, Message.PARTY_NOT_INVITED)
        party.sendPartyMessage(Message.PARTY_MEMBER_JOINED, Pair("%other%", Bukkit.getOfflinePlayer(player.uniqueId).name?:"Unknown"))
        party.members.add(player.uniqueId)
        player.party = WeakReference(party)
        party.invites.remove(player.uniqueId)
        plugin.playerManager.findInvitesForPlayer(player.uniqueId).forEach { it.invites.remove(player.uniqueId) }
        return PartyRequestResponse(ResultType.SUCCESS, Message.PARTY_JOINED)
    }

    fun declinePartyInvite(player: ShadowPlayer, partyLeader: OfflinePlayer): PartyRequestResponse {
        val party = parties.find { it.owner == partyLeader.uniqueId }?: return PartyRequestResponse(ResultType.FAILED, Message.PARTY_UNKNOWN_INVITE)
        if(!party.invites.contains(player.uniqueId)) return PartyRequestResponse(ResultType.FAILED, Message.PARTY_NOT_INVITED)
        party.invites.remove(player.uniqueId)
        return PartyRequestResponse(ResultType.SUCCESS, Message.PARTY_DECLINED)
    }

    fun declinePartyInvite(player: ShadowPlayer, partyOwner: String): PartyRequestResponse{
        if(partyOwner.isBlank())return PartyRequestResponse(ResultType.FAILED, Message.UNKNOWN_PLAYER_GIVEN)
        val offlinePartyOwner = Bukkit.getOfflinePlayerIfCached(partyOwner)?: return PartyRequestResponse(ResultType.FAILED, Message.PARTY_UNKNOWN_INVITE)
        return declinePartyInvite(player, offlinePartyOwner)
    }

    fun transferPartyToMember(player: ShadowPlayer, uuid: UUID): PartyRequestResponse{
        if(!player.isInParty()) return PartyRequestResponse(ResultType.FAILED, Message.PARTY_NOT_IN_PARTY)
        if(!player.isPartyOwner()) return PartyRequestResponse(ResultType.FAILED, Message.PARTY_NOT_OWNER)
        if(player.uniqueId == uuid)return PartyRequestResponse(ResultType.FAILED, Message.PARTY_INVALID_INTERACTION)

        val party = player.party.get()?: return PartyRequestResponse(ResultType.FAILED, Message.PARTY_INVALID_INTERACTION)
        if(!party.members.contains(uuid))return PartyRequestResponse(ResultType.FAILED, Message.PARTY_MEMBER_NOT_IN_PARTY)
        val oldOwner = party.owner
        party.owner = uuid
        party.members.remove(uuid)
        party.members.add(oldOwner)
        party.sendPartyMessage(Message.PARTY_TRANSFERRED, Pair("%from%", Bukkit.getOfflinePlayer(oldOwner).name?:"Unknown"), Pair("%to%",Bukkit.getOfflinePlayer(uuid).name?:"Unknown"))
        return PartyRequestResponse(ResultType.SUCCESS, Message.PARTY_TRANSFERRED_SUCCESS)
    }

    fun kickPartyMember(player: ShadowPlayer, uuid: UUID): PartyRequestResponse{
        if(!player.isInParty()) return PartyRequestResponse(ResultType.FAILED, Message.PARTY_NOT_IN_PARTY)
        if(!player.isPartyOwner()) return PartyRequestResponse(ResultType.FAILED, Message.PARTY_NOT_OWNER)
        if(player.uniqueId == uuid)return PartyRequestResponse(ResultType.FAILED, Message.PARTY_INVALID_INTERACTION)
        val kickedShadowPlayer = plugin.playerManager.getPlayer(uuid)?: return PartyRequestResponse(ResultType.FAILED, Message.PARTY_INVALID_INTERACTION)

        val party = player.party.get()?: return PartyRequestResponse(ResultType.FAILED, Message.PARTY_INVALID_INTERACTION)
        if(!party.members.contains(uuid))return PartyRequestResponse(ResultType.FAILED, Message.PARTY_MEMBER_NOT_IN_PARTY)
        party.members.remove(uuid)
        kickedShadowPlayer.party = WeakReference(null)
        val kicked = Bukkit.getPlayer(uuid)
        if(kicked != null)
            Message.PARTY_YOU_WAS_KICKED.send(kicked)
        party.sendPartyMessage(Message.PARTY_REMOVED_MEMBER, Pair("%other%", Bukkit.getOfflinePlayer(uuid).name?:"Unknown"), excludePlayers = arrayOf(player.uniqueId))
        return PartyRequestResponse(ResultType.SUCCESS, Message.PARTY_REMOVED_MEMBER)
    }

    fun leaveParty(player: ShadowPlayer): PartyRequestResponse{
        if(!player.isInParty())return PartyRequestResponse(ResultType.FAILED, Message.PARTY_NOT_IN_PARTY)
        val playerParty = player.party.get()?: return PartyRequestResponse(ResultType.FAILED, Message.PARTY_INVALID_INTERACTION)
        return if(playerParty.owner == player.uniqueId && playerParty.members.isEmpty()){
            parties.remove(playerParty)
            player.party = WeakReference(null)
            PartyRequestResponse(ResultType.SUCCESS, Message.PARTY_DISBANDED)
        }else if(playerParty.owner == player.uniqueId && playerParty.members.isNotEmpty()){
            val newLeader = playerParty.members.first()
            playerParty.owner = newLeader
            playerParty.members.remove(newLeader)
            playerParty.sendPartyMessage(Message.PARTY_TRANSFERRED, Pair("%from%", Bukkit.getOfflinePlayer(player.uniqueId).name?:"Unknown"), Pair("%to%",Bukkit.getOfflinePlayer(playerParty.members.first()).name?:"Unknown"))
            player.party = WeakReference(null)
            PartyRequestResponse(ResultType.SUCCESS, Message.PARTY_YOU_LEFT)
        }else{
            playerParty.members.remove(player.uniqueId)
            playerParty.sendPartyMessage(Message.PARTY_MEMBER_LEFT, Pair("%other%",Bukkit.getOfflinePlayer(player.uniqueId).name?:"Unknown"))
            player.party = WeakReference(null)
            PartyRequestResponse(ResultType.SUCCESS, Message.PARTY_YOU_LEFT)
        }
    }

    fun disbandParty(player: ShadowPlayer): PartyRequestResponse{
        if(!player.isInParty())return PartyRequestResponse(ResultType.FAILED, Message.PARTY_NOT_IN_PARTY)
        if(!player.isPartyOwner())return PartyRequestResponse(ResultType.FAILED, Message.PARTY_NOT_OWNER)

        val party = player.getParty()?: return PartyRequestResponse(ResultType.FAILED, Message.PARTY_INVALID_INTERACTION)

        party.sendPartyMessage(Message.PARTY_DISBANDED)
        party.members.forEach { plugin.playerManager.getPlayer(it)?.party = WeakReference(null) }
        parties.remove(party)
        player.party = WeakReference(null)
        return PartyRequestResponse(ResultType.SUCCESS, Message.PARTY_SUCCESSFULLY_DISBANDED)
    }

    fun sendFriendRequest(sender: ShadowPlayer, receieverUUID: UUID): FriendRequestResponse{
        if(sender.uniqueId == receieverUUID)return FriendRequestResponse(ResultType.FAILED, Message.CAN_NOT_ADD_YOURSELF)
        if(sender.friends.contains(receieverUUID))return FriendRequestResponse(ResultType.FAILED, Message.ALREADY_FRIENDS)
        if(sender.friendRequests.contains(receieverUUID))return FriendRequestResponse(ResultType.FAILED, Message.ALREADY_HAS_FRIEND_REQUEST)
        val receiever = getPlayerOrTempLoad(receieverUUID)
        if(receiever.friendRequests.contains(sender.uniqueId))return FriendRequestResponse(ResultType.FAILED, Message.ALREADY_SENT_FRIEND_REQUEST)
        receiever.friendRequests.add(sender.uniqueId)
        plugin.configManager.savePlayer(receiever)
        return FriendRequestResponse(ResultType.SUCCESS, Message.SENT_FRIEND_REQUEST)
    }

    fun cancelFriendRequest(sender: ShadowPlayer, receieverUUID: UUID): FriendRequestResponse{
        val receiever = getPlayerOrTempLoad(receieverUUID)
        if(!receiever.friendRequests.contains(sender.uniqueId))return FriendRequestResponse(ResultType.FAILED, Message.NO_FRIEND_REQUEST_TO_CANCEL)
        receiever.friendRequests.remove(sender.uniqueId)
        plugin.configManager.savePlayer(receiever)
        return FriendRequestResponse(ResultType.SUCCESS, Message.FRIEND_REQUEST_CANCELLED)
    }

    fun acceptFriendRequest(sender: ShadowPlayer, receieverUUID: UUID): FriendRequestResponse{
        if(!sender.friendRequests.contains(receieverUUID))return FriendRequestResponse(ResultType.FAILED, Message.NO_FRIEND_REQUEST_FROM)
        val receiever = getPlayerOrTempLoad(receieverUUID)
        receiever.friends.add(sender.uniqueId)
        sender.friends.add(receieverUUID)
        sender.friendRequests.remove(receieverUUID)
        plugin.configManager.savePlayer(receiever)
        plugin.configManager.savePlayer(sender)
        return FriendRequestResponse(ResultType.SUCCESS, Message.FRIEND_REQUEST_ACCEPTED)
    }

    fun declineFriendRequest(sender: ShadowPlayer, receieverUUID: UUID): FriendRequestResponse{
        if(!sender.friendRequests.contains(receieverUUID))return FriendRequestResponse(ResultType.FAILED, Message.NO_FRIEND_REQUEST_FROM)
        sender.friendRequests.remove(receieverUUID)
        plugin.configManager.savePlayer(sender)
        return FriendRequestResponse(ResultType.SUCCESS, Message.FRIEND_REQUEST_DECLINED)
    }

    fun removeFriend(sender: ShadowPlayer, receieverUUID: UUID): FriendRequestResponse{
        if(!sender.friends.contains(receieverUUID))return FriendRequestResponse(ResultType.FAILED, Message.YOU_ARE_NOT_FRIENDS)
        val receiver = getPlayerOrTempLoad(receieverUUID)
        receiver.friends.remove(sender.uniqueId)
        sender.friends.remove(receieverUUID)
        plugin.configManager.savePlayer(receiver)
        plugin.configManager.savePlayer(sender)
        return FriendRequestResponse(ResultType.SUCCESS, Message.FRIEND_REMOVED)
    }

}