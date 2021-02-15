package me.sgrishchenko.jetbrainschat.model

import kotlinx.serialization.Serializable

@Serializable
data class User(val nickname: String)