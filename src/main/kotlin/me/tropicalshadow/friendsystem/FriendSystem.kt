package me.tropicalshadow.friendsystem

import me.tropicalshadow.friendsystem.commands.utils.DynamicCommand
import me.tropicalshadow.friendsystem.commands.utils.ShadowCommand
import me.tropicalshadow.friendsystem.config.ConfigManager
import me.tropicalshadow.friendsystem.listener.ShadowListener
import me.tropicalshadow.friendsystem.player.PlayerManager
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.reflections.Reflections

class FriendSystem: JavaPlugin() {

    lateinit var configManager: ConfigManager
    lateinit var playerManager: PlayerManager
    lateinit var miniMessages: MiniMessage

    var debug = false

    override fun onEnable(){
        configManager = ConfigManager(this)
        playerManager = PlayerManager(this)

        registerListeners()
        registerCommands()

        miniMessages = MiniMessage.miniMessage()
        logger.info("Plugin Enabled")
    }

    override fun onDisable() {

        logger.info("Plugin Disabled")
    }

    private fun registerListeners(){
        val packageName = javaClass.`package`.name
        for (clazz in Reflections("$packageName.listeners").getSubTypesOf(
            ShadowListener::class.java
        )){
            try{
                val listener: ShadowListener = clazz.getDeclaredConstructor(this::class.java).newInstance(this)
                if(debug) logger.info("Registering ${listener.javaClass.name.split(".").last()}")
                Bukkit.getPluginManager().registerEvents(listener, this)
                if(debug) logger.info("Registered ${listener.javaClass.name.split(".").last()}")
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
    private fun registerCommands() {
        val packageName = javaClass.getPackage().name
        for (clazz in Reflections("$packageName.commands").getSubTypesOf(
            ShadowCommand::class.java
        )) {
            try {
                val cmd: ShadowCommand = clazz.getDeclaredConstructor(this::class.java).newInstance(this)
                val cmdName: String = cmd.getCommandInfo().name
                if (cmdName.isEmpty()) continue
                var command = getCommand(cmdName)
                if (command == null) {
                    logger.info("Injecting Command: $cmdName")
                    if (! Bukkit.getCommandMap()
                            .register(cmd.getCommandInfo().name, name.lowercase(), DynamicCommand(cmd))) {
                        logger.info("Failed to add $cmdName to command map")
                        continue
                    }
                    command = getCommand(cmdName)
                    if (command == null) {
                        logger.info("Command $cmdName failed to inject")
                        continue
                    }
                }
                logger.info("Registering command $cmdName")
                command.setExecutor(cmd)
                command.tabCompleter = cmd
                if (cmd.getCommandInfo().permission.isEmpty()) command.permission = null else command.permission =
                    cmd.getCommandInfo().permission
                command.description = cmd.getCommandInfo().description
                command.usage = cmd.getCommandInfo().usage
                command.aliases = cmd.aliases
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}