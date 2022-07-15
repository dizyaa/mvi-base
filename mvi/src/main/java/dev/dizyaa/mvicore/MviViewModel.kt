package dev.dizyaa.mvicore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class MviViewModel<State, Event, Effect>: ViewModel() {

    abstract fun setInitialState(): State
    abstract fun handleEvents(event: Event)

    private val initialState: State by lazy { setInitialState() }

    private val _state: MutableStateFlow<State> = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state

    private val _event: MutableSharedFlow<Event> = MutableSharedFlow()

    private val _effect: Channel<Effect> = Channel()
    val effect: Flow<Effect> = _effect.receiveAsFlow()

    private val loadingFlags: MutableSet<Int> = mutableSetOf()

    init {
        subscribeToEvents()
    }

    fun setEvent(event: Event) {
        viewModelScope.launch { _event.emit(event) }
    }

    fun setState(reducer: State.() -> State) {
        val newState = _state.value.reducer()
        _state.value = newState
    }

    fun setEffect(builder: () -> Effect) {
        val effectValue = builder()
        viewModelScope.launch { _effect.send(effectValue) }
    }

    abstract fun onError(exception: Exception)
    abstract fun onLoading(loading: Boolean)

    protected fun <T> makeRequest(
        onLoadingChange: (Boolean) -> Unit = {},
        onFailure: (Throwable) -> Unit = { onError(it as Exception) },
        withIndication: Boolean = true,
        request: suspend () -> (T),
    ) {
        val flag = request.hashCode()

        viewModelScope.launch {
            try {
                if (withIndication) addLoadingFlag(flag)
                onLoadingChange(true)
                request()
            } catch (ex: Exception) {
                onFailure(ex)
            } finally {
                if (withIndication) removeLoadingFlag(flag)
                onLoadingChange(false)
            }
        }
    }

    private fun addLoadingFlag(flag: Int) {
        loadingFlags.add(flag)
        onLoading(true)
    }

    private fun removeLoadingFlag(flag: Int) {
        loadingFlags.remove(flag)
        if (loadingFlags.isEmpty()) onLoading(false)
    }

    private fun subscribeToEvents() {
        viewModelScope.launch {
            _event.collect {
                handleEvents(it)
            }
        }
    }
}