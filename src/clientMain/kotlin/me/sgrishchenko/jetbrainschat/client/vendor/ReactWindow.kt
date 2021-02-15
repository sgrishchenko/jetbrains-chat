@file:JsModule("react-window")
@file:JsNonModule

package me.sgrishchenko.jetbrainschat.client.vendor

import react.*

external interface WindowScrollProps {
    val scrollOffset: Int
    val scrollUpdateWasRequested: Boolean
}

external interface WindowChildProps {
    val index: Int
    val style: dynamic
}

external interface VariableSizeListProps : RProps {
    var width: Int
    var height: Int
    var direction: String

    var itemCount: Int
    var itemSize: (index: Int) -> Int
    var estimatedItemSize: Int
    var overscanCount: Int

    var innerRef: RRef
    var outerRef: RRef

    var onScroll: (props: WindowScrollProps) -> Unit
}

external interface VariableSizeListElement {
    fun scrollTo(scrollOffset: Int)
    fun resetAfterIndex(index: Int)
}

@JsName("VariableSizeList")
external val VariableSizeList: RClass<VariableSizeListProps>