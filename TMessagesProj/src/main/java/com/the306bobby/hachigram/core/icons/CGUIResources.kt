/**
 * This is the source code of Hachigram for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.hachigram.core.icons

import android.annotation.SuppressLint
import android.content.res.*
import android.graphics.drawable.Drawable
import android.util.Log
import com.the306bobby.hachigram.core.configs.HachigramAppearanceConfig
import com.the306bobby.hachigram.core.configs.HachigramCoreConfig
import com.the306bobby.hachigram.core.icons.icon_replaces.BaseIconReplace

@Suppress("DEPRECATION")
@SuppressLint("UseCompatLoadingForDrawables")
class CGUIResources(private val wrapped: Resources) : Resources(wrapped.assets, wrapped.displayMetrics, wrapped.configuration) {

    private var activeReplacement: BaseIconReplace = HachigramAppearanceConfig.getCurrentIconPack()

    fun reloadReplacements() {
        activeReplacement = HachigramAppearanceConfig.getCurrentIconPack()
        clearCache()
    }

    private val drawableCache = object : LinkedHashMap<Triple<Int, Int?, Theme?>, Drawable?>(300, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Triple<Int, Int?, Theme?>, Drawable?>?): Boolean {
            if (size > 300) {
                clearCache()
                return true
            }
            return false
        }
    }

    private var cacheHits = 0
    private var cacheMisses = 0

    private fun clearCache() {
        drawableCache.clear()
        cacheHits = 0
        cacheMisses = 0

        if (HachigramCoreConfig.isDevBuild()) Log.d("CGUIResources", "🗑 Cache cleared automatically (limit exceeded)!")
    }

    private fun getCachedDrawable(
        cacheKey: Triple<Int, Int?, Theme?>,
        wrappedId: Int,
        loader: () -> Drawable?
    ): Drawable? {
        return drawableCache.getOrPut(cacheKey) {
            cacheMisses++
            if (HachigramCoreConfig.isDevBuild()) Log.w("CGUIResources", "🛑 Cache MISS ($cacheMisses misses) - Loading new drawable for id=$wrappedId, key=$cacheKey")
            loader()
        }?.let { drawable ->
            drawable.constantState?.newDrawable(wrapped, cacheKey.third)?.mutate() ?: drawable
        }?.also {
            cacheHits++
            if (HachigramCoreConfig.isDevBuild()) Log.d("CGUIResources", "✅ Cache HIT ($cacheHits hits) - Using cached drawable for id=$wrappedId, key=$cacheKey")
        }
    }

    @Deprecated("Deprecated in Java")
    @Throws(NotFoundException::class)
    override fun getDrawable(id: Int): Drawable? {
        val wrappedId = activeReplacement.wrap(id)
        val cacheKey = Triple(wrappedId, null, null)

        return getCachedDrawable(cacheKey, wrappedId) { wrapped.getDrawable(wrappedId, null) }
    }

    @Throws(NotFoundException::class)
    override fun getDrawable(id: Int, theme: Theme?): Drawable? {
        val wrappedId = activeReplacement.wrap(id)
        val cacheKey = Triple(wrappedId, null, theme)

        return getCachedDrawable(cacheKey, wrappedId) { wrapped.getDrawable(wrappedId, theme) }
    }

    @Deprecated("Deprecated in Java")
    @Throws(NotFoundException::class)
    override fun getDrawableForDensity(id: Int, density: Int): Drawable? {
        val wrappedId = activeReplacement.wrap(id)
        val cacheKey = Triple(wrappedId, density, null)

        return getCachedDrawable(cacheKey, wrappedId) { wrapped.getDrawableForDensity(wrappedId, density, null) }
    }

    override fun getDrawableForDensity(id: Int, density: Int, theme: Theme?): Drawable? {
        val wrappedId = activeReplacement.wrap(id)
        val cacheKey = Triple(wrappedId, density, theme)

        return getCachedDrawable(cacheKey, wrappedId) { wrapped.getDrawableForDensity(wrappedId, density, theme) }
    }

}