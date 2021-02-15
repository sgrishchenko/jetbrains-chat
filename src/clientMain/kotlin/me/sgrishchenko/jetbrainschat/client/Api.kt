package me.sgrishchenko.jetbrainschat.client

import kotlinx.browser.window
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import me.sgrishchenko.jetbrainschat.model.Message

fun fetchMessages(offset: Int) =
    window.fetch("/messages?offset=$offset")
        .then { it.text() }
        .then { response ->
            Json.decodeFromString(
                ListSerializer(Message.serializer()),
                response
            )
        }
