/**
 * Copyright (c) 2018 Sam Gleske - https://github.com/samrocketman/nexus3-config-as-code
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
/*
   This Nexus 3 REST function will check if a task is running.

   Returns:

      true - Task exists and is running.
     false - Task is not running or does not exist.

   POST data arguments:

      <id> - The ID of the task in which to check if it is running.
 */

import org.sonatype.nexus.scheduling.TaskInfo
import org.sonatype.nexus.scheduling.TaskInfo.State
import org.sonatype.nexus.scheduling.TaskScheduler

id = args.trim()
taskManager = container.lookup(TaskScheduler.class.name)
taskManager.listsTasks()*.id
TaskInfo task = taskManager.getTaskById(id)
Boolean.parseBoolean((task?.currentState?.state == State.RUNNING).toString())
