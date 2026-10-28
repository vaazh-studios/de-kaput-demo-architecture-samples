/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.lifecycle.SavedStateHandle
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.example.android.architecture.blueprints.todoapp.data.FakeTaskRepository
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * The task list when some of its tasks have no title (synced from other clients).
 *
 * Crashlytics issue 9f2c6b8e1d4a4f7c8e0b3a5d7c9e1f20: opening the list crashed with
 * "Comparison method violates its general contract!" once it held enough tasks for TimSort to
 * merge runs and some of them were untitled.
 */
@ExperimentalCoroutinesApi
class TaskListWithUntitledTasksTest {

    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Test
    fun longListWithUntitledTasks_showsActiveFirstThenByTitleWithUntitledLast() {
        // 64 tasks in the order they synced: every third one untitled, every fifth one completed.
        val tasks = (0 until 64).map { i ->
            Task(
                id = "task-$i",
                title = if (i % 3 == 0) "" else "Task ${(i * 7) % 64}",
                description = "Synced task $i",
                isCompleted = i % 5 == 4,
            )
        }
        val repository = FakeTaskRepository().apply { addTasks(*tasks.toTypedArray()) }
        val viewModel = TasksViewModel(repository, SavedStateHandle())

        // The list is sorted in viewModelScope, outside the test body, so runTest reports a crash
        // there when it returns: read the shown list inside it and check it afterwards.
        var shown: List<Task> = emptyList()
        runTest {
            shown = viewModel.uiState.first().items
        }

        // Active tasks, then completed ones; each group by title ignoring case, untitled last.
        val (active, completed) = tasks.partition { it.isActive }
        val expected = listOf(active, completed).flatMap { group ->
            val (untitled, titled) = group.partition { it.title.isBlank() }
            titled.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title }) + untitled
        }
        assertThat(shown).containsExactlyElementsIn(expected).inOrder()
    }
}
