package com.aptopayments.sdk.utils

import android.content.Context
import java.io.File

internal interface CacheFileManager {
    fun createTempFile(namePrefix: String = "tmp", nameSuffix: String = "", dirName: String = ""): File
    fun cleanCache(dirName: String = "")
}

internal class CacheFileManagerImpl(context: Context) : CacheFileManager {
    private val cacheDir = context.cacheDir

    override fun createTempFile(namePrefix: String, nameSuffix: String, dirName: String): File {
        val dir = getDir(dirName)
        return createTempFile(namePrefix, nameSuffix, dir)
    }

    override fun cleanCache(dirName: String) {
        val dir = getDir(dirName)
        removeAllFilesFromDir(dir)
    }

    private fun getDir(dirName: String): File {
        return if (dirName.isNotEmpty()) {
            val dir = File("${cacheDir.absolutePath}${File.separatorChar}$dirName")
            createIfNotExists(dir)
            dir
        } else {
            cacheDir
        }
    }

    private fun createIfNotExists(dir: File) {
        if (!dir.exists()) {
            dir.mkdir()
        }
    }

    private fun removeAllFilesFromDir(dir: File) {
        if (dir.exists()) {
            dir.listFiles().forEach { it.delete() }
        }
    }
}
