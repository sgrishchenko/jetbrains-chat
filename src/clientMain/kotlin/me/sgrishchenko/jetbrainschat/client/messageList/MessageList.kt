package me.sgrishchenko.jetbrainschat.client.messageList

import kotlinx.css.*
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onScrollFunction
import me.sgrishchenko.jetbrainschat.client.*
import me.sgrishchenko.jetbrainschat.client.hooks.useResizeObserver
import me.sgrishchenko.jetbrainschat.client.loader.Loader
import me.sgrishchenko.jetbrainschat.client.loader.LoaderStyles
import me.sgrishchenko.jetbrainschat.client.messageItem.MessageItem
import me.sgrishchenko.jetbrainschat.client.virtualList.StaticItem
import me.sgrishchenko.jetbrainschat.client.virtualList.VirtualList
import org.w3c.dom.DOMRectReadOnly
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import react.*
import styled.css
import styled.styledButton
import styled.styledDiv

val MessageList = rFunction<RProps>("MessageList") {
    val (state, dispatch) = useReducer(::messagesReducer, messagesInitialState)
    val (contentRect, setContentRect) = useState<DOMRectReadOnly?>(null)
    val (scrollOffset, setScrollOffset) = useState(0)

    val content = useRef<Element?>(null)
    val scroll = useRef<Element?>(null)

    val offset = state.messages.size
    val isLoading = state.isLoading
    val loadingIsComplete = state.loadingIsComplete

    val loadChunk = useCallback({
        if (!isLoading) {
            dispatch(LoadMessages())

            fetchMessages(offset).then {
                dispatch(LoadMessagesDone(it))
            }
        }
    }, arrayOf(offset, isLoading))

    val onScroll = useCallback({ event: Event ->
        val element = event.currentTarget as HTMLElement
        setScrollOffset(element.scrollTop.toInt())
    }, arrayOf(setScrollOffset))

    useResizeObserver(content, setContentRect)

    useEffect(listOf(scroll, scrollOffset, loadingIsComplete, loadChunk)) {
        val element = scroll.current

        if (element != null) {
            val maxScrollOffset = element.scrollHeight - element.clientHeight
            val threshold = maxScrollOffset - 100

            if (scrollOffset > threshold && !loadingIsComplete) {
                loadChunk()
            }
        }
    }

    styledButton {
        css { +MessageListStyles.loadButton }
        attrs {
            onClickFunction = { loadChunk() }
        }
        +"Load More"
    }

    styledDiv {
        css { +MessageListStyles.container }

        styledDiv {
            ref = content
            css { +MessageListStyles.content }

            styledDiv {
                ref = scroll
                css {
                    height = 100.pct
                    overflow = Overflow.auto

                    put("willChange", "transform")
                }

                attrs {
                    onScrollFunction = onScroll
                }

                VirtualList {
                    attrs {
                        height = contentRect?.height?.toInt() ?: 0
                        this.scrollOffset = scrollOffset

                        itemCount = state.messages.size + 1
//                        overscanCount = 3
                        estimatedItemSize = MessageListStyles.estimatedItemSize + MessageListStyles.itemsGap

                        renderItem = { props ->
                            if (props.index == state.messages.size) {
                                StaticItem {
                                    attrs {
                                        size = LoaderStyles.loaderHeight
                                        updateSize = props.setItemSize
                                    }

                                    if (!loadingIsComplete) {
                                        Loader {}
                                    }
                                }
                            } else {
                                MessageItem {
                                    attrs {
                                        message = state.messages[props.index]
                                        updateSize = props.setItemSize
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
