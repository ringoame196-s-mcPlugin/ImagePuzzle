package com.github.ringoame196_s_mcPlugin.events

import com.github.ringoame196_s_mcPlugin.datas.Data
import com.github.ringoame196_s_mcPlugin.managers.ImgMapManager
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.plugin.Plugin

class ItemFrameEvents(private val plugin: Plugin) : Listener {
    private val imgMapManager = ImgMapManager()

    @EventHandler
    fun onBreak(e: HangingBreakEvent) {
        if (isImgItemFrame(e.entity)) e.isCancelled = true
    }

    @EventHandler
    fun onRightClick(e: PlayerInteractEntityEvent) {
        val itemFrame = e.rightClicked as? ItemFrame ?: return
        if (isImgItemFrame(itemFrame)) e.isCancelled = true
        val player = e.player
        val sound = Sound.UI_BUTTON_CLICK
        imgMapManager.next(itemFrame)
        player.playSound(player, sound, 1f, 1f)
    }

    @EventHandler
    fun onLeftClick(e: EntityDamageEvent) {
        if (isImgItemFrame(e.entity)) e.isCancelled = true
    }

    private fun isImgItemFrame(entity: Entity): Boolean {
        return entity.scoreboardTags.contains(Data.IMG_ITEM_FRAME_TAG)
    }
}
