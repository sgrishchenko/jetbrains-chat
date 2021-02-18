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