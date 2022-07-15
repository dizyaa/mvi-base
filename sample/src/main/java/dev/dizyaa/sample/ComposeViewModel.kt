package dev.dizyaa.sample

import dev.dizyaa.mvicore.MviViewModel

class ComposeViewModel: MviViewModel<Contract.State, Contract.Event, Contract.Effect>() {
    override fun setInitialState() = Contract.State.Empty

    override fun handleEvents(event: Contract.Event) {
        when (event) {
            is Contract.Event.Increment -> {
                setState { copy(count = state.value.count + 1) }
                setEffect { Contract.Effect.ShowToast("Increment") }
            }
            is Contract.Event.Decrement -> {
                setState { copy(count = state.value.count - 1) }
                setEffect { Contract.Effect.ShowToast("Decrement") }
            }
        }
    }

    override fun onError(exception: Exception) {
        setEffect { Contract.Effect.ShowToast(exception.message ?: "Error") }
    }

    override fun onLoading(loading: Boolean) { }
}

sealed class Contract {
    data class State(
        val count: Int
    ) {
        companion object {
            val Empty = State(
                count = 0
            )
        }
    }

    sealed class Event {
        object Increment: Event()
        object Decrement: Event()
    }

    sealed class Effect {
        data class ShowToast(
            val text: String
        ): Effect()
    }
}