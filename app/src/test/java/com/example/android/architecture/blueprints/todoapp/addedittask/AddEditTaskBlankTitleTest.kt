/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.addedittask

import androidx.lifecycle.SavedStateHandle
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.example.android.architecture.blueprints.todoapp.R.string
import com.example.android.architecture.blueprints.todoapp.data.FakeTaskRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * Saving a new task whose title is only spaces crashed the app (Crashlytics c41f7a2e:
 * NoSuchElementException "Char sequence is empty." in AddEditTaskViewModel.createNewTask).
 */
@ExperimentalCoroutinesApi
class AddEditTaskBlankTitleTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Test
    fun saveNewTask_titleOfOnlySpaces_showsEmptyTaskMessage() = runTest {
        val tasksRepository = FakeTaskRepository()
        val addEditTaskViewModel = AddEditTaskViewModel(tasksRepository, SavedStateHandle())
        addEditTaskViewModel.updateTitle("   ")
        addEditTaskViewModel.updateDescription("Buy milk")

        addEditTaskViewModel.saveTask()
        advanceUntilIdle()

        assertThat(addEditTaskViewModel.uiState.value.userMessage).isEqualTo(string.empty_task_message)
        assertThat(tasksRepository.savedTasks.value).isEmpty()
    }
}
