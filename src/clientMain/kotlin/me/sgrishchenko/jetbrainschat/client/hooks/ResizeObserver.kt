package me.sgrishchenko.jetbrainschat.client.hooks

import me.sgrishchenko.jetbrainschat.client.browser.ResizeObserver
import org.w3c.dom.DOMRectReadOnly
import org.w3c.dom.Element
import react.RMutableRef
import react.useEffectWithCleanup

fun useResizeObserver(
    target: RMutableRef<Element?>,
    callback: (rect: DOMRectReadOnly) -> Unit
) {
    useEffectWithCleanup(listOf(target, callback)) {
        val observer = ResizeObserver { entries, _ ->
            for (entry in entries) {
                callback(entry.contentRect)
            }
        }

        val element = target.current

        if (element != null) observer.observe(element);

        {
            if (element != null) observer.unobserve(element)
        }
    }
}