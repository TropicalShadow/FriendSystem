package me.tropicalshadow.friendsystem.utils

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
// Chat Serialization https://webui.adventure.kyori.net/
enum class Message(val configLocation: String,val defaultDisplayName: String) {
    DEFAULT("default", "Default"),

    // Friend Request General Messages

    FRIEND_REQUEST_RECEIVED("friend.notification.friend_request", "<green>You received a friend request off %other%"),
    FRIEND_REMOVED_RECEIVED("friend.notification.friend_removed", "<red>%other% removed you as a friend"),
    FRIEND_REQUEST_CANCELLED_NOTIFICATION("friend.notification.cancelled_friend_request", "<red>%other% cancelled their friend request"),
    FRIEND_REQUEST_ACCEPTED_NOTIFICATION("friend.notification.cancelled_friend_request", "<green>%other% cancelled their friend request"),

    // FRIEND REQUEST MESSAGES

    ALREADY_SENT_FRIEND_REQUEST("friend.request.already_sent", "<red>You have already sent that person a friend request"),
    ALREADY_HAS_FRIEND_REQUEST("friend.request.already_has","<red>You already have a friend request from this player, accept it with /friend accept username"),
    SENT_FRIEND_REQUEST("friend.request.sent","<green>Friend request has been sent."),
    NO_FRIEND_REQUEST_TO_CANCEL("friend.request.no_request_to_cancel","<red>That player doesn't have a friend request from you."),
    FRIEND_REQUEST_CANCELLED("friend.request.cancelled", "<green>Friend request cancelled"),
    NO_FRIEND_REQUEST_FROM("friend.request.no_requests_from_player", "<red>You don't have a friend request form that player"),
    FRIEND_REQUEST_ACCEPTED("friend.request.accept", "<green>Friend request accepted"),
    FRIEND_REQUEST_DECLINED("friend.request.decline", "<red>Friend request declined"),
    ALREADY_FRIENDS("friend.request.already_friends", "<red>You are already friends"),
    YOU_ARE_NOT_FRIENDS("friend.you_are_not_friend", "<red>You are not friends"),
    FRIEND_REMOVED("friend.removed","<red>You removed them as a friend"),

    // FRIEND REQUEST COMMAND MESSAGES

    UNKNOWN_PLAYER_GIVEN("command.unknown_player", "<red>Unknown player given.")

    ;

    fun getComponent(player: CommandSender,vararg placeholders: Pair<String, String>): ComponentLike{
        var message = messages[this.configLocation]?: this.defaultDisplayName
        placeholders.forEach { message = message.replace(it.first,it.second) }
        message = message.replace("%player%", player.name)
        return miniMessages.deserialize(message)
    }

    fun sendMessage(player: CommandSender, vararg placeholders: Pair<String, String>){
        player.sendMessage(getComponent(player,*placeholders))

    }

    fun sendBossBar(player: Player, vararg placeholders: Pair<String, String>) {
        player.showBossBar(BossBar.bossBar(getComponent(player,*placeholders),1f, BossBar.Color.GREEN,BossBar.Overlay.PROGRESS))
    }

    fun sendActionBar(player: Player, vararg placeholders: Pair<String, String>){
        player.sendActionBar(getComponent(player, *placeholders))
    }

    companion object{
        val messages = HashMap<String, String>()
        lateinit var miniMessages: MiniMessage

        fun writeMessages(file: File){
            val config = YamlConfiguration.loadConfiguration(file)
            values().forEach {
                config[it.configLocation] = it.defaultDisplayName
            }
            config.save(file)
        }

        fun readMessages(file: File){
            val config = YamlConfiguration.loadConfiguration(file)
            values().forEach {
                val message = config.getString(it.configLocation)
                if(message == null){
                    config.set(it.configLocation, it.defaultDisplayName)
                }
                messages[it.configLocation] = it.defaultDisplayName
            }
            config.save(file)
        }
    }

}