package me.sgrishchenko.jetbrainschat.client

import me.sgrishchenko.jetbrainschat.model.Message

data class MessagesState(
    val messages: List<Message>,
    val isLoading: Boolean,
    val loadingIsComplete: Boolean
)

data class LoadMessages(val unit: Unit = Unit)
data class LoadMessagesDone(val messages: List<Message>)

val messagesInitialState = MessagesState(
    messages = emptyList(),
    isLoading = false,
    loadingIsComplete = false,
)

fun messagesReducer(state: MessagesState, action: Any) =
    when (action) {
        is LoadMessages -> MessagesState(
            messages = state.messages,
            isLoading = true,
            loadingIsComplete = state.loadingIsComplete,
        )
        is LoadMessagesDone -> MessagesState(
            messages = state.messages + action.messages,
            isLoading = false,
            loadingIsComplete = action.messages.isEmpty(),
        )
        else -> state
    }