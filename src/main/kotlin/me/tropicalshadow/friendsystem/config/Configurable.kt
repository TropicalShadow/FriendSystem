package me.tropicalshadow.friendsystem.config

import org.bukkit.configuration.file.YamlConfiguration

interface Configurable {
    fun writeFile(file: YamlConfiguration)
    fun readFile(file: YamlConfiguration)
}