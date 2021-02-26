package me.sgrishchenko.jetbrainschat.client.messageItem

import kotlinx.css.*
import styled.StyleSheet

object MessageItemStyles : StyleSheet("MessageItemStyles", isStatic = true) {
    const val itemPadding = 20
    const val itemGap = 20
    const val estimatedItemSize = 120 + itemGap

    val container by css {
        color = Color.white
        backgroundColor = Color.blueViolet

        padding(itemPadding.px)
        borderRadius = 40.px
        borderBottomLeftRadius = 0.px

        marginLeft = 20.px
        marginRight = 20.px
        marginBottom = itemGap.px
    }

    val header by css {
        display = Display.flex
        justifyContent = JustifyContent.spaceBetween
        marginBottom = 10.px
    }

    val nickname by css {
        fontWeight = FontWeight.bold
    }

    val time by css {
        color = Color.white.withAlpha(0.7)
        fontStyle = FontStyle.italic
        fontSize = 0.9.rem
    }
}
