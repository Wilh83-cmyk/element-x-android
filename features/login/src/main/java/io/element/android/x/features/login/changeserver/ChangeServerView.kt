/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.x.features.login.changeserver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.x.architecture.Async
import io.element.android.x.core.compose.textFieldState
import io.element.android.x.designsystem.ElementXTheme
import io.element.android.x.designsystem.components.VectorIcon
import io.element.android.x.features.login.R
import io.element.android.x.features.login.error.changeServerError

@Composable
fun ChangeServerView(
    state: ChangeServerState,
    modifier: Modifier = Modifier,
    onChangeServerSuccess: () -> Unit = {},
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background,
    ) {
        val eventSink = state.eventSink
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(
                        state = scrollState,
                    )
                    .padding(horizontal = 16.dp)
            ) {
                val isError = state.changeServerAction is Async.Failure
                Box(
                    modifier = Modifier
                        .padding(top = 99.dp)
                        .size(width = 81.dp, height = 73.dp)
                        .align(Alignment.CenterHorizontally)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(32.dp)
                        )
                ) {
                    VectorIcon(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(width = 48.dp, height = 48.dp),
                        // TODO Update with design input
                        resourceId = R.drawable.ic_baseline_dataset_24,
                    )
                }
                Text(
                    text = "Your server",
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 38.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                )
                Text(
                    text = "A server is a home for all your data.\n" +
                        "You choose your server and it’s easy to make one.", // TODO "Learn more.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                var homeserverFieldState by textFieldState(stateValue = state.homeserver)
                OutlinedTextField(
                    value = homeserverFieldState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 200.dp),
                    onValueChange = {
                        homeserverFieldState = it
                        eventSink(ChangeServerEvents.SetServer(it))
                    },
                    label = {
                        Text(text = "Server")
                    },
                    isError = isError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { eventSink(ChangeServerEvents.Submit) }
                    )
                )
                if (state.changeServerAction is Async.Failure) {
                    Text(
                        text = changeServerError(
                            state.homeserver,
                            state.changeServerAction.error
                        ),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Button(
                    onClick = { eventSink(ChangeServerEvents.Submit) },
                    enabled = state.submitEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 44.dp)
                ) {
                    Text(text = "Continue")
                }
                if (state.changeServerAction is Async.Success) {
                    onChangeServerSuccess()
                }
            }
            if (state.changeServerAction is Async.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
@Preview
fun ChangeServerContentPreview() {
    ElementXTheme {
        ChangeServerView(
            state = ChangeServerState(homeserver = "matrix.org"),
        )
    }
}