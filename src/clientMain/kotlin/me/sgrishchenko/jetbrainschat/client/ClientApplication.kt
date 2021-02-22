package me.sgrishchenko.jetbrainschat.client

import react.dom.render
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.css.*
import me.sgrishchenko.jetbrainschat.client.messageList.MessageList
import styled.injectGlobal

val styles = CSSBuilder().apply {
    body {
        margin(0.px)
    }

    "html, body, #root" {
        height = 100.pct
    }
}


fun main() {
    window.onload = {
        injectGlobal(styles.toString())

        render(document.getElementById("root")) {
            MessageList {}
        }
    }
}
