package me.tropicalshadow.friendsystem.gui

import de.themoep.inventorygui.*
import me.tropicalshadow.friendsystem.FriendSystem
import me.tropicalshadow.friendsystem.utils.ItemBuilder
import me.tropicalshadow.friendsystem.utils.Message
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Function


class GuiManager(private val plugin: FriendSystem) {

    fun openFriendGui(player:Player, uuid: UUID){
        val friend = Bukkit.getOfflinePlayer(uuid)
        val gui = InventoryGui(plugin, "${friend.name} Manager", arrayOf("qoooioooo","ozxcvbnmo","oooodoooo"))
        gui.title = Message.GUI_TITLE_FRIEND_MANAGER.getDisplayContent(player, gui=gui,placeholders = arrayOf(Pair("%friend%","${friend.name}")))

        gui.addElement(StaticGuiElement('x', ItemBuilder(Material.STRUCTURE_VOID).customName(Message.GUI_REMOVE_FRIEND.getComponent(player,gui=gui)).build(),1,
            GuiElement.Action { click ->
                plugin.playerManager.removeFriend(plugin.playerManager.getPlayer(player),uuid)
                val goneBack = InventoryGui.getHistory(click.whoClicked)
                goneBack.pollLast()
                if(goneBack.isEmpty())click.gui.close()
                if(goneBack.isNotEmpty()) openFriendsListGui(player)
                return@Action true
            }))
        gui.addElement(StaticGuiElement('v', ItemBuilder(Material.PLAYER_HEAD).customName(friend.name?:"Unknown").skull(friend).build()))
        gui.addElement(StaticGuiElement('n', ItemBuilder(Material.FIREWORK_ROCKET).customName(Message.GUI_INVITE_PARTY.getComponent(player, gui=gui)).build(),1,GuiElement.Action { click ->plugin.playerManager.inviteToParty(plugin.playerManager.getPlayer(player),uuid);return@Action true}))

        gui.addElement(StaticGuiElement('q', ItemBuilder(Material.REDSTONE).customName(Message.GUI_FORCE_QUIT.getComponent(player, gui=gui)).build(), 1, GuiElement.Action { click -> click.gui.close(); return@Action true }))
        gui.addElement(StaticGuiElement('i',
            ItemBuilder(Material.NAME_TAG)
                .customName(Message.GUI_ITEM_TITLE_FRIEND_MANAGER.getComponent(player,gui=gui,placeholders = arrayOf(Pair("%friend%","${friend.name}"))
        )).build()))
        gui.addElement(StaticGuiElement('o', ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).customName(" ").build()))
        gui.addElement(StaticGuiElement('d', ItemBuilder(Material.BARRIER).customName(Message.GUI_BACK_EXIT.getComponent(player,gui=gui)).build(), 1,
            GuiElement.Action { click ->
                val goneBack = InventoryGui.getHistory(click.whoClicked)
                goneBack.pollLast()
                if(goneBack.isEmpty())click.gui.close()
                if(goneBack.isNotEmpty()) openFriendsListGui(player)
                return@Action true
            }))
        gui.closeAction = InventoryGui.CloseAction { close -> return@CloseAction true }
        gui.show(player, false)
    }


    fun openFriendsListGui(player: Player){
        val gui = InventoryGui(plugin, "", arrayOf("qoooioooo","opppppppo","opppppppo","opppppppo","oofbdnloo") )
        gui.title = gui.replaceVars(player, Message.GUI_TITLE_FRIENDS_LIST.getDisplayContent(player, gui =gui))
        val friendsGroupElement = GuiElementGroup('p')
        friendsGroupElement.filler = StaticGuiElement(' ',ItemBuilder(Material.RED_STAINED_GLASS_PANE).customName(Message.GUI_NO_FRIEND_FILLER.getComponent(player,gui=gui)).build())
        val shadowPlayer = plugin.playerManager.getPlayer(player)
        shadowPlayer.friends.forEach { friendUUID ->
            friendsGroupElement.addElement(
                DynamicGuiElement('e', Function { viewer ->
                    val friend = Bukkit.getOfflinePlayer(friendUUID)

                    return@Function StaticGuiElement('e', ItemBuilder(Material.PLAYER_HEAD).skull(friend).build(),
                        GuiElement.Action { click ->
                            openFriendGui(click.whoClicked as Player, friendUUID)
                            return@Action true
                        },
                        friend.name,
                        "Click to view friend gui")
                })
            )
        }

        gui.addElement(friendsGroupElement)
        gui.addElement(StaticGuiElement('q', ItemBuilder(Material.REDSTONE).customName(Message.GUI_FORCE_QUIT.getComponent(player, gui=gui)).build(), 1, GuiElement.Action { click -> click.gui.close(); return@Action false }))
        gui.addElement(DynamicGuiElement('i', Function { viewer ->return@Function StaticGuiElement('i', ItemBuilder(Material.NAME_TAG).customName(Message.GUI_ITEM_TITLE_FRIENDS_LIST.getComponent(player, gui=gui)).stackSize(gui.getPageNumber(viewer)).build()) }))
        gui.addElement(StaticGuiElement('o', ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).customName(" ").build()))
        gui.addElement(GuiPageElement('f', ItemBuilder(Material.ARROW).customName(Message.GUI_GO_TO_FIRST.getComponent(player, gui=gui)).build(), GuiPageElement.PageAction.FIRST))
        gui.addElement(GuiPageElement('b', ItemBuilder(Material.OAK_SIGN).customName(Message.GUI_GO_TO_PREVIOUS.getComponent(player,gui=gui)).build(), GuiPageElement.PageAction.PREVIOUS ))
        gui.addElement(StaticGuiElement('d', ItemBuilder(Material.BARRIER).customName(Message.GUI_BACK_EXIT.getComponent(player, gui=gui)).build(), 1,
            GuiElement.Action { click ->
                click.gui.close()
                return@Action true
            }))
        gui.addElement(GuiPageElement('n', ItemBuilder(Material.OAK_SIGN).customName(Message.GUI_GO_TO_NEXT.getComponent(player,gui=gui)).build(), GuiPageElement.PageAction.NEXT))
        gui.addElement(GuiPageElement('l', ItemBuilder(Material.ARROW).customName(Message.GUI_GO_TO_LAST.getComponent(player,gui=gui)).build(), GuiPageElement.PageAction.LAST, ))
        gui.closeAction = InventoryGui.CloseAction { return@CloseAction false }
        gui.show(player, false)


    }
}