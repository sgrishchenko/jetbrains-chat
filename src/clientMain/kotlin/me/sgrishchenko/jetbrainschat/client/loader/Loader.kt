package me.sgrishchenko.jetbrainschat.client.loader

import react.RProps
import react.rFunction
import styled.css
import styled.styledDiv

val Loader = rFunction<RProps>("Loader") {
    styledDiv {
        css { +LoaderStyles.container }

        styledDiv {
            css { +LoaderStyles.content }

            styledDiv {
                css { +LoaderStyles.firstItem }
            }
            styledDiv {
                css { +LoaderStyles.secondItem }
            }
            styledDiv {
                css { +LoaderStyles.thirdItem }
            }
            styledDiv {
                css { +LoaderStyles.fourthItem }
            }
        }
    }
}
