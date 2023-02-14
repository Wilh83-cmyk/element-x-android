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

package io.element.android.features.preferences.root

import io.element.android.features.rageshake.rageshake.RageshakeDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

const val A_SENSITIVITY = 1f

// TODO Remove this duplicated class when we will rework modules.
class FakeRageshakeDataStore(
    isEnabled: Boolean = true,
    sensitivity: Float = A_SENSITIVITY,
) : RageshakeDataStore {

    private val isEnabledFlow = MutableStateFlow(isEnabled)
    override fun isEnabled(): Flow<Boolean> = isEnabledFlow

    override suspend fun setIsEnabled(isEnabled: Boolean) {
        isEnabledFlow.value = isEnabled
    }

    private val sensitivityFlow = MutableStateFlow(sensitivity)
    override fun sensitivity(): Flow<Float> = sensitivityFlow

    override suspend fun setSensitivity(sensitivity: Float) {
        sensitivityFlow.value = sensitivity
    }

    override suspend fun reset() = Unit
}
