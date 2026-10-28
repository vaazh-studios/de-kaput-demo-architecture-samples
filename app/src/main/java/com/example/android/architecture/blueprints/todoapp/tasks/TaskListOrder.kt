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

package com.example.android.architecture.blueprints.todoapp.tasks

import com.example.android.architecture.blueprints.todoapp.data.Task

/**
 * The order of the task list: active tasks first, then by title (ignoring case), with untitled
 * tasks (synced from other clients) at the end.
 */
internal val TaskListOrder = Comparator<Task> { a, b ->
    when {
        a.isActive != b.isActive -> if (a.isActive) -1 else 1
        // Decide on both titles: answering 1 whenever a is untitled says "a after b" both ways for
        // two untitled tasks, and a titled task compares after "" too, which breaks the sort.
        a.title.isBlank() != b.title.isBlank() -> if (a.title.isBlank()) 1 else -1
        else -> a.title.compareTo(b.title, ignoreCase = true)
    }
}
