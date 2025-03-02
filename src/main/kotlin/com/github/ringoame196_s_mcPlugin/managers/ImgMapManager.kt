package com.github.ringoame196_s_mcPlugin.managers

import com.github.ringoame196_s_mcPlugin.datas.Data
import com.github.ringoame196_s_mcPlugin.datas.ImageRenderer
import com.github.ringoame196_s_mcPlugin.directions.Direction
import com.github.ringoame196_s_mcPlugin.directions.East
import com.github.ringoame196_s_mcPlugin.directions.North
import com.github.ringoame196_s_mcPlugin.directions.South
import com.github.ringoame196_s_mcPlugin.directions.West
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapView
import org.bukkit.util.BlockIterator
import java.awt.image.BufferedImage

class ImgMapManager() {
    private val mapManager = MapManager()

    fun summonItemFrame(location: Location, mapID: Int): ItemFrame? {
        val world = location.world
        val map = mapManager.acquisitionMap(mapID)
        // 額縁召喚
        val itemFrame: ItemFrame? = world?.spawn(location, ItemFrame::class.java)
        itemFrame?.let {
            it.isVisible = false // 可視化するかどうか
            it.isInvulnerable = true
            it.isCustomNameVisible = true
            it.setGravity(false)
            it.scoreboardTags.add(Data.IMG_ITEM_FRAME_TAG)
        }
        itemFrame?.setItem(map)
        return itemFrame
    }

    fun delete(groupID: String) {
        val itemFrameList = Data.groupData[groupID]?.itemFrameList ?: return
        for (itemFrame in itemFrameList) {
            Data.itemFrameData.remove(itemFrame)
            itemFrame.remove()
        }
        Data.groupData.remove(groupID)
    }

    fun setImg(img: BufferedImage, mapID: Int) {
        var mapView = Bukkit.getMap(mapID) ?: return // マップIDからMapViewを取得
        val mapItem = ItemStack(Material.FILLED_MAP) // マップアイテムを作成
        val meta = mapItem.itemMeta as? MapMeta ?: return // MapMetaを取得

        mapView = pasteMap(mapView)
        mapView = pasteImage(mapView, img)
        meta.mapView = mapView
        mapItem.itemMeta = meta
    }

    fun acquisitionBlockBeforeLookingAt(player: Player): Location? {
        val blockIterator = BlockIterator(player, 5) // 最大5ブロック先まで視線を追跡
        var lastBlock: org.bukkit.block.Block? = null

        while (blockIterator.hasNext()) {
            val currentBlock = blockIterator.next()
            if (!currentBlock.type.isAir) {
                return lastBlock?.location // 1個手前のブロックを返す
            }
            lastBlock = currentBlock
        }
        return null // 見ているブロックが空気なら null
    }

    fun acquisitionRightDirection(player: Player): Direction {
        // プレイヤーの向いている方向 (yaw) を取得
        val yaw = (player.location.yaw + 360) % 360 // 負の値を防ぐため 360 を足してから 360 で割る

        return when (yaw) {
            in 45.0..135.0 -> North() // 向いているのが西なら右は北
            in 135.0..225.0 -> East() // 向いているのが北なら右は東
            in 225.0..315.0 -> South() // 向いているのが東なら右は南
            else -> West() // 向いているのが南なら右は西
        }
    }

    fun acquisitionItemFrame(player: Player): ItemFrame? {
        val maxDistance = 5.0 // 検出範囲 (ブロック単位)

        val result = player.world.rayTraceEntities(
            player.eyeLocation, // プレイヤーの視点位置
            player.location.direction, // プレイヤーの向いている方向
            maxDistance, // 最大距離
            { it is ItemFrame } // 額縁のみ対象
        )

        return result?.hitEntity as? ItemFrame
    }

    private fun pasteImage(mapView: MapView, img: BufferedImage): MapView {
        mapView.addRenderer(ImageRenderer(img))
        return mapView
    }

    private fun pasteMap(mapView: MapView): MapView {
        mapView.renderers.clear()
        mapView.scale = MapView.Scale.FARTHEST
        return mapView
    }

    fun deleteALL() {
        for (groupID in Data.groupData.keys) {
            delete(groupID)
        }
    }

    fun next(itemFrame: ItemFrame) {
        val itemFrameData = Data.itemFrameData[itemFrame] ?: return
        val groupID = itemFrameData.groupID
        val groupData = Data.groupData[groupID]
        val itemFrameList = groupData?.itemFrameList ?: return
        val itemFrameNumber = Data.itemFrameData[itemFrame]?.selectNumber ?: 0
        val nextNumber = (itemFrameNumber + 1) % itemFrameList.size
        Data.itemFrameData[itemFrame]?.selectNumber = nextNumber

        val nextMapID = groupData.imgMapID[nextNumber]
        val mapItem = mapManager.acquisitionMap(nextMapID) ?: return
        itemFrame.setItem(mapItem)
    }
}
