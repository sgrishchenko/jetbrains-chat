package me.sgrishchenko.jetbrainschat.client.messageList

import kotlinx.html.js.onClickFunction
import me.sgrishchenko.jetbrainschat.client.*
import me.sgrishchenko.jetbrainschat.client.hooks.useResizeObserver
import me.sgrishchenko.jetbrainschat.client.loader.Loader
import me.sgrishchenko.jetbrainschat.client.loader.LoaderStyles
import me.sgrishchenko.jetbrainschat.client.messageItem.MessageItem
import me.sgrishchenko.jetbrainschat.client.vendor.VariableSizeList
import me.sgrishchenko.jetbrainschat.client.vendor.VariableSizeListElement
import me.sgrishchenko.jetbrainschat.client.vendor.WindowChildProps
import me.sgrishchenko.jetbrainschat.client.vendor.WindowScrollProps
import org.w3c.dom.DOMRectReadOnly
import org.w3c.dom.Element
import react.*
import react.dom.jsStyle
import styled.css
import styled.styledButton
import styled.styledDiv

val MessageList = rFunction<RProps>("MessageList") {
    val (state, dispatch) = useReducer(::messagesReducer, messagesInitialState)
    val (cache, dispatchCache) = useReducer(::messageSizesReducer, messageSizesInitialState)
    val (contentRect, setContentRect) = useState<DOMRectReadOnly?>(null)

    val content = useRef<Element?>(null)
    val list = useRef<VariableSizeListElement?>(null)
    val outerListContainer = useRef<Element?>(null)

    val offset = state.messages.size
    val isLoading = state.isLoading
    val loadingIsComplete = state.loadingIsComplete

    val loadChunk = useCallback({
        if (!isLoading) {
            dispatch(LoadMessages())

            fetchMessages(offset).then {
                dispatch(LoadMessagesDone(it.toList()))
            }
        }
    }, arrayOf(offset, isLoading))

    val loadOnScroll = useCallback({ props: WindowScrollProps ->
        val element = outerListContainer.current
        if (element != null) {
            val maxScrollOffset = element.scrollHeight - element.clientHeight
            val threshold = maxScrollOffset - 100

            if (props.scrollOffset > threshold && !loadingIsComplete) {
                loadChunk()
            }
        }
    }, arrayOf(loadChunk, outerListContainer, loadingIsComplete))

    val getItemSize = useCallback({ index: Int ->
        if (index == state.messages.size) {
            LoaderStyles.loaderHeight
        } else {
            val message = state.messages[index]

            val itemSize = cache.messageSizes[message.id] ?: MessageListStyles.estimatedItemSize
            itemSize + MessageListStyles.itemsGap
        }
    }, arrayOf(state, cache))

    val updateItemSize = useCallback({ pair: Pair<MessageId, MessageSize> ->
        dispatchCache(UpdateMessageSize(pair))
    }, arrayOf(dispatchCache))

    // forced reset of react-window cache
    useEffect(listOf(getItemSize, list)) {
        list.current?.resetAfterIndex(0)
    }

    useResizeObserver(content, setContentRect)

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

            VariableSizeList {
                ref = list

                attrs {
                    height = contentRect?.height?.toInt() ?: 0
                    direction = "rtl"

                    // loader is additional item
                    itemCount = state.messages.size + 1
                    itemSize = getItemSize
                    estimatedItemSize = MessageListStyles.estimatedItemSize + MessageListStyles.itemsGap
                    overscanCount = 5

                    outerRef = outerListContainer

                    onScroll = loadOnScroll
                }

                childList += { props: WindowChildProps ->
                    buildElement {
                        if (props.index == state.messages.size) {
                            styledDiv {
                                css { +MessageListStyles.loader }
                                attrs {
                                    jsStyle = props.style
                                }

                                if (!loadingIsComplete) {
                                    Loader {}
                                }
                            }
                        } else {
                            val message = state.messages[props.index]

                            styledDiv {
                                css { +MessageListStyles.item }
                                attrs {
                                    jsStyle = props.style
                                }

                                MessageItem {
                                    attrs {
                                        messageId = message.id
                                        nickname = message.author.nickname
                                        text = message.text
                                        time = message.time

                                        updateSize = updateItemSize
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
