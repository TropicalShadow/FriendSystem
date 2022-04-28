package me.tropicalshadow.friendsystem.player

import me.tropicalshadow.friendsystem.config.Configurable
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.util.*

data class ShadowPlayer(
    var uniqueId: UUID,
    val friends: ArrayList<UUID> = ArrayList(),
    val friendRequests: ArrayList<UUID> = ArrayList()
    ): Configurable {


    override fun writeFile(file: YamlConfiguration) {
        file["uniqueId"] = uniqueId.toString()
        file["friends"] = serializedUUIDList(friends)
        file["friendRequests"] = serializedUUIDList(friendRequests)
    }

    override fun readFile(file: YamlConfiguration) {
        uniqueId = UUID.fromString(file["uniqueId"] as String)
        loadSerializedUUIDS(file.getStringList("friends"), friends)
        loadSerializedUUIDS(file.getStringList("friendRequests"), friendRequests)
    }

    private fun serializedUUIDList(uuidList: ArrayList<UUID>): List<String>{
        return uuidList.map { it.toString() }
    }

    private fun loadSerializedUUIDS(serializedFriends: List<String>, toList: ArrayList<UUID>){
        toList.clear()
        toList.addAll(serializedFriends.map{ UUID.fromString(it) })
    }

    fun getBukkitPlayer(): Player?{
        return Bukkit.getPlayer(uniqueId)
    }

    fun getBukkitOfflinePlayer(): OfflinePlayer{
        return Bukkit.getOfflinePlayer(uniqueId)
    }

    fun getOnlineFriends(): List<Player>{
        return friends.filter { Bukkit.getPlayer(it) != null }.map{Bukkit.getPlayer(it)!!}
    }

    fun getOfflineFriends(): List<OfflinePlayer>{
        return friends.map { Bukkit.getOfflinePlayer(it) }
    }

    fun getOfflineFriendRequests(): List<OfflinePlayer>{
        return friendRequests.map{Bukkit.getOfflinePlayer(it)}
    }

    fun isFriendsWith(bukkitOfflinePlayer: OfflinePlayer): Boolean{
        return isFriendsWith(bukkitOfflinePlayer.uniqueId)
    }

    fun isFriendsWith(uuid: UUID): Boolean{
        return friends.contains(uuid)
    }

}