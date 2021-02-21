package me.sgrishchenko.jetbrainschat.server

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import me.sgrishchenko.jetbrainschat.model.Message
import me.sgrishchenko.jetbrainschat.model.User
import java.io.File
import java.time.Duration

private fun loadStringList(resource: String) = Json.decodeFromString(
    ListSerializer(String.serializer()),
    File(ClassLoader.getSystemResource(resource).file).readText()
)

private val NICKNAME_LIST = loadStringList("nicknames.json")

private val TEXT_LIST = loadStringList("texts.json")

private val LAST_MESSAGE_TIME = System.currentTimeMillis()

fun generateNickname() = NICKNAME_LIST.random()

fun generateText() = TEXT_LIST.random()

fun generateTime(index: Int): Long {
    val minute = Duration.ofMinutes(1).toMillis()
    return LAST_MESSAGE_TIME - index * minute - (0..minute / 2).random()
}

fun generateMessage(index: Int) = Message(
    id = index.toLong(),
    User(generateNickname()),
    generateText(),
    generateTime(index)
)
