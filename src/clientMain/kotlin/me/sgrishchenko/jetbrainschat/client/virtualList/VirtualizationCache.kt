package me.sgrishchenko.jetbrainschat.client.virtualList

class VirtualizationCache(private val estimatedItemSize: Int) {
    // TODO: parameterize overscanCount
    val overscanCount = 1

    private var itemSizes: MutableMap<Int, Int> = mutableMapOf()
    private var itemsOffsets: MutableList<Int> = mutableListOf()

    fun setItemSize(index: Int, size: Int) {
        if (itemSizes[index] == size) return

        itemSizes[index] = size

        if (index < itemsOffsets.size) {
            // invalidate cache
            itemsOffsets.subList(index, itemsOffsets.size).clear()
        }

        // fill gaps for interpolation
        while (itemsOffsets.size <= index) {
            val lastIndex = itemsOffsets.size
            val cachedSize = itemSizes[lastIndex] ?: estimatedItemSize

            val previousIndex = lastIndex - 1
            val previousOffset =
                if (previousIndex < 0) 0 else itemsOffsets[previousIndex]

            itemsOffsets.add(previousOffset + cachedSize)
        }
    }

    private val measuredHeight
        get() = itemsOffsets[itemsOffsets.size - 1]

    fun getScrollHeight(itemCount: Int): Int {
        val measuredCount = itemsOffsets.size
        val extrapolatedCount = itemCount - measuredCount
        val extrapolatedHeight = extrapolatedCount * estimatedItemSize

        return measuredHeight + extrapolatedHeight
    }

    fun getInitialOffset(): Int = TODO()

    fun getVisibleIndexes(clientWindow: IntRange): IntRange {
        val scrollOffset = clientWindow.first
        val clientHeight = clientWindow.last - scrollOffset

        if (scrollOffset > measuredHeight) {
            val lastMeasuredIndex = itemsOffsets.size - 1
            val estimatedItemCount = clientHeight / estimatedItemSize
            return lastMeasuredIndex..lastMeasuredIndex + estimatedItemCount
        }

        val indexRange = binaryIntersectionSearch(clientWindow)
        val middleIndex = (indexRange.first + indexRange.last) / 2


        val startIndexRange = binarySearch(clientWindow.first, indexRange.first..middleIndex)
        val endIndexRange = binarySearch(clientWindow.last, middleIndex..indexRange.last)

        return startIndexRange.last..endIndexRange.last
    }

    private fun binaryIntersectionSearch(valueRange: IntRange): IntRange {
        val startIndex = 0
        val endIndex = itemsOffsets.size - 1

        return binaryReduce(startIndex..endIndex) { current, middleIndex ->
            val middleOffset = itemsOffsets[middleIndex]

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
            val middleOffset = itemsOffsets[middleIndex]

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

//    fun getVisibleIndexes(clientWindow: IntRange): IntRange {
//        var scrollHeight = 0
//        var startIndex: Int? = null
//        var endIndex: Int? = null
//
//        for (index in 0 until itemCount) {
//            val itemInfo = items[index]
//            val itemSize = itemInfo?.size ?: estimatedItemSize
//
//            val itemStart = scrollHeight
//            scrollHeight += itemSize
//            val itemEnd = scrollHeight
//
//            if (startIndex == null) {
//                if (clientWindow.contains(itemStart)) {
//                    startIndex = index
//                }
//            } else if (endIndex == null) {
//                if (!clientWindow.contains(itemStart)) {
//                    endIndex = index
//                }
//            }
//        }
//
//        startIndex = max((startIndex ?: 0) - overscanCount, 0)
//        endIndex = min((endIndex ?: itemCount) + overscanCount, itemCount)
//
//        return startIndex..endIndex
//    }
}