package me.tropicalshadow.friendsystem.commands.utils

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class ShadowCommandInfo(
    val name: String,
    val permission: String = "",
    val isPlayerOnly: Boolean = false,
    val permissionErr: String = "You don't chat permission to run this command",
    val isPlayerOnlyErr: String = "This command requires you to be a player to run",
    val description: String = "",
    val usage: String = "/<command>"
)
