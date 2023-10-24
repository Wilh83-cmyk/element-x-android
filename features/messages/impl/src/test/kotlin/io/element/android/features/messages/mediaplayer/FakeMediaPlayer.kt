/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.messages.mediaplayer

import io.element.android.features.messages.impl.mediaplayer.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Fake implementation of [MediaPlayer] for testing purposes.
 */
class FakeMediaPlayer : MediaPlayer {
    private val _state = MutableStateFlow(MediaPlayer.State(false, null, 0L))

    override val state: StateFlow<MediaPlayer.State> = _state.asStateFlow()

    override fun acquireControlAndPlay(uri: String, mediaId: String, mimeType: String) {
        _state.update {
            it.copy(
                isPlaying = true,
                mediaId = mediaId,
                currentPosition = it.currentPosition + 1000L,
            )
        }
    }

    override fun play() {
        _state.update {
            it.copy(
                isPlaying = true,
                currentPosition = it.currentPosition + 1000L,
            )
        }
    }

    override fun pause() {
        _state.update {
            it.copy(
                isPlaying = false,
            )
        }
    }

    override fun seekTo(positionMs: Long) {
        _state.update {
            it.copy(
                currentPosition = positionMs,
            )
        }
    }

    override fun close() {
        // no-op
    }
}