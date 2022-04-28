package me.tropicalshadow.friendsystem.gui

import de.themoep.inventorygui.*
import me.tropicalshadow.friendsystem.FriendSystem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*
import java.util.function.Function


class GuiManager(private val plugin: FriendSystem) {

    fun openFriendGui(player:Player, uuid: UUID){

    }


    //TODO - Create a Simple gui for main menu, Friends list & Friend Gui
    fun openFriendsListGui(player: Player){
        val gui = InventoryGui(plugin, "Friends List", arrayOf("ooooioooo","ppppppppp","ppppppppp","ppppppppp","  fbdnl  ") )//TODO setup basic gui's for all friend related gui
        val friendsGroupElement = GuiElementGroup('p')
        val shadowPlayer = plugin.playerManager.getPlayer(player)
        shadowPlayer.friends.forEach { friendUUID ->
            friendsGroupElement.addElement(
                DynamicGuiElement('p', Function { viewer ->
                    val playerHead = ItemStack(Material.PLAYER_HEAD)
                    val friend = Bukkit.getOfflinePlayer(friendUUID)
                    playerHead.editMeta{ meta -> (meta as SkullMeta).owningPlayer = friend }
                    return@Function StaticGuiElement('p', ItemStack(Material.PLAYER_HEAD),
                        GuiElement.Action { click ->
                            openFriendGui(click.whoClicked as Player, friendUUID)
                            return@Action true
                        },
                        friend.name,
                        "Click to view friend gui")
                })
            )
        }
        gui.addElement(GuiPageElement('f', ItemStack(Material.ARROW), GuiPageElement.PageAction.FIRST, "Go to first page (current: %page%)"))
        gui.addElement(GuiPageElement('b', ItemStack(Material.OAK_SIGN), GuiPageElement.PageAction.PREVIOUS, "Go to previous page (%prevpage%)"))
        gui.addElement(StaticGuiElement('d', ItemStack(Material.BARRIER), 1,
            GuiElement.Action { click ->
                click.gui.close()
                return@Action true
            }))
        gui.addElement(GuiPageElement('n', ItemStack(Material.OAK_SIGN), GuiPageElement.PageAction.NEXT, "Go to next page (%nextpage%)"))
        gui.addElement(GuiPageElement('l', ItemStack(Material.ARROW), GuiPageElement.PageAction.LAST, "Go to last page (%pages%)"))
        gui.show(player)


    }
}