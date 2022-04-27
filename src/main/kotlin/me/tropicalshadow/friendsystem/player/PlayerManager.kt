package me.tropicalshadow.friendsystem.player

import com.google.common.collect.ImmutableMap
import me.tropicalshadow.friendsystem.FriendSystem
import me.tropicalshadow.friendsystem.utils.Message
import me.tropicalshadow.friendsystem.utils.ResultType
import org.bukkit.entity.Player
import java.util.*

class PlayerManager(private val plugin: FriendSystem) {

    private val players = HashMap<UUID, ShadowPlayer>()

    fun shutdown(){
        val configManager = plugin.configManager
        getPlayers().forEach{ (uuid, shadowPlayer) ->
            configManager.savePlayer(shadowPlayer)
            removePlayer(uuid)
        }
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

    fun sendFriendRequest(sender: ShadowPlayer, receieverUUID: UUID): FriendRequestResponse{
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