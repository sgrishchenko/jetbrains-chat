package me.sgrishchenko.jetbrainschat.client.virtualList

import kotlin.math.max
import kotlin.math.min

class VirtualizationCache(private val estimatedItemSize: Int) {
    private var itemSizes: MutableMap<Int, Int> = mutableMapOf()
    private var itemOffsets: MutableList<Int> = mutableListOf()

    private val measuredHeight
        get() = if (itemOffsets.isNotEmpty()) {
            itemOffsets[itemOffsets.size - 1]
        } else {
            0
        }

    fun setItemSize(index: Int, size: Int) {
        if (itemSizes[index] == size) return

        itemSizes[index] = size

        if (index < itemOffsets.size) {
            // invalidate cache
            itemOffsets.subList(index, itemOffsets.size).clear()
        }

        // fill gaps for interpolation
        fillOffsetGaps(index)
    }

    private fun fillOffsetGaps(index: Int) {
        while (itemOffsets.size <= index) {
            val lastIndex = itemOffsets.size
            val cachedSize = itemSizes[lastIndex] ?: estimatedItemSize

            val previousIndex = lastIndex - 1
            val previousOffset =
                if (previousIndex < 0) 0 else itemOffsets[previousIndex]

            itemOffsets.add(previousOffset + cachedSize)
        }
    }

    private fun actualizeOffsets(bound: Int) {
        while (measuredHeight < bound) {
            val lastIndex = itemOffsets.size

            if (itemSizes[lastIndex] == null) break

            fillOffsetGaps(lastIndex)
        }
    }

    fun getScrollHeight(itemCount: Int): Int {
        val measuredCount = itemOffsets.size
        val extrapolatedCount = itemCount - measuredCount
        val extrapolatedHeight = extrapolatedCount * estimatedItemSize

        return measuredHeight + extrapolatedHeight
    }

    fun getVisibleIndexes(clientWindow: IntRange, itemCount: Int): IntRange {
        // cache can be invalidated after item size setting
        actualizeOffsets(clientWindow.last)

        if (itemOffsets.isEmpty()) return IntRange.EMPTY

        with(handleFullyUnmeasuredClientWindow(clientWindow)) {
            if (this != null) return limitVisibleIndexes(this, itemCount)
        }

        val (measuredClientWindow, estimatedItemCount) =
            handlePartiallyUnmeasuredClientWindow(clientWindow)

        val indexRange = binaryIntersectionSearch(measuredClientWindow)
        val middleIndex = (indexRange.first + indexRange.last) / 2

        val startIndexRange = binarySearch(measuredClientWindow.first, indexRange.first..middleIndex)
        val endIndexRange = binarySearch(measuredClientWindow.last, middleIndex..indexRange.last)

        val startIndex = startIndexRange.last
        val endIndex = endIndexRange.last + estimatedItemCount

        return limitVisibleIndexes(startIndex..endIndex, itemCount)
    }

    private fun limitVisibleIndexes(visibleIndexes: IntRange, itemCount: Int): IntRange {
        val startIndex = max(visibleIndexes.first, 0)
        val endIndex = min(visibleIndexes.last, itemCount - 1)

        return startIndex..endIndex
    }

    private fun handleFullyUnmeasuredClientWindow(clientWindow: IntRange): IntRange? {
        val scrollOffset = clientWindow.first
        val scrollOffsetBound = clientWindow.last

        val clientWindowIsFullyUnmeasured = scrollOffset > measuredHeight
        if (clientWindowIsFullyUnmeasured) {
            val invisibleHeight = scrollOffset - measuredHeight
            val estimatedInvisibleItemCount = (invisibleHeight / estimatedItemSize) + 1

            val clientHeight = scrollOffsetBound - scrollOffset
            val estimatedVisibleItemCount = (clientHeight / estimatedItemSize) + 1

            val lastMeasuredIndex = itemOffsets.size - 1
            val startIndex = lastMeasuredIndex + estimatedInvisibleItemCount
            val endIndex = startIndex + estimatedVisibleItemCount

            return startIndex..endIndex
        }

        return null
    }

    private fun handlePartiallyUnmeasuredClientWindow(clientWindow: IntRange): Pair<IntRange, Int> {
        val scrollOffset = clientWindow.first
        val scrollOffsetBound = clientWindow.last

        var measuredClientWindow = clientWindow
        var estimatedItemCount = 0

        val clientWindowIsPartiallyUnmeasured = scrollOffsetBound > measuredHeight
        if (clientWindowIsPartiallyUnmeasured) {
            measuredClientWindow = scrollOffset..measuredHeight
            val unmeasuredClientHeight = scrollOffsetBound - measuredHeight
            estimatedItemCount = (unmeasuredClientHeight / estimatedItemSize) + 1
        }

        return measuredClientWindow to estimatedItemCount
    }

    private fun binaryIntersectionSearch(valueRange: IntRange): IntRange {
        val startIndex = 0
        val endIndex = itemOffsets.size - 1

        return binaryReduce(startIndex..endIndex) { current, middleIndex ->
            val middleOffset = itemOffsets[middleIndex]

            when {
                valueRange.first > middleOffset -> {
                    middleIndex..current.last
                }
                valueRange.last < middleOffset -> {
                    current.first..middleIndex
                }
                else -> current
            }
        }
    }

    private fun binarySearch(value: Int, indexRange: IntRange): IntRange {
        return binaryReduce(indexRange) { current, middleIndex ->
            val middleOffset = itemOffsets[middleIndex]

            if (value > middleOffset) {
                middleIndex..current.last
            } else {
                current.first..middleIndex
            }
        }
    }

    private fun binaryReduce(
        initialIndexRange: IntRange,
        operation: (indexRange: IntRange, middleIndex: Int) -> IntRange
    ): IntRange {
        var startIndex = initialIndexRange.first
        var endIndex = initialIndexRange.last

        var previousStartIndex: Int? = null
        var previousEndIndex: Int? = null

        while (
            startIndex != previousStartIndex ||
            endIndex != previousEndIndex
        ) {
            val middleIndex = (startIndex + endIndex) / 2

            previousStartIndex = startIndex
            previousEndIndex = endIndex

            val result = operation(startIndex..endIndex, middleIndex)

            startIndex = result.first
            endIndex = result.last
        }

        return startIndex..endIndex
    }

    fun getInitialOffset(scrollOffset: Int, startIndex: Int): Int {
        if (startIndex >= itemOffsets.size) return 0

        val itemSize = itemSizes[startIndex] ?: estimatedItemSize
        val itemOffset = itemOffsets[startIndex]
        val itemStart = itemOffset - itemSize

        return itemStart - scrollOffset
    }
}