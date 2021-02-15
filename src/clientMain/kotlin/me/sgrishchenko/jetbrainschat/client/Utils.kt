package me.sgrishchenko.jetbrainschat.client

import kotlin.js.Date

fun formatTime(time: Long): String {
    val date = Date(time)

    return listOf(
        date.getHours(),
        date.getMinutes(),
        date.getSeconds()
    )
        .joinToString(":") {
            it.toString().padStart(2, '0')
        }
}
