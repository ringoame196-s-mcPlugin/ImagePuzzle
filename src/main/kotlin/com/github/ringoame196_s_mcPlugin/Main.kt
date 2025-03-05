package com.github.ringoame196_s_mcPlugin

import com.github.ringoame196_s_mcPlugin.commands.Command
import com.github.ringoame196_s_mcPlugin.datas.Data
import com.github.ringoame196_s_mcPlugin.events.ItemFrameEvents
import com.github.ringoame196_s_mcPlugin.managers.ImgMapManager
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    private val plugin = this
    private val itemMapManager = ImgMapManager()

    override fun onEnable() {
        super.onEnable()
        saveDefaultConfig() // config生成
        Data.max = config.getInt("max")
        server.pluginManager.registerEvents(ItemFrameEvents(plugin), plugin)
        val command = getCommand("imagepuzzle")
        command!!.setExecutor(Command())
    }

    override fun onDisable() {
        itemMapManager.deleteALL()
        super.onDisable()
    }
}
