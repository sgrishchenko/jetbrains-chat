package me.sgrishchenko.jetbrainschat.client.messageList

import kotlinx.css.*
import styled.StyleSheet

object MessageListStyles : StyleSheet("MessageListStyles", isStatic = true) {
    val container by css {
        display = Display.flex
        justifyContent = JustifyContent.center
        height = 100.pct
    }

    val content by css {
        width = 600.px
    }

    val loadButton by css {
        position = Position.fixed
        top = 10.px
        left = 10.px
    }
}
