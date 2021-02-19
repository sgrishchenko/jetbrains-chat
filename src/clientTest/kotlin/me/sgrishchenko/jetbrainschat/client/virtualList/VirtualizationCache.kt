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

    @Test
    fun shouldReturnVisibleIndexes() {
        val cache = VirtualizationCache(estimatedItemSize)

        cache.setItemSize(index = 0, size = 100) // 100
        // <- 250
        cache.setItemSize(index = 1, size = 200) // 300
        cache.setItemSize(index = 2, size = 300) // 600
        cache.setItemSize(index = 3, size = 200) // 800
        // <- 750
        cache.setItemSize(index = 4, size = 100) // 900

        assertEquals(
            1..3,
            cache.getVisibleIndexes(clientWindow = 250..750)
        )
    }

    @Test
    fun shouldReturnVisibleIndexesForLongList() {
        val cache = VirtualizationCache(estimatedItemSize)

        cache.setItemSize(index = 0, size = 100) // 100
        cache.setItemSize(index = 1, size = 200) // 300
        cache.setItemSize(index = 2, size = 300) // 600
        // <- 650
        cache.setItemSize(index = 3, size = 200) // 800
        cache.setItemSize(index = 4, size = 100) // 900
        cache.setItemSize(index = 5, size = 100) // 1000
        cache.setItemSize(index = 6, size = 200) // 1200
        cache.setItemSize(index = 7, size = 300) // 1500
        // <- 1650
        cache.setItemSize(index = 8, size = 200) // 1700
        cache.setItemSize(index = 9, size = 100) // 1800
        cache.setItemSize(index = 10, size = 100) // 1900
        cache.setItemSize(index = 11, size = 200) // 2100
        // <- 2150
        cache.setItemSize(index = 12, size = 300) // 2400
        // <- 2450
        cache.setItemSize(index = 13, size = 200) // 2600
        cache.setItemSize(index = 14, size = 100) // 2700

        assertEquals(
            8..12,
            cache.getVisibleIndexes(clientWindow = 1650..2150)
        )

        assertEquals(
            3..13,
            cache.getVisibleIndexes(clientWindow = 650..2450)
        )

        assertEquals(
            3..8,
            cache.getVisibleIndexes(clientWindow = 650..1650)
        )

        assertEquals(
            8..13,
            cache.getVisibleIndexes(clientWindow = 1650..2450)
        )

        assertEquals(
            5..11,
            cache.getVisibleIndexes(clientWindow = 1000..2100)
        )
    }
}