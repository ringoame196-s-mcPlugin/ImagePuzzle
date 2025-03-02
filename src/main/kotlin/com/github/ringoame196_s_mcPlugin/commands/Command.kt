package com.github.ringoame196_s_mcPlugin.commands

import com.github.ringoame196_s_mcPlugin.datas.Data
import com.github.ringoame196_s_mcPlugin.datas.GroupData
import com.github.ringoame196_s_mcPlugin.datas.ItemFrameData
import com.github.ringoame196_s_mcPlugin.managers.ImgManager
import com.github.ringoame196_s_mcPlugin.managers.ImgMapManager
import com.github.ringoame196_s_mcPlugin.managers.MapManager
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import java.net.MalformedURLException
import java.net.URL
import kotlin.random.Random

class Command() : CommandExecutor, TabCompleter {
    private val mapManager = MapManager()
    private val imgMapManager = ImgMapManager()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            val message = "${ChatColor.RED}このコマンドはプレイヤーのみ実行可能です"
            sender.sendMessage(message)
            return true
        }
        if (args.isEmpty()) return false

        val subCommand = args[0]

        return when (subCommand) {
            CommandConst.MAKE_COMMAND -> makeCommand(sender, args)
            CommandConst.DELETE_COMMAND -> deleteCommand(sender)
            CommandConst.CHECK_COMMAND -> check(sender)
            else -> {
                val message = "${ChatColor.RED}コマンド構文が間違っています"
                sender.sendMessage(message)
                return true
            }
        }
    }

    private fun makeCommand(sender: Player, args: Array<out String>): Boolean {
        if (args.size < 3) return false
        val playerLocation = imgMapManager.acquisitionBlockBeforeLookingAt(sender)?.clone() ?: return true
        val url = try { URL(args[1]) } catch (_: MalformedURLException) { return false }
        val width = args[2].toIntOrNull() ?: return false

        if (width > Data.max) {
            val message = "${ChatColor.RED}横幅が長すぎます"
            sender.sendMessage(message)
            return true
        }

        val imgManager = ImgManager(url)

        val rightDirection = imgMapManager.acquisitionRightDirection(sender)
        val cutImgList = imgManager.splitImage(width)

        val groupID = imgManager.groupID
        val itemFrameList = mutableListOf<ItemFrame>()
        val imgMapID = mutableListOf<Int>()

        var i = 0
        var placeLocation = playerLocation

        val size = cutImgList.size
        var firstMapID: Int? = null

        for (cutImg in cutImgList) {
            val mapID = mapManager.issueNewMap()
            if (placeLocation.block.type == Material.AIR) {
                val itemFrame = imgMapManager.summonItemFrame(placeLocation, mapID) ?: continue
                itemFrameList.add(itemFrame)
                imgMapManager.setImg(cutImg, mapID)

                imgMapID.add(mapID)

                val randomMapID = (Random.nextInt(0, size) + (firstMapID ?: mapID))
                val itemFrameData = ItemFrameData(groupID, randomMapID, mapID)
                Data.itemFrameData[itemFrame] = itemFrameData
            }
            rightDirection.addition(placeLocation, 1)
            i ++
            if (i == width) {
                i = 0
                placeLocation.add(0.0, -1.0, 0.0)
                rightDirection.reset(placeLocation, width)
            }
            if (firstMapID == null) firstMapID = mapID
        }

        val groupData = GroupData(
            itemFrameList,
            imgMapID.shuffled(), // シャッフル
            firstMapID ?: 0
        )
        Data.groupData[groupID] = groupData

        val message = "${ChatColor.GOLD}${size}枚の画像貼り付け完了"
        val sound = Sound.BLOCK_ANVIL_USE
        sender.sendMessage(message)
        sender.playSound(sender, sound, 1f, 1f)

        return true
    }

    private fun deleteCommand(sender: Player): Boolean {
        val itemFrame = imgMapManager.acquisitionItemFrame(sender)
        if (itemFrame == null) {
            val message = "${ChatColor.RED}額縁の取得に失敗しました"
            sender.sendMessage(message)
            return true
        }
        val groupID = Data.itemFrameData[itemFrame]?.groupID
        if (groupID != null) imgMapManager.delete(groupID)
        val message = "${ChatColor.RED}画像削除しました"
        val sound = Sound.BLOCK_ANVIL_USE
        sender.sendMessage(message)
        sender.playSound(sender, sound, 1f, 1f)
        return true
    }

    private fun check(sender: Player): Boolean {
        val itemFrame = imgMapManager.acquisitionItemFrame(sender)
        if (itemFrame == null) {
            val message = "${ChatColor.RED}額縁の取得に失敗しました"
            sender.sendMessage(message)
            return true
        }
        val message = if (checkImg(itemFrame)) "${ChatColor.GOLD}クリア" else "${ChatColor.RED}失敗"
        sender.sendMessage(message)
        return true
    }

    private fun checkImg(itemFrame: ItemFrame): Boolean {
        val itemFrameData = Data.itemFrameData[itemFrame]
        val groupID = itemFrameData?.groupID
        val groupData = Data.groupData[groupID] ?: return false

        var isMatched = true

        for (itemFrame in Data.groupData[groupID]?.itemFrameList ?: return true) {
            val itemFrameData = Data.itemFrameData[itemFrame]
            val selectNumber = itemFrameData?.selectNumber ?: continue
            val mapID = groupData.imgMapID[selectNumber]
            if (itemFrameData.mapID != mapID) isMatched = false
        }
        return isMatched
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String>? {
        return when (args.size) {
            1 -> mutableListOf(CommandConst.MAKE_COMMAND, CommandConst.DELETE_COMMAND, CommandConst.CHECK_COMMAND)
            2 -> when (args[0]) {
                CommandConst.MAKE_COMMAND -> mutableListOf("[画像のURL]")
                else -> mutableListOf()
            }
            3 -> when (args[0]) {
                CommandConst.MAKE_COMMAND -> mutableListOf("[横幅]")
                else -> mutableListOf()
            }
            else -> mutableListOf()
        }
    }
}
