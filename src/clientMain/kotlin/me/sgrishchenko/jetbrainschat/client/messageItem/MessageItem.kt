package me.sgrishchenko.jetbrainschat.client.messageItem

import me.sgrishchenko.jetbrainschat.client.MessageId
import me.sgrishchenko.jetbrainschat.client.MessageSize
import me.sgrishchenko.jetbrainschat.client.formatTime
import me.sgrishchenko.jetbrainschat.client.hooks.useResizeObserver
import org.w3c.dom.DOMRectReadOnly
import org.w3c.dom.Element
import react.*
import styled.css
import styled.styledDiv

interface MessageProps : RProps {
    var messageId: Long
    var nickname: String
    var text: String
    var time: Long

    var updateSize: (Pair<MessageId, MessageSize>) -> Unit
}

val MessageItem = rFunction<MessageProps>("MessageItem") { props ->
    val messageId = props.messageId
    val updateSize = props.updateSize
    val container = useRef<Element?>(null)

    val handleResize = useCallback({ rect: DOMRectReadOnly ->
        val messageSize = rect.height.toInt() + 2 * MessageItemStyles.itemsPadding
        updateSize(messageId to messageSize)
    }, arrayOf(messageId, updateSize))

    useResizeObserver(container, handleResize)

    styledDiv {
        ref = container
        css { +MessageItemStyles.container }
        styledDiv {
            css { +MessageItemStyles.header }
            styledDiv {
                css { +MessageItemStyles.nickname }
                +"@${props.nickname}"
            }
            styledDiv {
                css { +MessageItemStyles.time }
                +formatTime(props.time)
            }
        }
        +props.text
    }
}