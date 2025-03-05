package com.github.ringoame196_s_mcPlugin.datas

import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player

object Data {
    const val IMG_ITEM_FRAME_TAG = "img_frame"
    var max = 10

    val groupData = mutableMapOf<String, GroupData>()
    val itemFrameData = mutableMapOf<ItemFrame, ItemFrameData>()

    val playerSelectItemFrame = mutableMapOf<Player, ItemFrame>()
}
