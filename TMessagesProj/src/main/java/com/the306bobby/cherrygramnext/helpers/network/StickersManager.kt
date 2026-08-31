/**
 * This is the source code of Cherrygram Next for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.cherrygramnext.helpers.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.widget.Toast
import androidx.core.util.component1
import androidx.core.util.component2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLog
import org.telegram.messenger.MessageObject
import org.telegram.tgnet.TLRPC
import com.the306bobby.cherrygramnext.core.configs.CherrygramChatsConfig
import com.the306bobby.cherrygramnext.core.configs.CherrygramCoreConfig
import com.the306bobby.cherrygramnext.core.configs.CherrygramDebugConfig
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt
import androidx.core.graphics.scale
import org.telegram.messenger.UserConfig
import com.the306bobby.cherrygramnext.chats.helpers.ChatsHelper

object StickersManager {

    private const val FILE_NAME = "blocker_stickers_list.txt"
    private val REFRESH_INTERVAL = if (CherrygramCoreConfig.isDevBuild()) 60 * 60 * 1000L else 24 * 60 * 60 * 1000L // 24 hours

    private val blockedStickersIds = mutableSetOf<Long>()

    suspend fun startAutoRefresh(context: Context) {
        loadLocalStickersList(context)
    }


    private suspend fun loadLocalStickersList(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, FILE_NAME)
                if (!file.exists()) return@withContext

                val reader = InputStreamReader(context.openFileInput(FILE_NAME))
                val tempStickerSetIDs = mutableSetOf<Long>()

                reader.buffered().useLines { lines ->
                    lines.forEach { line ->
                        line.trim().toLongOrNull()?.let { id ->
                            tempStickerSetIDs.add(id)
                        }
                    }
                }

                synchronized(blockedStickersIds) {
                    blockedStickersIds.clear()
                    blockedStickersIds.addAll(tempStickerSetIDs)
                }

            } catch (e: Exception) {
                FileLog.e(e)
            }
        }
    }

    private fun isStickerSetBlockedRemote(stickerSetID: Long): Boolean {
        synchronized(blockedStickersIds) {
            return blockedStickersIds.contains(stickerSetID)
        }
    }

    // Locally stored IDs
    private val iDs = arrayOf(683462835916767409L, 1510769529645432834L, 8106175868352593928L, 5835129661968875533L,
        5149354467191160831L, 5091996690789957635L, 7131267980628852734L, 7131267980628852733L, 3346563080237613068L,
        6055278067666911223L, 5062008833983905790L, 1169953291908415506L, 6055278067666911216L, 4331929539736240157L,
        5091996690789957649L, 9087292238668496936L, 6417088260173987842L, 8728063708061761539L, 4238900539514945542L,
        4008340909736329215, 3560771065137332232, 5062008833983905791L, 3442978400170409980L, 4366402085420269571L,
        7346771112912486399L, 1307729623638867964L, 8088313558921641982L, 3980386015587074063L, 3471325442030436360L,
        5390126781875355656L, 5390126781875355657L, 4888624861133406212L
    )

    private fun isLocalSetId(document: TLRPC.Document): Boolean = iDs.any { setID: Long ->
        setID == MessageObject.getStickerSetId(document)
    }

    fun isStickerSetToBlock(document: TLRPC.Document): Boolean {
        return isStickerSetBlockedRemote(document.id) || isLocalSetId(document)
    }

    //Get sticker from assets
    fun copyStickerFromAssets() {
        try {
            val outFile = File(ApplicationLoader.applicationContext.getExternalFilesDir(null), "stickers/cherrygram.webm")
            if (outFile.exists()) return
            outFile.parentFile?.mkdirs()

            ApplicationLoader.applicationContext.assets.open("cherrygram.webm").use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getStickerFileFromImage(originalPath: String): File {
        val dotIndex = originalPath.lastIndexOf('.')
        val basePath = if (dotIndex != -1) originalPath.take(dotIndex) else originalPath
        val webpFile = File("$basePath.webp")

        try {
            val MAX_SIZE = 2560
            var bmp = BitmapFactory.decodeFile(originalPath)
            if (bmp != null) {
                val width = bmp.width
                val height = bmp.height

                if (width > MAX_SIZE || height > MAX_SIZE) {
                    val scale = minOf(MAX_SIZE.toFloat() / width, MAX_SIZE.toFloat() / height)
                    val newWidth = (width * scale).roundToInt()
                    val newHeight = (height * scale).roundToInt()
                    bmp = bmp.scale(newWidth, newHeight)
                }

                val (rotate, flip) = AndroidUtilities.getImageOrientation(originalPath)

                if (rotate != 0 || flip != 0) {
                    val matrix = Matrix()
                    if (flip == 1) {
                        matrix.postScale(-1f, 1f)
                        matrix.postTranslate(bmp.width.toFloat(), 0f)
                    }
                    if (rotate != 0) {
                        matrix.postRotate(rotate.toFloat())
                    }
                    bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                }

                FileOutputStream(webpFile).use { out ->
                    bmp.compress(Bitmap.CompressFormat.WEBP, 50, out)
                }
            }
        } catch (e: Exception) {
            FileLog.e(e)
        }

        return webpFile
    }

    fun addMessageToClipboardAsSticker(selectedObject: MessageObject, callback: Runnable?) {
        val path = ChatsHelper.getInstance(UserConfig.selectedAccount).getPathToMessage(selectedObject)
        if (path.isNullOrEmpty()) return

        Thread {
            try {
                val image = BitmapFactory.decodeFile(path) ?: return@Thread

                val dotIndex = path.lastIndexOf('.')
                val basePath = if (dotIndex != -1) path.take(dotIndex) else path
                val file = File("$basePath.webp")

                FileOutputStream(file).use { stream ->
                    image.compress(Bitmap.CompressFormat.WEBP, 50, stream)
                }

                AndroidUtilities.runOnUIThread {
                    ChatsHelper.getInstance(UserConfig.selectedAccount).addFileToClipboard(file, callback)
                }
            } catch (e: Exception) {
                FileLog.e(e)
            }
        }.start()
    }

}