package me.tropicalshadow.friendsystem.utils

import de.themoep.inventorygui.InventoryGui
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import java.io.File

enum class Message(val configLocation: String,val defaultDisplayName: String,val notificationPositional: MessagePosition = MessagePosition.CHAT) {
    DEFAULT("default", "Default"),

    // Party Messages
    PARTY_INVITE_SENT("party.invite_sent", "<green>You sent an invite to %other%", MessagePosition.ACTIONBAR),
    PARTY_INVITE_RECEIVED("party.invite_received", "<green>You received a party invite from %other%", MessagePosition.ACTIONBAR),
    PARTY_INVITE_FAILED_TO_SEND("party.invite_failed_to_send", "<red>Party invite failed to send", MessagePosition.CHAT),
    PARTY_DISBANDED("party.disbanded", "<red>Party was disbanded",MessagePosition.ACTIONBAR),
    PARTY_TRANSFERED("party.transfered", "<blue>Party was transfered from %from% to %to%",MessagePosition.ACTIONBAR),
    PARTY_MEMBER_LEFT("party.left_member", "<red>%other% left the party.",MessagePosition.ACTIONBAR),
    PARTY_NOT_OWNER("party.not_owner", "<red>You are not owner of the current party",MessagePosition.ACTIONBAR),
    PARTY_NOT_IN_PARTY("party.not_in_party", "<red>You are not currently in a party",MessagePosition.ACTIONBAR),
    PARTY_SUCCESSFULLY_DISBANDED("party.successfully_disbanded","<green>Successfully disbanded party",MessagePosition.ACTIONBAR),
    PARTY_INVITE_EXPIRE("party.invite_expired","<red>party invite from %other% expired",MessagePosition.ACTIONBAR),
    PARTY_UNKNOWN_INVITE("party.unknown_invite", "<red>Unknown party invite", MessagePosition.CHAT),
    PARTY_NOT_INVITED("party.not_invited","<red>You was not invited into this party", MessagePosition.CHAT),
    PARTY_JOINED("party.joined", "<green>You joined a party",MessagePosition.CHAT),
    PARTY_ALREADY_IN_PARTY("party.already_in_party", "<red>You are already in a party",MessagePosition.CHAT),
    PARTY_DECLINED("party.declined","<green>Declined party invite",MessagePosition.CHAT),
    PARTY_CREATED("party.created", "<green>Created a new party", MessagePosition.CHAT),

    // GUI Messages
    GUI_FORCE_QUIT("gui.force_quit", "<red>Force Quit", MessagePosition.NON),
    GUI_BACK_EXIT("gui.back_exit", "<red>Back / Exit", MessagePosition.NON),
    GUI_GO_TO_FIRST("gui.go_to_first", "<white>Go to first page (current: %page%)", MessagePosition.NON),
    GUI_GO_TO_LAST("gui.go_to_last","<white>Go to last page (%pages%)",MessagePosition.NON),
    GUI_GO_TO_PREVIOUS("gui.go_to_previous", "<white>Go to previous (page %prevpage%)", MessagePosition.NON),
    GUI_GO_TO_NEXT("gui.go_to_next", "<white>Go to next (page %nextpage%)", MessagePosition.NON),
    GUI_NO_FRIEND_FILLER("gui.no_friend_filler", "<red>No More Friends", MessagePosition.NON),
    GUI_TITLE_FRIENDS_LIST("gui.title.friends_list", "Friends List", MessagePosition.NON),
    GUI_ITEM_TITLE_FRIENDS_LIST("gui.item.friends_list_title", "%title%", MessagePosition.NON),
    GUI_TITLE_FRIEND_MANAGER("gui.title.friend_manager", "%friend% Manager", MessagePosition.NON),
    GUI_ITEM_TITLE_FRIEND_MANAGER("gui.item.friend_manager_title", "%title%", MessagePosition.NON),
    GUI_REMOVE_FRIEND("gui.remove_friend", "<red>Remove Friend", MessagePosition.NON),
    GUI_INVITE_PARTY("gui.invite_party", "Invite to party", MessagePosition.NON),

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

    UNKNOWN_PLAYER_GIVEN("command.unknown_player", "<red>Unknown player given."),
    CAN_NOT_ADD_YOURSELF("command.can_not_add_yourself", "<red>You can't send yourself a friend request"),

    ;

    fun getDisplayContent(player: CommandSender, vararg placeholders: Pair<String, String>, gui: InventoryGui? = null): String{
        var message = messages["${this.configLocation}.text"]?: this.defaultDisplayName
        placeholders.forEach { message = message.replace(it.first,it.second) }
        if(player is HumanEntity && gui != null)
            message = gui.replaceVars(player, message)
        message = message.replace("%player%", player.name)
        return message
    }

    fun getComponent(player: CommandSender,vararg placeholders: Pair<String, String>, gui: InventoryGui? = null): Component{
        return messageDeserializer(getDisplayContent(player, *placeholders, gui =gui))
    }

    fun send(player: Player, vararg placeholders: Pair<String, String>){

        when(MessagePosition.getFromName(messages["${this.configLocation}.position"]?: this.notificationPositional.name)){
            MessagePosition.CHAT -> sendMessage(player, *placeholders)
            MessagePosition.ACTIONBAR -> sendActionBar(player, *placeholders)
            MessagePosition.BOSSBAR -> sendBossBar(player, *placeholders)
            else -> return
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

        fun messageDeserializer(input: String): Component{
            var string = input
            val colourMap = HashMap<String, String>()
            ChatColor.values().forEach { colour ->
                colourMap["<${colour.name.lowercase()}>"] = "&${colour.char}"
            }
            colourMap.forEach{ (colourSerialized, colourFormat) -> string = string.replace(colourSerialized, colourFormat)}
            return Component.text(ChatColor.translateAlternateColorCodes('&', string)).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        }

        fun writeMessages(file: File){
            val config = YamlConfiguration.loadConfiguration(file)
            values().forEach {
                if(!config.isSet("${it.configLocation}.text"))
                    config["${it.configLocation}.text"] = it.defaultDisplayName
                if(!config.isSet("${it.configLocation}.position") && it.notificationPositional != MessagePosition.NON)
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
                if(it.notificationPositional != MessagePosition.NON)
                    messages["${it.configLocation}.position"] = MessagePosition.getFromName(config.getString("${it.configLocation}.position",  it.notificationPositional.name)!!).name
                else
                    messages["${it.configLocation}.position"] = MessagePosition.NON.name
            }
            config.save(file)
        }
    }

}