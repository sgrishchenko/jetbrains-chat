package me.sgrishchenko.jetbrainschat.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: Long,
    val author: User,
    val text: String,
    val time: Long
)