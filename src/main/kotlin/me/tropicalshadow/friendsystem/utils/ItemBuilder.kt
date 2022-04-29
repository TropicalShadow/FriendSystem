package me.tropicalshadow.friendsystem.utils

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class ItemBuilder(val material: Material) {

    private var name:String? = null
    private var customName: Component? = null
    private var lore: ArrayList<String> = ArrayList()
    private var size: Int = 1

    private var skullPlayer: OfflinePlayer? = null


    fun customName(customName: String) = apply { this.customName = Message.messageDeserializer(customName) }
    fun customName(customName: Component) = apply { this.customName = customName }
    fun stackSize(size: Int) = apply { this.size = size }
    fun skull(offlinePlayer: OfflinePlayer) = apply { this.skullPlayer = offlinePlayer }
    fun addLore(vararg lines: String) = apply { lore.addAll(lines) }
    fun addLore(lines: List<String>) = apply { lore.addAll(lines) }

    fun build(): ItemStack{
        val item = ItemStack(this.material, this.size)
        if(this.name != null)
            item.editMeta{ meta -> meta.displayName(Message.messageDeserializer(this.name!!))}
        if(this.customName != null)
            item.editMeta{ meta -> meta.displayName(customName!!)}
        if(lore.isNotEmpty())
            item.editMeta{ meta -> meta.lore(lore.map { Message.messageDeserializer(it) })}
        if(material == Material.PLAYER_HEAD && skullPlayer != null) {
            item.editMeta { meta -> (meta as SkullMeta).owningPlayer = skullPlayer }
            item.editMeta { meta -> (meta as SkullMeta).owner = skullPlayer?.name?: "Steve" }
        }
        return item
    }


}