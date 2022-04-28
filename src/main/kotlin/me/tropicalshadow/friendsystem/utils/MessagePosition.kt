package me.tropicalshadow.friendsystem.utils

enum class MessagePosition {
    BOSSBAR,
    ACTIONBAR,
    CHAT;

    companion object{

        fun getFromName(name: String): MessagePosition {
            return values().find { it.name.equals(name,true) }?: CHAT
        }

    }
}