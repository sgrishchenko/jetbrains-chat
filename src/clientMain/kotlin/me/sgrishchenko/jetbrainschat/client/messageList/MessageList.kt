package me.sgrishchenko.jetbrainschat.client.messageList

import kotlinx.html.js.onClickFunction
import me.sgrishchenko.jetbrainschat.client.*
import me.sgrishchenko.jetbrainschat.client.hooks.useResizeObserver
import me.sgrishchenko.jetbrainschat.client.loader.Loader
import me.sgrishchenko.jetbrainschat.client.loader.LoaderStyles
import me.sgrishchenko.jetbrainschat.client.messageItem.MessageItem
import me.sgrishchenko.jetbrainschat.client.messageItem.MessageItemStyles
import me.sgrishchenko.jetbrainschat.client.scroll.Scroll
import me.sgrishchenko.jetbrainschat.client.scroll.ScrollEvent
import me.sgrishchenko.jetbrainschat.client.virtualList.StaticItem
import me.sgrishchenko.jetbrainschat.client.virtualList.VirtualList
import org.w3c.dom.DOMRectReadOnly
import org.w3c.dom.Element
import react.*
import styled.css
import styled.styledButton
import styled.styledDiv

val MessageList = rFunction<RProps>("MessageList") {
    val (state, dispatch) = useReducer(::messagesReducer, messagesInitialState)
    val (contentRect, setContentRect) = useState<DOMRectReadOnly?>(null)
    val (scrollOffset, setScrollOffset) = useState(0)
    val (initialized, setInitialized) = useState(false)

    val content = useRef<Element?>(null)

    val offset = state.messages.size
    val isLoading = state.isLoading
    val loadingIsComplete = state.loadingIsComplete

    val contentHeight = contentRect?.height?.toInt() ?: 0

    val loadChunk = useCallback({
        if (!isLoading) {
            dispatch(LoadMessages())

            fetchMessages(offset).then {
                dispatch(LoadMessagesDone(it))
            }
        }
    }, arrayOf(isLoading, offset, dispatch))

    useResizeObserver(content, setContentRect)

    val onScroll = useCallback({ event: ScrollEvent ->
        val maxScrollOffset = event.scrollSize - contentHeight
        val threshold = maxScrollOffset - 100

        if (
            event.scrollOffset != scrollOffset &&
            event.scrollOffset > threshold &&
            !loadingIsComplete
        ) {
            loadChunk()
        }

        setScrollOffset(event.scrollOffset)
    }, arrayOf(contentHeight, scrollOffset, loadingIsComplete, loadChunk, setScrollOffset))

    useEffect(listOf(loadChunk, initialized, setInitialized)) {
        if (!initialized) {
            setInitialized(true)
            loadChunk()
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

            Scroll {
                attrs {
                    height = contentHeight
                    this.onScroll = onScroll

                    VirtualList {
                        attrs {
                            height = contentHeight
                            this.scrollOffset = scrollOffset

                            itemCount = state.messages.size + 1
                            estimatedItemSize = MessageItemStyles.estimatedItemSize

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
}

