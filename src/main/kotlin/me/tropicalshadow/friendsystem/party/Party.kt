package me.tropicalshadow.friendsystem.party

import me.tropicalshadow.friendsystem.utils.Message
import me.tropicalshadow.friendsystem.utils.ShadowTaskTimer
import org.bukkit.Bukkit
import java.util.*

class Party(var owner: UUID, val members: ArrayList<UUID> = ArrayList()){


    val invites = ArrayList<UUID>()

    fun sendInvite(receiver: UUID){
        invites.add(receiver)
        ShadowTaskTimer(30, onEnd = {
            invites.remove(receiver)
            val receiverPlayer = Bukkit.getPlayer(receiver)?:return@ShadowTaskTimer
            Message.PARTY_INVITE_EXPIRE.send(receiverPlayer, Pair("%other%", Bukkit.getOfflinePlayer(owner).name?:"Unknown"))
        })
    }


    fun sendPartyMessage(message: Message, vararg placeholder: Pair<String, String>, excludePlayers: Array<UUID> = arrayOf()){
        val ownerPlayer = Bukkit.getPlayer(owner)
        if(ownerPlayer != null && !excludePlayers.contains(owner))
            message.send(ownerPlayer, *placeholder)
        members.filterNot { excludePlayers.contains(it) }.forEach { memberUUID ->
            val memberPlayer = Bukkit.getPlayer(memberUUID)
            if(memberPlayer != null)
                message.send(memberPlayer, *placeholder)

        }

    }

}
