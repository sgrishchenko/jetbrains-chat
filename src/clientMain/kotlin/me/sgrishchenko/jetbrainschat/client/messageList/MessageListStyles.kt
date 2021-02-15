package me.sgrishchenko.jetbrainschat.client.messageList

import kotlinx.css.*
import kotlinx.css.properties.deg
import kotlinx.css.properties.rotate
import kotlinx.css.properties.transform
import styled.StyleSheet

object MessageListStyles : StyleSheet("MessageListStyles", isStatic = true) {
    const val estimatedItemSize = 120
    const val itemsGap = 20

    val container by css {
        display = Display.flex
        justifyContent = JustifyContent.center
        height = 100.pct
    }

    val content by css {
        width = 600.px
        transform {
            rotate(180.deg)
        }
    }

    val item by css {
        marginLeft = 20.px
        direction = Direction.ltr
        transform {
            rotate(180.deg)
        }

        // react-window injects width: 100%,
        // that is why !important is used here
        put("width", "calc(100% - 40px) !important")
    }

    val loader by css {
        display = Display.flex
        justifyContent = JustifyContent.center
    }

    val loadButton by css {
        position = Position.fixed
        top = 10.px
        left = 10.px
    }
}
