package me.sgrishchenko.jetbrainschat.client

typealias MessageId = Long
typealias MessageSize = Int

data class MessageSizesState(
    val messageSizes: Map<MessageId, MessageSize>,
)

data class UpdateMessageSize(val pair: Pair<MessageId, MessageSize>)
data class ResetMessageSizes(val unit: Unit = Unit)

val messageSizesInitialState = MessageSizesState(
    messageSizes = emptyMap()
)

fun messageSizesReducer(state: MessageSizesState, action: Any) =
    when (action) {
        is UpdateMessageSize -> {
            val (messageId, messageSize) = action.pair
            val previousSize = state.messageSizes[messageId]

            if (messageSize != previousSize) {
                MessageSizesState(
                    messageSizes = state.messageSizes + action.pair
                )
            } else {
                state
            }

        }
        is ResetMessageSizes -> messageSizesInitialState
        else -> state
    }