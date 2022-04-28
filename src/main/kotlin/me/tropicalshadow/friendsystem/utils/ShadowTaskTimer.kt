package me.tropicalshadow.friendsystem.utils

import me.tropicalshadow.friendsystem.FriendSystem
import org.bukkit.scheduler.BukkitRunnable
import java.util.function.Consumer

class ShadowTaskTimer(var duration: Long,val onTick: Consumer<Long>? = null,val onEnd: Consumer<Long>? = null, val tickLast: Boolean = false): BukkitRunnable() {

    override fun run() {
        if(duration <= 0){
            cancel()
            if(tickLast)onTick?.accept(duration)
            onEnd?.accept(duration)
            return
        }
        onTick?.accept(duration)
        duration -= 1
    }

    companion object{
        lateinit var plugin: FriendSystem


        fun start(duration: Long, delay: Long = 0, period: Long = 20, onTick: Consumer<Long>? = null, onEnd: Consumer<Long>? = null, tickLast: Boolean = false): ShadowTaskTimer{
            val shadowTask = ShadowTaskTimer(duration, onTick, onEnd, tickLast)
            shadowTask.runTaskTimer(plugin, delay, period)
            return shadowTask
        }

        fun startAsync(duration: Long, delay: Long = 0, period: Long = 20, onTick: Consumer<Long>? = null, onEnd: Consumer<Long>? = null, tickLast: Boolean = false): ShadowTaskTimer{
            val shadowTask = ShadowTaskTimer(duration, onTick, onEnd, tickLast)
            shadowTask.runTaskTimerAsynchronously(plugin, delay, period)
            return shadowTask
        }

    }

}