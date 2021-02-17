package me.sgrishchenko.jetbrainschat.client.virtualList

import react.RProps
import react.rFunction
import react.useEffect

interface StaticItemProps : RProps {
    var size: Int
    var updateSize: (size: Int) -> Unit
}

val StaticItem = rFunction<StaticItemProps>("StaticItem") { props ->
    val size = props.size
    val updateSize = props.updateSize

    useEffect(listOf(updateSize)) {
        updateSize(size)
    }

    props.children()
}
