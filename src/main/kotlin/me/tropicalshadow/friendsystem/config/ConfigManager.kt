package me.tropicalshadow.friendsystem.config

import me.tropicalshadow.friendsystem.FriendSystem
import me.tropicalshadow.friendsystem.player.ShadowPlayer
import me.tropicalshadow.friendsystem.utils.Message
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.*

class ConfigManager(private val plugin: FriendSystem) {

    val playerFolder = File(plugin.dataFolder, "players")
    val languageFile = File(plugin.dataFolder, "lang.yml")

    init{
        saveDefaultConfigs()
        plugin.reloadConfig()
        plugin.debug = plugin.config.getBoolean("debug")
        validatePlayerFolder()

    }

    private fun saveDefaultConfigs(){
        plugin.saveDefaultConfig()
        if(!languageFile.exists())Message.writeMessages(languageFile)
        Message.readMessages(languageFile)
    }

    private fun validatePlayerFolder(){
        if(!playerFolder.exists())
            playerFolder.mkdir()
    }

    fun userFile(uniqueId: UUID): File{
        return File(playerFolder, "$uniqueId.shadow")
    }

    fun saveBlankPlayer(uniqueId: UUID){
        if(!userFile(uniqueId).exists())savePlayer(ShadowPlayer(uniqueId))
    }
    fun savePlayerAndRemove(uniqueId: UUID){
        savePlayer(uniqueId)
        plugin.playerManager.removePlayer(uniqueId)
    }

    fun savePlayer(uniqueId: UUID){
        val shadowPlayer = plugin.playerManager.getPlayer(uniqueId)?: return
        savePlayer(shadowPlayer)
    }
    fun savePlayer(playerData: ShadowPlayer){
        val userFile = userFile(playerData.uniqueId)
        val config = YamlConfiguration.loadConfiguration(userFile)
        playerData.writeFile(config)
        config.save(userFile)
    }

    fun readPlayer(uniqueId: UUID): ShadowPlayer{
        val userFile = userFile(uniqueId)
        saveBlankPlayer(uniqueId)
        val config = YamlConfiguration.loadConfiguration(userFile)
        val wonderingPlayer = ShadowPlayer(uniqueId)
        wonderingPlayer.readFile(config)
        return wonderingPlayer
    }

    fun loadPlayer(bukkitPlayer: Player){
        loadPlayer(bukkitPlayer.uniqueId)
    }

    fun loadPlayer(uniqueId: UUID){
        plugin.playerManager.setPlayer(uniqueId, readPlayer(uniqueId))
    }

}