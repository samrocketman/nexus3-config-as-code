import groovy.json.JsonSlurper
import org.sonatype.nexus.quartz.internal.task.QuartzTaskInfo
import org.sonatype.nexus.scheduling.TaskConfiguration
import org.sonatype.nexus.scheduling.TaskDescriptor
import org.sonatype.nexus.scheduling.TaskFactory
import org.sonatype.nexus.scheduling.TaskScheduler
import org.sonatype.nexus.scheduling.schedule.Manual

taskManager = container.lookup(TaskScheduler.class.name)
//taskScheduler is class org.sonatype.nexus.quartz.internal.QuartzSchedulerSPI
taskScheduler = taskManager.scheduler
taskFactory = taskManager.taskFactory
scheduleFactory = taskManager.scheduleFactory


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

void createTask(String name, Map json) {
    String typeId = json['type']
    List<String> blobstore_settings = ['blobstoreName', 'dryRun', 'integrityCheck', 'restoreBlobs', 'undeleteBlobs']
    TaskConfiguration config = createTaskConfigurationInstance(name, typeId)
    json[typeId].findAll { k, v ->
        k in blobstore_settings
    }.each { k, v ->
        config.setString(k, v.toString())
    }
    config.alertEmail = json.get('alertEmail', null)
    config.name = json.get('friendlyName', config.name)
    config.enabled = Boolean.parseBoolean(json.get('enabled', 'true'))
    taskManager.scheduleTask(config, scheduleFactory.manual())
}

String task_json = '''
{
    "tasks": {
        "reconcile-blob-maven-default": {
            "enabled": "true",
            "friendlyName": "Reconcile blob store maven-default",
            "alertEmail": "",
            "schedule": "",
            "type": "blobstore.rebuildComponentDB",
            "blobstore.rebuildComponentDB": {
                "blobstoreName": "maven-default",
                "dryRun": "false",
                "integrityCheck": "true",
                "restoreBlobs": "true",
                "undeleteBlobs": "true"
            }
        }
    }
}
'''

Map tasks = (new JsonSlurper()).parseText(task_json)
tasks['tasks'].each { k, v ->
    createTask(k, v)
}


//.metaClass.methods*.name.sort().unique().join(' ')
