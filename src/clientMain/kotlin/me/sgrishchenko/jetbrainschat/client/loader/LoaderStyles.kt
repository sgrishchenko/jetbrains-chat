package me.sgrishchenko.jetbrainschat.client.loader

import kotlinx.css.*
import kotlinx.css.properties.*
import styled.StyleSheet
import styled.animation

object LoaderStyles : StyleSheet("LoaderStyles", isStatic = true) {
    const val loaderHeight = 40

    private val showAnimation by css {
        animation(duration = 0.6.s, iterationCount = IterationCount.infinite) {
            from {
                transform { scale(0) }
            }
            to {
                transform { scale(1) }
            }
        }
    }

    private val moveAnimation by css {
        animation(duration = 0.6.s, iterationCount = IterationCount.infinite) {
            from {
                transform { translate(0.px, 0.px) }
            }
            to {
                transform { translate(24.px, 0.px) }
            }
        }
    }

    private val hideAnimation by css {
        animation(duration = 0.6.s, iterationCount = IterationCount.infinite) {
            from {
                transform { scale(1) }
            }
            to {
                transform { scale(0) }
            }
        }
    }

    val container by css {
        position = Position.relative
        width = 80.px
        height = loaderHeight.px
    }

    private val item by css {
        position = Position.absolute
        top = 13.px
        width = 13.px
        height = 13.px
        borderRadius = 50.pct
        backgroundColor = Color.blueViolet
    }

    val firstItem by css {
        left = 8.px
        +item
        +showAnimation
    }

    val secondItem by css {
        left = 8.px
        +item
        +moveAnimation
    }

    val thirdItem by css {
        left = 32.px
        +item
        +moveAnimation
    }

    val fourthItem by css {
        left = 56.px
        +item
        +hideAnimation
    }
}
