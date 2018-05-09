import groovy.json.JsonSlurper
import org.sonatype.nexus.scheduling.TaskConfiguration
import org.sonatype.nexus.scheduling.TaskDescriptor
import org.sonatype.nexus.scheduling.TaskScheduler

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

Map<String, String> getTaskTypeDefaults(String typeId) {
    (taskFactory.findDescriptor(typeId).formFields?.collect {
        [
            (it.id): (it.initialValue == null)? null : it.initialValue.toString()
        ]
    }?.sum()) ?: [:]
}

void createTask(String name, Map json) {
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

task_json = '''
{
    "tasks": {
        "reconcile-blob-maven-default": {
            "type": "blobstore.rebuildComponentDB"
        }
    }
}
'''

Map tasks = (new JsonSlurper()).parseText(task_json)
tasks['tasks'].each { k, v ->
    createTask(k, v)
}

'success'
//equals getAttributes getClass getHelpText getId getIdMapping getInitialValue getLabel getNameMapping getRegexValidation getStoreApi getStoreFilters getType hashCode isDisabled isReadOnly isRequired mandatory notify notifyAll optional setDisabled setHelpText setId setInitialValue setLabel setReadOnly setRegexValidation setRequired toString wait witHelpText witLabel withId withIdMapping withInitialValue withNameMapping withRegexValidation withRequired withStoreApi withStoreFilter
//taskFactory.findDescriptor('blobstore.rebuildComponentDB').formFields[1].initialValue
//.metaClass.methods*.name.sort().unique().join(' ')
