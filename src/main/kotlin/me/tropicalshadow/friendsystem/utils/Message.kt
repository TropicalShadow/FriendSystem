package me.tropicalshadow.friendsystem.utils

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

enum class Message(val configLocation: String,val defaultDisplayName: String,val notificationPositional: MessagePosition = MessagePosition.CHAT) {
    DEFAULT("default", "Default"),

    //Friend Join / Leave
    FRIEND_JOINED("friend.joined", "<green>%other% has joined the game"),
    FRIEND_LEAVE("friend.left", "<red>%other% has left the game"),


    FRIENDS_ONLINE_JOIN_MESSAGE("friend.join.online_friends", "<green>%friends% friends online", MessagePosition.ACTIONBAR),
    NO_FRIENDS_ONLINE_JOIN_MESSAGE("friend.join.online_friends_non", "<green>No friends online", MessagePosition.ACTIONBAR),

    // Friend Request General Messages

    FRIEND_REQUEST_RECEIVED("friend.notification.friend_request", "<green>You received a friend request off %other%", MessagePosition.BOSSBAR),
    FRIEND_REMOVED_RECEIVED("friend.notification.friend_removed", "<red>%other% removed you as a friend", MessagePosition.BOSSBAR),
    FRIEND_REQUEST_CANCELLED_NOTIFICATION("friend.notification.cancelled_friend_request", "<red>%other% cancelled your friend request", MessagePosition.BOSSBAR),
    FRIEND_REQUEST_ACCEPTED_NOTIFICATION("friend.notification.accepted_friend_request", "<green>You are now friends with %other%", MessagePosition.BOSSBAR),
    FRIEND_REQUEST_DECLINED_NOTIFICATION("friend.notification.declined_friend_request", "<red>%other% doesn't want to be your friend, sucks to be you", MessagePosition.BOSSBAR),

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
        var message = messages["${this.configLocation}.text"]?: this.defaultDisplayName
        placeholders.forEach { message = message.replace(it.first,it.second) }
        message = message.replace("%player%", player.name)
        return messageDeserializer(message)
    }

    fun send(player: Player, vararg placeholders: Pair<String, String>){

        when(MessagePosition.getFromName(messages["${this.configLocation}.position"]?: this.notificationPositional.name)){
            MessagePosition.CHAT -> sendMessage(player, *placeholders)
            MessagePosition.ACTIONBAR -> sendActionBar(player, *placeholders)
            MessagePosition.BOSSBAR -> sendBossBar(player, *placeholders)
        }

    }

    fun sendMessage(player: CommandSender, vararg placeholders: Pair<String, String>){
        player.sendMessage(getComponent(player,*placeholders))

    }

    fun sendBossBar(player: Player, vararg placeholders: Pair<String, String>) {
        val bossBar = BossBar.bossBar(getComponent(player,*placeholders), 1f, BossBar.Color.GREEN,BossBar.Overlay.NOTCHED_6)
        player.showBossBar(bossBar)
        ShadowTaskTimer.start(6, period = 10, onTick = {
            bossBar.progress(it.toFloat().div(6f))
        }, onEnd = {
            player.hideBossBar(bossBar)
        })

    }

    fun sendActionBar(player: Player, vararg placeholders: Pair<String, String>){
        player.sendActionBar(getComponent(player, *placeholders))
    }

    companion object{
        val messages = HashMap<String, String>()

        fun messageDeserializer(input: String): ComponentLike{
            var string = input
            val colourMap = HashMap<String, String>()
            ChatColor.values().forEach { colour ->
                colourMap["<${colour.name.lowercase()}>"] = "&${colour.char}"
            }
            colourMap.forEach{ (colourSerialized, colourFormat) -> string = string.replace(colourSerialized, colourFormat)}
            return Component.text(
                ChatColor.translateAlternateColorCodes('&', string)
            )
        }

        fun writeMessages(file: File){
            val config = YamlConfiguration.loadConfiguration(file)
            values().forEach {
                if(!config.isSet("${it.configLocation}.text"))
                    config["${it.configLocation}.text"] = it.defaultDisplayName
                if(!config.isSet("${it.configLocation}.position"))
                    config["${it.configLocation}.position"] = it.notificationPositional.name
            }
            config.save(file)
        }

        fun readMessages(file: File){
            val config = YamlConfiguration.loadConfiguration(file)
            values().forEach {
                val message = config.getString("${it.configLocation}.text")
                if(message == null){
                    config.set("${it.configLocation}.text", it.defaultDisplayName)
                    messages["${it.configLocation}.text"] = it.defaultDisplayName
                }else{
                    messages["${it.configLocation}.text"] = message
                }

                messages["${it.configLocation}.position"] = MessagePosition.getFromName(config.getString("${it.configLocation}.position",  it.notificationPositional.name)!!).name

            }
            config.save(file)
        }
    }

}