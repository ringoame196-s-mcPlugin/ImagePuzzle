package com.github.ringoame196_s_mcPlugin.managers

import org.bukkit.plugin.Plugin
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.util.UUID
import javax.imageio.ImageIO

class ImgManager(downloadURL: String, private val plugin: Plugin) {
    private val imgID = UUID.randomUUID()
    private val img: BufferedImage? = try {
        ImageIO.read(URL(downloadURL))
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    private fun saveImg(img: BufferedImage, id: String) {
        val imgFolder = File(plugin.dataFolder, "img")
        if (!imgFolder.exists()) imgFolder.mkdirs()
        val imgGroup = File("${imgFolder.path}/$imgID")
        if (!imgGroup.exists()) imgGroup.mkdirs()
        val path = File(imgGroup, id)
        ImageIO.write(img, "PNG", path)
    }

    fun acquisitionWidth(): Int {
        return img?.width ?: 0
    }

    fun acquisitionHeight(): Int {
        return img?.height ?: 0
    }

    fun splitImage(cols: Int): List<BufferedImage> {
        val imgList = mutableListOf<BufferedImage>()
        img ?: return imgList
        // 画像の幅と高さを取得
        val imageWidth = acquisitionWidth()
        val imageHeight = acquisitionHeight()

        // 各分割部分の幅と高さ
        val partWidth = imageWidth / cols

        val rows = imageHeight / partWidth

        val partHeight = imageHeight / rows

        // 分割された画像をListにする
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                // 分割された部分を切り取る
                val x = j * partWidth
                val y = i * partHeight
                val croppedImage = img.getSubimage(x, y, partWidth, partHeight)

                imgList.add(adjustmentSize(croppedImage))
                saveImg(croppedImage, "$i$j")
            }
        }
        return imgList
    }

    private fun adjustmentSize(originalImage: BufferedImage): BufferedImage {
        val size = 128
        val resizedImage = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g = resizedImage.createGraphics()
        g.drawImage(originalImage, 0, 0, size, size, null)
        g.dispose()
        return resizedImage
    }
}
