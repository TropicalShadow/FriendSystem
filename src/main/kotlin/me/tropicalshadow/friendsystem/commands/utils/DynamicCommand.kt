package me.tropicalshadow.friendsystem.commands.utils

import me.tropicalshadow.friendsystem.commands.utils.ShadowCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandSender


class DynamicCommand(command: ShadowCommand) :
    Command(command.getCommandInfo().name) {
    var command: ShadowCommand
    override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
        return command.onCommand(sender, this, commandLabel, args)
    }

    init {
        this.command = command
        aliases = command.aliases
    }
}
