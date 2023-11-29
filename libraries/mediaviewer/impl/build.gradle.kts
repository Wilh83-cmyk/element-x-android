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
plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.anvil)
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.libraries.mediaviewer.impl"
}

anvil {
    generateDaggerFactories.set(true)
}

dependencies {
    anvil(projects.anvilcodegen)
    implementation(projects.anvilannotations)

    implementation(libs.coroutines.core)
    implementation(libs.dagger)

    api(projects.libraries.mediaviewer.api)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    implementation(projects.libraries.matrix.api)

    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.mediaviewer.test)
    testImplementation(projects.tests.testutils)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.turbine)
    testImplementation(libs.coroutines.core)
    testImplementation(libs.coroutines.test)
}