package me.tropicalshadow.friendsystem.gui

import de.themoep.inventorygui.*
import me.tropicalshadow.friendsystem.FriendSystem
import me.tropicalshadow.friendsystem.party.Party
import me.tropicalshadow.friendsystem.utils.ItemBuilder
import me.tropicalshadow.friendsystem.utils.Message
import me.tropicalshadow.friendsystem.utils.ResultType
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Function


class GuiManager(private val plugin: FriendSystem) {

    // Util gui functions

    private fun addUtilItems(gui : InventoryGui, player: Player){
        gui.addElement(StaticGuiElement('q', ItemBuilder(Material.REDSTONE).customName(Message.GUI_FORCE_QUIT.getComponent(player, gui=gui)).build(), 1, GuiElement.Action { click -> click.gui.close(); return@Action true }))
        gui.addElement(StaticGuiElement('o', ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).customName(" ").build()))

    }

    private fun addArrows(gui : InventoryGui, player: Player){
        gui.addElement(GuiPageElement('f', ItemBuilder(Material.ARROW).customName(Message.GUI_GO_TO_FIRST.getComponent(player, gui=gui)).build(), GuiPageElement.PageAction.FIRST))
        gui.addElement(GuiPageElement('b', ItemBuilder(Material.OAK_SIGN).customName(Message.GUI_GO_TO_PREVIOUS.getComponent(player,gui=gui)).build(), GuiPageElement.PageAction.PREVIOUS ))
        gui.addElement(GuiPageElement('n', ItemBuilder(Material.OAK_SIGN).customName(Message.GUI_GO_TO_NEXT.getComponent(player,gui=gui)).build(), GuiPageElement.PageAction.NEXT))
        gui.addElement(GuiPageElement('l', ItemBuilder(Material.ARROW).customName(Message.GUI_GO_TO_LAST.getComponent(player,gui=gui)).build(), GuiPageElement.PageAction.LAST, ))

    }



    // Party System Gui functions

    fun partyInvites(player: Player){
        val shadowPlayer = plugin.playerManager.getPlayer(player)
        if(shadowPlayer.isInParty())return

        val gui = InventoryGui(plugin,"Party Invites", arrayOf("qoooioooo","opppppppo","opppppppo","opppppppo","oofbdnloo"))
        gui.title = Message.GUI_TITLE_PARTY_INVITES.getDisplayContent(player, gui=gui)
        gui.addElement('i', ItemBuilder(Material.NAME_TAG).customName(Message.GUI_ITEM_TITLE_PARTY_INVITES.getComponent(player,gui=gui)).build())

        val invitesList = GuiElementGroup('p')

        plugin.playerManager.findInvitesForPlayer(player.uniqueId).forEach { party: Party ->
            val owner = Bukkit.getOfflinePlayer(party.owner)
            invitesList.addElement(
                StaticGuiElement('e', ItemBuilder(Material.PLAYER_HEAD).skull(owner).addLore(Message.GUI_ITEM_LORE_PARTY_INVITE.getDisplayContent(player, gui=gui)).customName(Message.GUI_INVITE_LIST_PARTY_NAME.getComponent(player, gui=gui, placeholders= arrayOf(Pair("%owner%",owner.name?:"Unknown")))).build()
                    ,1 ,GuiElement.Action{ click ->
                        val response = plugin.playerManager.acceptPartyInvite(plugin.playerManager.getPlayer(click.whoClicked as Player), party.owner)
                        response.message.send(player)
                        if(response.resultType == ResultType.SUCCESS) click.gui.close()
                        return@Action true
                    }
                )
            )
        }

        gui.addElement(invitesList)
        gui.addElement(StaticGuiElement('d', ItemBuilder(Material.BARRIER).customName(Message.GUI_BACK_EXIT.getComponent(player, gui=gui)).build(), 1,
            GuiElement.Action { click ->
                click.gui.close()
                return@Action true
            }))
        addArrows(gui,player)
        addUtilItems(gui,player)
        gui.show(player)
    }

    fun partyManageMemberGui(player: Player, uuid: UUID){
        val shadowPlayer = plugin.playerManager.getPlayer(player)
        if(!shadowPlayer.isPartyOwner())return

        val partyMember = Bukkit.getOfflinePlayer(uuid)
        shadowPlayer.getParty()?: return
        if(!shadowPlayer.getParty()!!.members.contains(uuid))return

        val gui = InventoryGui(plugin,"Manage ${partyMember.name?:"Unknown"}", arrayOf("qoooioooo","ooxovonoo","oooodoooo"))
        gui.title = Message.GUI_TITLE_PARTY_MANAGE_MEMBER.getDisplayContent(player, gui=gui, placeholders= arrayOf(Pair("%other%", partyMember.name?:"Unknown")))
        gui.addElement(StaticGuiElement('i', ItemBuilder(Material.NAME_TAG).customName(Message.GUI_ITEM_TITLE_PARTY_MANAGE_MEMBER.getComponent(player, gui=gui)).build()))

        gui.addElement(StaticGuiElement('x', ItemBuilder(Material.RAIL).customName(Message.GUI_ITEM_TRANSFER_PARTY.getComponent(player, gui=gui)).build(), 1, GuiElement.Action {
            val response = plugin.playerManager.transferPartyToMember(plugin.playerManager.getPlayer(it.whoClicked as Player), uuid)
            if(response.resultType == ResultType.SUCCESS){
                it.gui.close()
            }
            response.message.send(player)
            return@Action true
        }))
        gui.addElement(StaticGuiElement('v', ItemBuilder(Material.PLAYER_HEAD).skull(partyMember).customName(Component.text(partyMember.name?:"Unknown")).build()))
        gui.addElement(StaticGuiElement('n', ItemBuilder(Material.STRUCTURE_VOID).customName(Message.GUI_ITEM_KICK_FROM_PARTY.getComponent(player, gui=gui)).build(), 1, GuiElement.Action { click ->
                val response = plugin.playerManager.kickPartyMember(plugin.playerManager.getPlayer(click.whoClicked as Player), uuid)
                response.message.send(player, Pair("%other%", Bukkit.getOfflinePlayer(uuid).name?:"Unknown"))
                click.gui.close()
                return@Action true
            }))
        gui.addElement(StaticGuiElement('d', ItemBuilder(Material.BARRIER).customName(Message.GUI_BACK_EXIT.getComponent(player, gui=gui)).build(), 1,
            GuiElement.Action { click ->
                val goneBack = InventoryGui.getHistory(click.whoClicked)
                goneBack.pollLast()
                if(goneBack.isEmpty())click.gui.close()
                if(goneBack.isNotEmpty()) partyListGui(click.whoClicked as Player)
                return@Action true
            }))
        addUtilItems(gui, player)
        gui.show(player)
    }

    fun partyListGui(player: Player){
        val shadowPlayer = plugin.playerManager.getPlayer(player)
        if(!shadowPlayer.isInParty())return
        val gui = InventoryGui(plugin, "Party List", arrayOf("qoooioooo","opppppppo","opppppppo","opppppppo","oofbdnloo"))
        gui.title = Message.GUI_TITLE_PARTY_LIST.getDisplayContent(player, gui=gui)
        gui.addElement(StaticGuiElement('i',
            ItemBuilder(Material.NAME_TAG)
                .customName(Message.GUI_ITEM_TITLE_PARTY_LIST.getComponent(player,gui=gui)).build()))

        val party = shadowPlayer.getParty()?:  return
        val partyMemberList = GuiElementGroup('p')
        val partyLeader = Bukkit.getOfflinePlayer(party.owner)

        partyMemberList.addElement(StaticGuiElement('e', ItemBuilder(Material.PLAYER_HEAD).skull(partyLeader).customName(partyLeader.name?: "Unknown").addLore(Message.PARTY_LEADER.getDisplayContent(player, gui=gui)).build()))
        party.members.forEach { friendUUID ->
            partyMemberList.addElement(
                DynamicGuiElement('e', Function { viewer ->
                    val friend = Bukkit.getOfflinePlayer(friendUUID)
                    val shadowClicker = plugin.playerManager.getPlayer(viewer as Player)

                    return@Function StaticGuiElement('e', ItemBuilder(Material.PLAYER_HEAD).skull(friend).customName(friend.name?: "Unknown").addLore(Message.PARTY_MEMBER.getDisplayContent(player,gui=gui), if(shadowClicker.isPartyOwner()) Message.GUI_ITEM_LORE_MANAGE_MEMBER.getDisplayContent(player, gui=gui) else "").build(),
                        GuiElement.Action { click ->
                            if(shadowClicker.isPartyOwner())
                                partyManageMemberGui(click.whoClicked as Player, friendUUID)
                            return@Action true
                        })
                })
            )
        }
        gui.addElement(partyMemberList)
        gui.addElement(StaticGuiElement('d', ItemBuilder(Material.BARRIER).customName(Message.GUI_BACK_EXIT.getComponent(player, gui=gui)).build(), 1,
            GuiElement.Action { click ->
                val goneBack = InventoryGui.getHistory(click.whoClicked)
                goneBack.pollLast()
                if(goneBack.isEmpty())click.gui.close()
                if(goneBack.isNotEmpty()) partyManagementGui(click.whoClicked as Player)
                return@Action true
            }))
        addArrows(gui, player)
        addUtilItems(gui, player)
        gui.show(player)

    }

    fun partyManagementGui(player: Player){
        val shadowPlayer = plugin.playerManager.getPlayer(player)
        val gui = InventoryGui(plugin, "Party Management", if(shadowPlayer.isInParty())  arrayOf("qoooioooo","ooxovonoo","oooodoooo") else arrayOf("qoooioooo","oooovoooo","oooodoooo"))
        gui.title = Message.GUI_TITLE_PARTY_MANAGEMENT.getDisplayContent(player, gui=gui)

        gui.addElement(StaticGuiElement('i',
            ItemBuilder(Material.NAME_TAG)
                .customName(Message.GUI_ITEM_TITLE_PARTY_MANAGEMENT.getComponent(player,gui=gui)).build()))

        if(!shadowPlayer.isInParty()){
            gui.addElement(StaticGuiElement('v', ItemBuilder(Material.LEAD).customName(Message.GUI_ITEM_CREATE_PARTY.getComponent(player, gui=gui)).build(), 1, GuiElement.Action { plugin.playerManager.createParty(shadowPlayer); partyManagementGui(it.whoClicked as Player); return@Action true }))
        }else{
            gui.addElement(StaticGuiElement('x', ItemBuilder(Material.BARRIER).customName(Message.GUI_ITEM_LEAVE_PARTY.getComponent(player, gui=gui)).build(), 1, GuiElement.Action { click ->
                plugin.playerManager.leaveParty(shadowPlayer)
                click.gui.close()
                return@Action true
            }))
            gui.addElement(StaticGuiElement('v', ItemBuilder(Material.LEAD).customName(Message.GUI_ITEM_VIEW_PARTY.getComponent(player, gui=gui)).build(), 1, GuiElement.Action { partyListGui(player); return@Action true }))
            if(shadowPlayer.isPartyOwner())
                gui.addElement(StaticGuiElement('n', ItemBuilder(Material.STRUCTURE_VOID).customName(Message.GUI_ITEM_DISBAND_PARTY.getComponent(player, gui=gui)).build(), 1, GuiElement.Action { plugin.playerManager.disbandParty(shadowPlayer); it.gui.close();return@Action true }))
            else
                gui.addElement(StaticGuiElement('n', ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).customName(" ").build()))
        }


        addUtilItems(gui,player)
        gui.addElement(StaticGuiElement('d', ItemBuilder(Material.BARRIER).customName(Message.GUI_BACK_EXIT.getComponent(player,gui=gui)).build(), 1,
            GuiElement.Action { click ->
                click.gui.close()
                return@Action true
            }))
        gui.show(player)
    }


    // Friend System Gui functions

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
        gui.addElement(StaticGuiElement('v', ItemBuilder(Material.PLAYER_HEAD).customName(Message.GUI_ITEM_VIEW_FRIEND.getComponent(player, gui=gui, placeholders = arrayOf(Pair("%friend%",friend.name?:"Unknown")))).skull(friend).build()))
        gui.addElement(StaticGuiElement('n', ItemBuilder(Material.FIREWORK_ROCKET).customName(Message.GUI_INVITE_PARTY.getComponent(player, gui=gui)).build(),1,GuiElement.Action { click ->
            val shadowPlayer = plugin.playerManager.getPlayer(click.whoClicked as Player)
            if(!shadowPlayer.isInParty()){
                val createPartyResponse = plugin.playerManager.createParty(shadowPlayer)
                createPartyResponse.message.send(player)
                if(createPartyResponse.resultType == ResultType.FAILED){
                    return@Action true
                }
            }
            val response = plugin.playerManager.inviteToParty(shadowPlayer,uuid)
            response.message.send(click.whoClicked as Player, Pair("%other%", Bukkit.getOfflinePlayer(uuid).name?:"Unknown"))
            return@Action true
        }))

        gui.addElement(StaticGuiElement('i',
            ItemBuilder(Material.NAME_TAG)
                .customName(Message.GUI_ITEM_TITLE_FRIEND_MANAGER.getComponent(player,gui=gui,placeholders = arrayOf(Pair("%friend%","${friend.name}"))
        )).build()))
        addUtilItems(gui, player)
        gui.addElement(StaticGuiElement('d', ItemBuilder(Material.BARRIER).customName(Message.GUI_BACK_EXIT.getComponent(player,gui=gui)).build(), 1,
            GuiElement.Action { click ->
                val goneBack = InventoryGui.getHistory(click.whoClicked)
                goneBack.pollLast()
                if(goneBack.isEmpty())click.gui.close()
                if(goneBack.isNotEmpty()) openFriendsListGui(click.whoClicked as Player)
                return@Action true
            }))
        gui.show(player, false)
    }


    fun openFriendsListGui(player: Player, isOnlineOnly: Boolean = false){
        val gui = InventoryGui(plugin, "Friends List", arrayOf("qoooimooo","opppppppo","opppppppo","opppppppo","oofbdnloo") )
        if(isOnlineOnly)
            gui.title = Message.GUI_TITLE_FRIENDS_LIST_ONLINE.getDisplayContent(player, gui=gui)
        else
            gui.title = Message.GUI_TITLE_FRIENDS_LIST.getDisplayContent(player, gui=gui)
        val friendsGroupElement = GuiElementGroup('p')
        friendsGroupElement.filler = StaticGuiElement(' ',ItemBuilder(Material.RED_STAINED_GLASS_PANE).customName(Message.GUI_NO_FRIEND_FILLER.getComponent(player,gui=gui)).build())
        val shadowPlayer = plugin.playerManager.getPlayer(player)
        val onlinePlayers = Bukkit.getOnlinePlayers().map{it.uniqueId}
        if(isOnlineOnly)
            gui.addElement(StaticGuiElement('m', ItemBuilder(Material.REDSTONE).customName(Message.GUI_ITEM_FRIENDS_ONLINE_OFF.getComponent(player, gui=gui)).build(), 1,
                GuiElement.Action{ click ->
                    click.gui.close()
                    openFriendsListGui(player, false)
                    return@Action true
                }
            ))
        else
            gui.addElement(StaticGuiElement('m', ItemBuilder(Material.GUNPOWDER).customName(Message.GUI_ITEM_FRIENDS_ONLINE_ON.getComponent(player, gui=gui)).build(), 1,
                GuiElement.Action { click ->
                    click.gui.close()
                    openFriendsListGui(player, true)
                    return@Action true
                }
            ))
        shadowPlayer.friends.filter{ !isOnlineOnly || onlinePlayers.contains(it)}.forEach { friendUUID ->
            friendsGroupElement.addElement(
                DynamicGuiElement('e', Function { _ ->
                    val friend = Bukkit.getOfflinePlayer(friendUUID)

                    return@Function StaticGuiElement('e', ItemBuilder(Material.PLAYER_HEAD).addLore(Message.GUI_ITEM_LORE_VIEW_FRIEND.getDisplayContent(player, gui=gui)).customName( Message.GUI_ITEM_VIEW_FRIEND.getComponent(player, gui=gui, placeholders=arrayOf(Pair("%friend%", friend.name?:"Unknown")))).skull(friend).build(),
                        GuiElement.Action { click ->
                            openFriendGui(click.whoClicked as Player, friendUUID)
                            return@Action true
                        })
                })
            )
        }

        gui.addElement(friendsGroupElement)
        gui.addElement(DynamicGuiElement('i', Function { viewer ->return@Function StaticGuiElement('i', ItemBuilder(Material.NAME_TAG).customName(Message.GUI_ITEM_TITLE_FRIENDS_LIST.getComponent(player, gui=gui)).stackSize(gui.getPageNumber(viewer)).build()) }))
        addUtilItems(gui, player)
        addArrows(gui, player)
        gui.addElement(StaticGuiElement('d', ItemBuilder(Material.BARRIER).customName(Message.GUI_BACK_EXIT.getComponent(player, gui=gui)).build(), 1,
            GuiElement.Action { click ->
                click.gui.close()
                return@Action true
            }))
         gui.show(player, false)


    }
}