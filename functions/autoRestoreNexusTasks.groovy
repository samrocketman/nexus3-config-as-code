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
   This Nexus 3 REST function will automatically configure tasks to reconcile
   Nexus DB from blob stores.  This function is only useful for restoring Nexus
   after a recovery.

   POST data arguments:

          restore - will restore blob stores by reconciling blob stores with
                    Nexus DB.  Tasks will be created and run.
     delete-tasks - will delete tasks created by this function only if the task
                    is not running.
 */

import org.sonatype.nexus.scheduling.TaskConfiguration
import org.sonatype.nexus.scheduling.TaskDescriptor
import org.sonatype.nexus.scheduling.TaskInfo
import org.sonatype.nexus.scheduling.TaskScheduler
import org.sonatype.nexus.scheduling.TaskInfo.State

taskManager = container.lookup(TaskScheduler.class.name)
taskScheduler = taskManager.scheduler
taskFactory = taskManager.taskFactory
scheduleFactory = taskManager.scheduleFactory

/**
  A custom exception class to limit unnecessary text in the JSON result of the
  Nexus REST API.
 */
class MyException extends Exception {
    String message
    MyException(String message) {
        this.message = message
    }
    String toString() {
        this.message
    }
}

TaskConfiguration createTaskConfigurationInstance(String name, String typeId) {
    TaskDescriptor descriptor = taskFactory.findDescriptor(typeId)
    TaskConfiguration config = new TaskConfiguration()
    descriptor.initializeConfiguration(config)
    config.id = name
    config.typeId = descriptor.id
    config.typeName = descriptor.name
    config.name = descriptor.name
    config.visible = descriptor.isVisible()
    config
}

Map<String, String> getTaskTypeDefaults(String typeId) {
    (taskFactory.findDescriptor(typeId).formFields?.collect {
        [
            (it.id): (it.initialValue == null)? null : it.initialValue.toString()
        ]
    }?.sum()) ?: [:]
}

void createManualTask(String name, Map json) {
    String typeId = json['type']
    TaskConfiguration config
    //check if exist
    if(taskManager.getTaskById(name)) {
        config = taskManager.getTaskById(name).configuration
    }
    else {
        config = createTaskConfigurationInstance(name, typeId)
    }
    Map typeDefaults = getTaskTypeDefaults(config.typeId)
    typeDefaults.each { setting, defaultValue ->
        String value = json[typeId]?.get(setting, defaultValue) ?: defaultValue
        config.setString(setting, value)
    }
    config.alertEmail = json.get('alertEmail', null)
    config.name = json.get('friendlyName', config.name)
    config.enabled = Boolean.parseBoolean(json.get('enabled', 'true').toString())
    taskManager.scheduleTask(config, scheduleFactory.manual())
}

command = args.trim()

if(!(command in ['restore', 'delete-tasks'])) {
    throw new MyException('Must pass one of the following values as POST data: restore, delete-tasks')
}

t = null

//get a list of all existing blob store names from Nexus
blobStore.blobStoreManager.browse()*.blobStoreConfiguration*.name.each { blobstoreName ->
    String name = "reconcile-blob-${blobstoreName}"
    Map settings = [
        enabled: true,
        friendlyName: "Reconcile blob store ${blobstoreName}",
        type: 'blobstore.rebuildComponentDB',
        'blobstore.rebuildComponentDB': [
            blobstoreName: blobstoreName,
            dryRun: 'false',
            integrityCheck: 'true',
            restoreBlobs: 'true',
            undeleteBlobs: 'true'
        ]
    ]

    if(command == 'restore') {
        //create a task to un-delete the blob store
        createManualTask(name, settings)
        //immediately execute the task we just created
        taskManager.getTaskById(name).runNow()
    }
    else {
        TaskInfo task = taskManager.getTaskById(name)
        if(task) {
            if(task.currentState.state == State.RUNNING) {
                throw new MyException("Not able to delete task ${name} because it is running.")
            }
            task.remove()
        }
    }
}

'success'
