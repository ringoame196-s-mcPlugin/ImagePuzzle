package com.github.ringoame196_s_mcPlugin.commands

import com.github.ringoame196_s_mcPlugin.datas.Data
import com.github.ringoame196_s_mcPlugin.managers.ImgManager
import com.github.ringoame196_s_mcPlugin.managers.ImgMapManager
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import java.net.MalformedURLException
import java.net.URL

class Command() : CommandExecutor, TabCompleter {
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
            CommandConst.CHECK_COMMAND -> checkCommand(sender)
            CommandConst.SHUFFLE_COMMAND -> shuffleCommand(sender)
            else -> {
                val message = "${ChatColor.RED}コマンド構文が間違っています"
                sender.sendMessage(message)
                return true
            }
        }
    }

    private fun makeCommand(sender: Player, args: Array<out String>): Boolean {
        if (args.size < 3) return false
        val url = try { URL(args[1]) } catch (_: MalformedURLException) { return false }
        val width = args[2].toIntOrNull() ?: return false

        if (width > Data.max) {
            val message = "${ChatColor.RED}横幅が長すぎます"
            sender.sendMessage(message)
            return true
        }

        val imgManager = ImgManager(url)
        val cutImgList = imgManager.splitImage(width)
        val groupID = imgManager.groupID
        val size = cutImgList.size

        if (imgMapManager.make(sender, groupID, cutImgList, width)) {
            val message = "${ChatColor.GOLD}${size}枚の画像貼り付け完了"
            val sound = Sound.BLOCK_ANVIL_USE
            sender.sendMessage(message)
            sender.playSound(sender, sound, 1f, 1f)
        } else {
            val message = "${ChatColor.RED}画像が正常に生成されませんでした"
            sender.sendMessage(message)
        }

        return true
    }

    private fun deleteCommand(sender: Player): Boolean {
        val itemFrame = acquisitionItemFrame(sender) ?: return true
        val groupID = Data.itemFrameData[itemFrame]?.groupID ?: return true
        imgMapManager.delete(groupID)

        val message = "${ChatColor.RED}画像削除しました"
        val sound = Sound.BLOCK_ANVIL_USE
        sender.sendMessage(message)
        sender.playSound(sender, sound, 1f, 1f)
        return true
    }

    private fun checkCommand(sender: Player): Boolean {
        val itemFrameList = acquisitionItemFrameList(sender) ?: return true

        if (imgMapManager.check(itemFrameList)) {
            val title = "${ChatColor.GOLD}パズルクリア"
            val subTitle = "${ChatColor.AQUA}おめでとう！"
            val sound = Sound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR

            sender.sendTitle(title, subTitle)
            sender.playSound(sender, sound, 1f, 1f)
        } else {
            val message = "${ChatColor.RED}失敗"
            val sound = Sound.BLOCK_NOTE_BLOCK_BELL

            sender.sendMessage(message)
            sender.playSound(sender, sound, 1f, 1f)
        }
        return true
    }

    private fun shuffleCommand(sender: Player): Boolean {
        val itemFrameList = acquisitionItemFrameList(sender) ?: return true
        imgMapManager.shuffle(itemFrameList)

        val message = "${ChatColor.GOLD}シャッフルしました"
        val sound = Sound.BLOCK_ANVIL_USE

        sender.sendMessage(message)
        sender.playSound(sender, sound, 1f, 1f)
        return true
    }

    private fun acquisitionItemFrame(sender: Player): ItemFrame? {
        val itemFrame = imgMapManager.acquisitionItemFrame(sender)
        if (itemFrame == null) {
            val message = "${ChatColor.RED}額縁の取得に失敗しました"
            sender.sendMessage(message)
        }
        return itemFrame
    }

    private fun acquisitionItemFrameList(sender: Player): MutableList<ItemFrame>? {
        val itemFrame = acquisitionItemFrame(sender) ?: return null
        val itemFrameData = Data.itemFrameData[itemFrame]
        val groupID = itemFrameData?.groupID
        val itemFrameList = Data.groupItemFrameList[groupID]

        if (itemFrameList == null) {
            val message = "${ChatColor.RED}グループから額縁の取得が出来ませんでした"
            sender.sendMessage(message)
        }

        return itemFrameList
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String>? {
        return when (args.size) {
            1 -> mutableListOf(
                CommandConst.MAKE_COMMAND, CommandConst.DELETE_COMMAND, CommandConst.CHECK_COMMAND,
                CommandConst.SHUFFLE_COMMAND
            )
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
