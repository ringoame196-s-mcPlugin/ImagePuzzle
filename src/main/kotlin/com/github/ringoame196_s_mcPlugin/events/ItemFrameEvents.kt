package com.github.ringoame196_s_mcPlugin.events

import com.github.ringoame196_s_mcPlugin.datas.Data
import com.github.ringoame196_s_mcPlugin.managers.ImgMapManager
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.player.PlayerInteractEntityEvent

class ItemFrameEvents() : Listener {
    private val imgMapManager = ImgMapManager()

    @EventHandler
    fun onBreak(e: HangingBreakEvent) {
        if (!isImgItemFrame(e.entity)) return
        if (Data.itemFrameAllList.contains(e.entity)) e.isCancelled = true
    }

    @EventHandler
    fun onRightClick(e: PlayerInteractEntityEvent) {
        val itemFrame = e.rightClicked as? ItemFrame ?: return
        if (isImgItemFrame(itemFrame)) e.isCancelled = true
        val player = e.player

        val playerSelectItemFrame = Data.playerSelectItemFrame[player]

        lateinit var message: String
        lateinit var sound: Sound

        if (playerSelectItemFrame == null) {
            sound = Sound.UI_BUTTON_CLICK
            message = "${ChatColor.GOLD}額縁を選択しました"
            Data.playerSelectItemFrame[player] = itemFrame
        } else {
            if (imgMapManager.isSameGroup(playerSelectItemFrame, itemFrame)) {
                imgMapManager.changeImg(playerSelectItemFrame, itemFrame)

                sound = Sound.BLOCK_ANVIL_USE
                message = "${ChatColor.AQUA}額縁を交換しました"
            } else {
                message = "${ChatColor.RED}同じグループの額縁のみ交換可能です"
            }

            Data.playerSelectItemFrame.remove(player)
        }
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(message))
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
