package me.sgrishchenko.jetbrainschat.client.virtualList

import kotlin.test.Test
import kotlin.test.assertEquals

class VirtualizationCacheTest {
    private val estimatedItemSize = 100

    @Test
    fun shouldReturnScrollHeightForMeasuredItems() {
        val cache = VirtualizationCache(estimatedItemSize)

        cache.setItemSize(index = 0, size = 10)
        cache.setItemSize(index = 1, size = 20)
        cache.setItemSize(index = 2, size = 50)

        assertEquals(
            10 + 20 + 50,
            cache.getScrollHeight(itemCount = 3)
        )
    }

    @Test
    fun shouldInterpolateScrollHeightForUnknownItems() {
        val cache = VirtualizationCache(estimatedItemSize)

        cache.setItemSize(index = 0, size = 10)
        cache.setItemSize(index = 3, size = 50)

        assertEquals(
            10 + (2 * estimatedItemSize) + 50,
            cache.getScrollHeight(itemCount = 4)
        )
    }

    @Test
    fun shouldExtrapolateScrollHeightForBigItemCount() {
        val cache = VirtualizationCache(estimatedItemSize)

        cache.setItemSize(index = 0, size = 10)
        cache.setItemSize(index = 1, size = 20)
        cache.setItemSize(index = 2, size = 50)

        assertEquals(
            10 + 20 + 50 + 3 * estimatedItemSize,
            cache.getScrollHeight(itemCount = 6)
        )
    }

    @Test
    fun shouldInvalidateCacheAfterSetMiddleItemSize() {
        val cache = VirtualizationCache(estimatedItemSize)

        cache.setItemSize(index = 0, size = 10)
        cache.setItemSize(index = 1, size = 20)
        cache.setItemSize(index = 2, size = 50)

        assertEquals(
            10 + 20 + 50 + 3 * estimatedItemSize,
            cache.getScrollHeight(itemCount = 6)
        )

        cache.setItemSize(index = 1, size = 30)

        assertEquals(
            10 + 30 + estimatedItemSize + 3 * estimatedItemSize,
            cache.getScrollHeight(itemCount = 6)
        )
    }
}