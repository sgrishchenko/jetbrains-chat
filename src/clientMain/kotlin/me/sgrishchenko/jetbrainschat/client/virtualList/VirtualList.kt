package me.sgrishchenko.jetbrainschat.client.virtualList

import kotlinx.css.*
import react.*
import react.dom.jsStyle
import styled.css
import styled.styledDiv

interface VirtualListRenderProps : RProps {
    var index: Int
    var setItemSize: (size: Int) -> Unit
}

interface VirtualListProps : RProps {
    var height: Int?
    var scrollOffset: Int?

    var itemCount: Int?
    var estimatedItemSize: Int?

    var renderItem: RBuilder.(VirtualListRenderProps) -> Unit
}

data class VirtualizationValues(
    val visibleIndexes: IntRange,
    val scrollHeight: Int,
    val initialOffset: Int,
)

typealias ItemSizeConsumer = (index: Int, size: Int) -> Unit

fun useVirtualizationCache(
    clientWindow: IntRange,
    itemCount: Int,
    estimatedItemSize: Int
): Pair<VirtualizationValues, ItemSizeConsumer> {
    val (cache) = useState { VirtualizationCache(estimatedItemSize) }

    // order is important, because there is an offsets actualization
    // inside visible indexes calculation
    val visibleIndexes = cache.getVisibleIndexes(clientWindow, itemCount)

    val scrollHeight = cache.getScrollHeight(itemCount)
    val initialOffset = cache.getInitialOffset(
        scrollOffset = clientWindow.first,
        startIndex = visibleIndexes.first
    )

    val itemSizeConsumer = useCallback<ItemSizeConsumer>({ index, size ->
        cache.setItemSize(index, size)
    }, arrayOf(cache))

    useEffect(listOf(itemSizeConsumer)) {
        // initialize measurement
        itemSizeConsumer(0, 0)
    }

    return Pair(
        VirtualizationValues(
            visibleIndexes,
            scrollHeight,
            initialOffset,
        ),
        itemSizeConsumer
    )
}

val VirtualList = rFunction<VirtualListProps>("VirtualList") { props ->
    val clientHeight = props.height ?: 0
    val scrollOffset = props.scrollOffset ?: 0
    val itemCount = props.itemCount ?: 0
    val estimatedItemSize = props.estimatedItemSize ?: 0
    val renderItem = props.renderItem

    val clientWindow = scrollOffset..scrollOffset + clientHeight

    val (virtualizationValues, setItemSize) = useVirtualizationCache(
        clientWindow,
        itemCount,
        estimatedItemSize
    )

    val (visibleIndexes, scrollHeight, initialOffset) = virtualizationValues

    styledDiv {
        css {
            position = Position.relative
            overflow = Overflow.hidden
        }

        attrs {
            jsStyle { height = scrollHeight.px }
        }

        styledDiv {
            css {
                position = Position.absolute
                left = 0.px
                right = 0.px
            }

            attrs {
                jsStyle { top = (scrollOffset + initialOffset).px }
            }

            for (index in visibleIndexes) {
                Fragment {
                    attrs {
                        key = index.toString()
                    }

                    renderItem(object : VirtualListRenderProps {
                        override var index = index
                        override var setItemSize = { size: Int ->
                            setItemSize(index, size)
                        }
                    })
                }
            }
        }
    }
}