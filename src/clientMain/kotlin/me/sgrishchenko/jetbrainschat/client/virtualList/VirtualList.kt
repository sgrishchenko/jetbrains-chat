package me.sgrishchenko.jetbrainschat.client.virtualList

import kotlinx.css.*
import react.*
import react.dom.jsStyle
import styled.css
import styled.styledDiv
import kotlin.math.max
import kotlin.math.min

interface VirtualListRenderProps : RProps {
    var index: Int
    var setItemSize: (size: Int) -> Unit
}

interface VirtualListProps : RProps {
    var height: Int?
    var scrollOffset: Int?

    var itemCount: Int?
    var estimatedItemSize: Int?
    var overscanCount: Int?

    var renderItem: RBuilder.(VirtualListRenderProps) -> Unit
}

data class UpdateItemSize(val key: Int, val size: Int)
data class ResetItemsSizes(val unit: Unit = Unit)

fun itemSizesReducer(state: Map<Int, Int>, action: Any) =
    when (action) {
        is UpdateItemSize -> {
            val key = action.key
            val nextSize = action.size
            val previousSize = state[key]

            if (nextSize != previousSize) {
                state + (key to nextSize)
            } else {
                state
            }
        }
        is ResetItemsSizes -> emptyMap()
        else -> state
    }

val VirtualList = rFunction<VirtualListProps>("VirtualList") { props ->
    val clientHeight = props.height ?: 0
    val scrollOffset = props.scrollOffset ?: 0
    val itemCount = props.itemCount ?: 0
    val overscanCount = props.overscanCount ?: 1
    val estimatedItemSize = props.estimatedItemSize ?: 0
    val renderItem = props.renderItem

    val (sizes, dispatch) = useReducer(::itemSizesReducer, emptyMap())

    var scrollHeight = 0
    var initialOffset: Int? = null
    var startIndex: Int? = null
    var endIndex: Int? = null

    val clientWindow = scrollOffset..scrollOffset + clientHeight

    for (index in 0 until itemCount) {
        val itemSize = sizes[index] ?: estimatedItemSize

        val itemStart = scrollHeight
        scrollHeight += itemSize
        val itemEnd = scrollHeight

        if (initialOffset == null) {
            if (clientWindow.contains(itemEnd)) {
                initialOffset = itemStart - scrollOffset
            }
        }

        if (startIndex == null) {
            if (clientWindow.contains(itemStart)) {
                startIndex = index
            }
        } else if (endIndex == null) {
            if (!clientWindow.contains(itemStart)) {
                endIndex = index
            }
        }
    }

    initialOffset = initialOffset ?: 0
    // TODO: fix overscan
    startIndex = max((startIndex ?: 0) - overscanCount, 0)
    endIndex = min((endIndex ?: itemCount) + overscanCount, itemCount)

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

            for (index in startIndex until endIndex) {
                child(functionalComponent(renderItem)) {
                    key = index.toString()
                    attrs {
                        this.index = index
                        setItemSize = {
                            dispatch(UpdateItemSize(index, it))
                        }
                    }
                }
            }
        }
    }
}