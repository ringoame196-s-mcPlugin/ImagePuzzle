package com.github.ringoame196_s_mcPlugin

import com.github.ringoame196_s_mcPlugin.commands.Command
import com.github.ringoame196_s_mcPlugin.events.ItemFrameEvents
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    private val plugin = this
    override fun onEnable() {
        super.onEnable()
        server.pluginManager.registerEvents(ItemFrameEvents(), plugin)
        val command = getCommand("imagepuzzle")
        command!!.setExecutor(Command(plugin))
    }
}
