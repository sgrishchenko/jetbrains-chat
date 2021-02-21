package me.sgrishchenko.jetbrainschat.client.scroll

import kotlinext.js.js
import kotlinx.browser.document
import kotlinx.css.*
import kotlinx.html.js.onMouseDownFunction
import kotlinx.html.js.onWheelFunction
import me.sgrishchenko.jetbrainschat.client.hooks.useResizeObserver
import org.w3c.dom.DOMRectReadOnly
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent
import react.*
import react.dom.div
import react.dom.jsStyle
import styled.css
import styled.styledDiv
import kotlin.math.max
import kotlin.math.min

interface ScrollEvent {
    var scrollOffset: Int
    var scrollSize: Int
}

interface ScrollProps : RProps {
    var height: Int?
    var onScroll: (ScrollEvent) -> Unit
}

data class MoveScrollOffset(val delta: Int, val maxScrollOffset: Int)
data class SetScrollOffset(val value: Int, val maxScrollOffset: Int)

fun scrollOffsetReducer(state: Int, action: Any) =
    when (action) {
        is MoveScrollOffset -> max(0, min(action.maxScrollOffset, state + action.delta))
        is SetScrollOffset -> max(0, min(action.maxScrollOffset, action.value))
        else -> state
    }

val Scroll = rFunction<ScrollProps>("Scroll") { props ->
    val (scrollOffset, dispatch) = useReducer(::scrollOffsetReducer, initState = 0)
    val (contentRect, setContentRect) = useState<DOMRectReadOnly?>(null)

    val scrollHeight = contentRect?.height?.toInt() ?: 0
    val clientHeight = props.height ?: 0
    val onScroll = props.onScroll

    val scrollbarWidth = 10

    val scale = if (scrollHeight != 0) {
        clientHeight.toDouble() / scrollHeight.toDouble()
    } else {
        0.0
    }

    val content = useRef<Element?>(null)

    val onWheel = { event: Event ->
        val wheelEvent = event.unsafeCast<WheelEvent>()

        val delta = wheelEvent.deltaY.toInt()
        val maxScrollOffset = scrollHeight - clientHeight

        dispatch(MoveScrollOffset(delta, maxScrollOffset))
    }

    val onMouseDown = { downEvent: Event ->
        // prevent text selection and native drag&drop
        downEvent.preventDefault()

        val mouseDownEvent = downEvent.unsafeCast<MouseEvent>()

        val initialCoordinate = mouseDownEvent.clientY

        val onMouseMove = { moveEvent: Event ->
            val mouseMoveEvent = moveEvent.unsafeCast<MouseEvent>()

            val currentCoordinate = mouseMoveEvent.clientY
            val delta = ((currentCoordinate - initialCoordinate) / scale).toInt()

            val maxScrollOffset = scrollHeight - clientHeight
            dispatch(SetScrollOffset(scrollOffset + delta, maxScrollOffset))
        }

        val onMouseUp = { _: Event ->
            document.removeEventListener("mousemove", onMouseMove)
        }

        document.addEventListener("mousemove", onMouseMove)
        document.addEventListener("mouseup", onMouseUp, js { once = true })
    }

    useResizeObserver(content, setContentRect)

    useEffect(listOf(scrollOffset, scrollHeight, onScroll)) {
        onScroll(object : ScrollEvent {
            override var scrollOffset = scrollOffset
            override var scrollSize = scrollHeight
        })
    }

    styledDiv {
        css {
            position = Position.relative
            overflow = Overflow.hidden
        }

        attrs {
            jsStyle { height = clientHeight.px }

            onWheelFunction = onWheel
        }

        styledDiv {
            css {
                position = Position.absolute
                left = 0.px
                right = scrollbarWidth.px
            }

            attrs {
                jsStyle { top = -scrollOffset.px }
            }

            div {
                ref = content

                props.children()
            }
        }

        styledDiv {
            css {
                position = Position.absolute
                right = 0.px
                width = scrollbarWidth.px
                backgroundColor = Color.lightGray
            }

            attrs {
                jsStyle {
                    top = (scale * scrollOffset).px
                    height = (scale * clientHeight).px
                }

                onMouseDownFunction = onMouseDown
            }
        }
    }
}