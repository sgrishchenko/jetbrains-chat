package me.sgrishchenko.jetbrainschat.client.messageItem

import me.sgrishchenko.jetbrainschat.client.formatTime
import me.sgrishchenko.jetbrainschat.client.hooks.useResizeObserver
import me.sgrishchenko.jetbrainschat.model.Message
import org.w3c.dom.DOMRectReadOnly
import org.w3c.dom.Element
import react.*
import styled.css
import styled.styledDiv

interface MessageItemProps : RProps {
    var message: Message
    var updateSize: (size: Int) -> Unit
}

val MessageItem = rFunction<MessageItemProps>("MessageItem") { props ->
    val message = props.message
    val updateSize = props.updateSize
    val container = useRef<Element?>(null)
    val previousSize = useRef<Int?>(null)

    val handleResize = useCallback({ rect: DOMRectReadOnly ->
        val messageSize = rect.height.toInt() +
            2 * MessageItemStyles.itemPadding +
            MessageItemStyles.itemGap

        if (messageSize != previousSize.current) {
            previousSize.current = messageSize
            updateSize(messageSize)
        }
    }, arrayOf(previousSize, updateSize))

    useResizeObserver(container, handleResize)

    styledDiv {
        ref = container
        css { +MessageItemStyles.container }
        styledDiv {
            css { +MessageItemStyles.header }
            styledDiv {
                css { +MessageItemStyles.nickname }
                +"@${message.author.nickname}"
            }
            styledDiv {
                css { +MessageItemStyles.time }
                +formatTime(message.time)
            }
        }
        +message.text
    }
}