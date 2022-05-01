package me.tropicalshadow.friendsystem

import me.tropicalshadow.friendsystem.commands.utils.DynamicCommand
import me.tropicalshadow.friendsystem.commands.utils.ShadowCommand
import me.tropicalshadow.friendsystem.config.ConfigManager
import me.tropicalshadow.friendsystem.gui.GuiManager
import me.tropicalshadow.friendsystem.listener.ShadowListener
import me.tropicalshadow.friendsystem.player.PlayerManager
import me.tropicalshadow.friendsystem.utils.ShadowTaskTimer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import org.reflections.Reflections


class FriendSystem: JavaPlugin() {

    lateinit var configManager: ConfigManager
    lateinit var playerManager: PlayerManager
    lateinit var guiManager: GuiManager

    var debug = false
    val cheese: Boolean = true

    override fun onEnable(){
        ShadowTaskTimer.plugin = this
        if(validateIntegrate())return
        configManager = ConfigManager(this)
        playerManager = PlayerManager(this)
        guiManager = GuiManager(this)

        registerListeners()
        registerCommands()


        logger.info("Plugin Enabled")
    }

    override fun onDisable() {
        HandlerList.unregisterAll(this)
        playerManager.shutdown()
        logger.info("Plugin Disabled")
    }

    private fun validateIntegrate(): Boolean{
        return try{
            val correct = !this::class.java.getDeclaredField("cheese").getBoolean(this)
            if(correct) {
                logger.info("Failed to validate integrate. To fix don't change any cheese code. if you touch cheese code it will break for some reason!")
                Bukkit.getPluginManager().disablePlugin(this)
                Bukkit.broadcast(Component.text("No on fucks with cheese variable!!!", NamedTextColor.RED))
                ShadowTaskTimer.start(3, onEnd = {Bukkit.shutdown()})
            }
            correct
        }catch(e: Exception){
            true
        }

    }

    private fun registerListeners(){
        val packageName = javaClass.`package`.name
        for (clazz in Reflections("$packageName.listener").getSubTypesOf(
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
                if(cmd.getCommandInfo().description.isNotBlank())
                    command.description = cmd.getCommandInfo().description
                if( cmd.getCommandInfo().usage != "/<command>")
                    command.usage = cmd.getCommandInfo().usage
                command.aliases = cmd.aliases
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}