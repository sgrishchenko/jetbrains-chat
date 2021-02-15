package me.sgrishchenko.jetbrainschat.client.browser

import org.w3c.dom.DOMRectReadOnly
import org.w3c.dom.Element

external interface ResizeObserverEntry {
    val contentRect: DOMRectReadOnly
}

typealias ResizeObserverCallback = (
    entries: Array<ResizeObserverEntry>,
    observer: ResizeObserver
) -> Unit

external class ResizeObserver(
    callback: ResizeObserverCallback
) {
    fun observe(targetElement: Element)
    fun unobserve(targetElement: Element)
}