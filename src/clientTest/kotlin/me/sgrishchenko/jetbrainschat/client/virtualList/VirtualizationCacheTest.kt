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

        cache.setItemSize(index = 0, size = 100) // offset = 100
        // <- 250
        cache.setItemSize(index = 1, size = 200) // offset = 300
        cache.setItemSize(index = 2, size = 300) // offset = 600
        // <- 750
        cache.setItemSize(index = 3, size = 200) // offset = 800
        cache.setItemSize(index = 4, size = 100) // offset = 900

        assertEquals(
            1..3,
            cache.getVisibleIndexes(clientWindow = 250..750, itemCount = 15)
        )
    }

    @Test
    fun shouldReturnVisibleIndexesForEmptyList() {
        val cache = VirtualizationCache(estimatedItemSize)

        assertEquals(
            IntRange.EMPTY,
            cache.getVisibleIndexes(clientWindow = 0..0, itemCount = 1)
        )
    }

    @Test
    fun shouldReturnVisibleIndexesForLongList() {
        val cache = VirtualizationCache(estimatedItemSize)

        cache.setItemSize(index = 0, size = 100) // offset = 100
        cache.setItemSize(index = 1, size = 200) // offset = 300
        cache.setItemSize(index = 2, size = 300) // offset = 600
        // <- 650
        cache.setItemSize(index = 3, size = 200) // offset = 800
        cache.setItemSize(index = 4, size = 100) // offset = 900
        cache.setItemSize(index = 5, size = 100) // offset = 1000
        cache.setItemSize(index = 6, size = 200) // offset = 1200
        cache.setItemSize(index = 7, size = 300) // offset = 1500
        // <- 1650
        cache.setItemSize(index = 8, size = 200) // offset = 1700
        cache.setItemSize(index = 9, size = 100) // offset = 1800
        cache.setItemSize(index = 10, size = 100) // offset = 1900
        cache.setItemSize(index = 11, size = 200) // offset = 2100
        // <- 2150
        cache.setItemSize(index = 12, size = 300) // offset = 2400
        // <- 2450
        cache.setItemSize(index = 13, size = 200) // offset = 2600
        cache.setItemSize(index = 14, size = 100) // offset = 2700

        assertEquals(
            8..12,
            cache.getVisibleIndexes(clientWindow = 1650..2150, itemCount = 15)
        )

        assertEquals(
            3..13,
            cache.getVisibleIndexes(clientWindow = 650..2450, itemCount = 15)
        )

        assertEquals(
            3..8,
            cache.getVisibleIndexes(clientWindow = 650..1650, itemCount = 15)
        )

        assertEquals(
            8..13,
            cache.getVisibleIndexes(clientWindow = 1650..2450, itemCount = 15)
        )

        assertEquals(
            5..11,
            cache.getVisibleIndexes(clientWindow = 1000..2100, itemCount = 15)
        )
    }

    @Test
    fun shouldReturnVisibleIndexesEvenIfCacheWasInvalidated() {
        val cache = VirtualizationCache(estimatedItemSize)

        cache.setItemSize(index = 0, size = 100) // offset = 100
        // <- 250
        cache.setItemSize(index = 1, size = 200) // offset = 300
        cache.setItemSize(index = 2, size = 300) // offset = 600
        cache.setItemSize(index = 3, size = 200) // offset = 800
        // <- 850
        cache.setItemSize(index = 4, size = 110) // offset = 910

        assertEquals(
            100 + 200 + 300 + 200 + 110,
            cache.getScrollHeight(itemCount = 5)
        )

        assertEquals(
            1..4,
            cache.getVisibleIndexes(clientWindow = 250..850, itemCount = 5)
        )

        cache.setItemSize(index = 2, size = 500)

        // current item offsets
        // index = 0, size = 100, offset = 100
        // <- 250
        // index = 1, size = 200, offset = 300
        // index = 2, size = 500, offset = 800 <- changed
        // <- 850
        // index = 3, size = 200, offset = ???
        // index = 4, size = 110, offset = ???

        assertEquals(
            100 + 200 + 500 + 2 * estimatedItemSize,
            cache.getScrollHeight(itemCount = 5)
        )

        assertEquals(
            1..3,
            cache.getVisibleIndexes(clientWindow = 250..850, itemCount = 5)
        )

        assertEquals(
            100 + 200 + 500 + 200 + estimatedItemSize,
            cache.getScrollHeight(itemCount = 5)
        )
    }

    @Test
    fun shouldReturnVisibleIndexesForFullyUnmeasuredClientWindow() {
        val cache = VirtualizationCache(estimatedItemSize)

        cache.setItemSize(index = 0, size = 100) // offset = 100
        cache.setItemSize(index = 1, size = 200) // offset = 300
        cache.setItemSize(index = 2, size = 300) // offset = 600
        // index = 3, size = estimated, offset = 700
        // index = 4, size = estimated, offset = 800
        // index = 5, size = estimated, offset = 900
        // <- 950
        // index = 6, size = estimated, offset = 1000
        // index = 7, size = estimated, offset = 1100
        // index = 8, size = estimated, offset = 1200
        // <- 1200
        // index = 9, size = estimated, offset = 1300
        // index = 10, size = estimated, offset = 1400

        assertEquals(
            6..9,
            cache.getVisibleIndexes(clientWindow = 950..1200, itemCount = 10)
        )
    }

    @Test
    fun shouldReturnVisibleIndexesForPartiallyUnmeasuredClientWindow() {
        val cache = VirtualizationCache(estimatedItemSize)

        cache.setItemSize(index = 0, size = 100) // offset = 100
        // <- 250
        cache.setItemSize(index = 1, size = 200) // offset = 300
        cache.setItemSize(index = 2, size = 300) // offset = 600
        // index = 3, size = estimated, offset = 700
        // index = 4, size = estimated, offset = 800
        // <- 850
        // index = 5, size = estimated, offset = 900

        assertEquals(
            1..5,
            cache.getVisibleIndexes(clientWindow = 250..850, itemCount = 10)
        )
    }

    @Test
    fun shouldReturnInitialOffset() {
        val cache = VirtualizationCache(estimatedItemSize)

        cache.setItemSize(index = 0, size = 100) // offset = 100
        // <- 250
        cache.setItemSize(index = 1, size = 200) // offset = 300
        cache.setItemSize(index = 2, size = 300) // offset = 600
        // <- 750
        cache.setItemSize(index = 3, size = 200) // offset = 800
        cache.setItemSize(index = 4, size = 100) // offset = 900

        val visibleIndexes = cache.getVisibleIndexes(clientWindow = 250..750, itemCount = 15)

        assertEquals(1..3, visibleIndexes)

        assertEquals(100 - 250, cache.getInitialOffset(scrollOffset = 250, visibleIndexes.first))
    }

    @Test
    fun shouldReturnInitialOffsetForEmptyList() {
        val cache = VirtualizationCache(estimatedItemSize)

        val visibleIndexes = cache.getVisibleIndexes(clientWindow = 0..0, itemCount = 1)

        assertEquals(IntRange.EMPTY, visibleIndexes)

        assertEquals(0, cache.getInitialOffset(scrollOffset = 0, visibleIndexes.first))
    }
}