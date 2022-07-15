package dev.dizyaa.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.Flow

class MainActivityCompose : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainScreenDestination()
        }
    }
}

@Composable
fun MainScreenDestination(
    viewModel: ComposeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    MainScreen(
        state = viewModel.state.collectAsState().value,
        effectFlow = viewModel.effect,
        onEventSend = { viewModel.handleEvents(it) }
    )
}

@Composable
fun MainScreen(
    state: Contract.State,
    effectFlow: Flow<Contract.Effect>?,
    onEventSend: (Contract.Event) -> Unit,
) {
    val context = LocalContext.current

    LaunchedEffect("key") {
        effectFlow?.collect { effect ->
            when (effect) {
                is Contract.Effect.ShowToast -> {
                    Toast.makeText(context, effect.text, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(
        Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            Text(text = state.count.toString())

            Row {
                Button(
                    onClick = { onEventSend(Contract.Event.Increment) }
                ) {
                    Text(text = "Increment")
                }

                Button(
                    onClick = { onEventSend(Contract.Event.Decrement) }
                ) {
                    Text(text = "Decrement")
                }
            }
        }
    }
}

@Composable
@Preview
private fun MainScreenPreview() {
    MainScreen(
        state = Contract.State.Empty,
        effectFlow = null,
        onEventSend = { }
    )
}