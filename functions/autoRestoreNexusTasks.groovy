import org.sonatype.nexus.scheduling.TaskScheduler
import org.sonatype.nexus.scheduling.TaskConfiguration
import org.sonatype.nexus.quartz.internal.task.QuartzTaskInfo
import org.sonatype.nexus.scheduling.TaskFactory
import org.sonatype.nexus.scheduling.schedule.Manual

taskFactory = container.lookup(TaskFactory.class.name)
taskManager = container.lookup(TaskScheduler.class.name)
//taskScheduler is class org.sonatype.nexus.quartz.internal.QuartzSchedulerSPI
taskScheduler = taskManager.scheduler

//taskManager.createTaskConfigurationInstance()

//taskManager.getScheduler()
//class org.sonatype.nexus.quartz.internal.QuartzSchedulerSPI$$EnhancerByGuice
//methods CGLIB$SET_STATIC_CALLBACKS CGLIB$SET_THREAD_CALLBACKS CGLIB$findMethodProxy cancelJob equals getClass getExecutedTaskCount getRunningTaskCount getStateGuard getTaskById hashCode isLimitedToAnotherNode listsTasks notify notifyAll on pause removeTask renderDetailMessage renderStatusMessage resume runNow scheduleFactory scheduleTask start stop toString triggerConverter wait

//taskManager.listsTasks()
//  "result" : "[QuartzTaskInfo{jobKey=nexus.248b393d-6516-407e-80fd-7199a0e95dcb, state=WAITING, taskState=QuartzTaskState{taskConfiguration={.updated=2018-05-08T03:27:34.179Z, .name=Task log cleanup, .id=a26636d5-b489-49b4-8454-c9776e9b55c2, .typeName=Task log cleanup, .visible=false, .typeId=tasklog.cleanup, .created=2018-05-08T03:27:34.179Z}, schedule=Cron{properties={schedule.startAt=2018-05-08T03:27:34.179Z, schedule.cronExpression=0 0 0 * * ?, schedule.type=cron}}, nextExecutionTime=Wed May 09 00:00:00 UTC 2018}, taskFuture=null, removed=false},

//QuartzTaskInfo{jobKey=nexus.6d6414fa-f178-4131-a236-d47ef8ada278, state=WAITING, taskState=QuartzTaskState{taskConfiguration={.name=Restore maven-default blob store, dryRun=false, restoreBlobs=true, lastRunState.runStarted=1525757624966, .id=95c42ced-1d37-47b3-bed5-b18876852a93, .typeName=Repair - Reconcile component database from blob store, .visible=true, .typeId=blobstore.rebuildComponentDB, undeleteBlobs=true, lastRunState.endState=OK, .updated=2018-05-08T05:33:22.654Z, integrityCheck=true, .enabled=true, blobstoreName=maven-default, lastRunState.runDuration=11719, .created=2018-05-08T05:33:22.654Z}, schedule=Manual{properties={schedule.type=manual}}, nextExecutionTime=Sun Aug 17 07:12:55 UTC 292278994}, taskFuture=null, removed=false},

//QuartzTaskInfo{jobKey=nexus.de6f3235-7255-4eda-a7af-85f318ad8984, state=WAITING, taskState=QuartzTaskState{taskConfiguration={.updated=2018-05-08T03:27:33.924Z, .name=Storage facet cleanup, lastRunState.runStarted=1525750800036, .message=Reclaim storage for deleted repositories, .id=68a93c15-5cfd-4035-aeae-37e8274b8470, lastRunState.runDuration=30, .typeName=Storage facet cleanup, .visible=false, .typeId=repository.storage-facet-cleanup, .created=2018-05-08T03:27:33.924Z, lastRunState.endState=OK}, schedule=Cron{properties={schedule.startAt=2018-05-08T03:27:33.918Z, schedule.cronExpression=0 */10 * * * ?, schedule.type=cron}}, nextExecutionTime=Tue May 08 03:50:00 UTC 2018}, taskFuture=null, removed=false}]"

//taskFactory.taskDefinitions.keySet().join(' ')
//repository.docker.gc repository.docker.upload-purge repository.storage-facet-cleanup repository.purge-unused repository.maven.rebuild-metadata healthcheck repository.yum.rebuild.metadata firewall.audit tasklog.cleanup repository.maven.publish-dotindex repository.npm.reindex blobstore.rebuildComponentDB create.browse.nodes repository.maven.remove-snapshots repository.maven.purge-unused-snapshots blobstore.compact repository.maven.unpublish-dotindex firewall.ignore-patterns analytics.submit script rebuild.asset.uploadMetadata security.purge-api-keys db.backup repository.rebuild-inde

//taskManager.getTaskById('95c42ced-1d37-47b3-bed5-b18876852a93').runNow('Script API')

//List<QuartzTaskInfo> tasks
//taskManager.createTaskConfigurationInstance('blobstore.rebuildComponentDB')

//taskScheduler methods
//CGLIB$SET_STATIC_CALLBACKS CGLIB$SET_THREAD_CALLBACKS CGLIB$findMethodProxy cancelJob equals getClass getExecutedTaskCount getRunningTaskCount getStateGuard getTaskById hashCode isLimitedToAnotherNode listsTasks notify notifyAll on pause removeTask renderDetailMessage renderStatusMessage resume runNow scheduleFactory scheduleTask start stop toString triggerConverter wait

QuartzTaskInfo task = taskManager.submit(taskManager.createTaskConfigurationInstance('blobstore.rebuildComponentDB'))
task.state = 'WAITING'
task.schedule = new Manual()
taskScheduler.scheduleTask(task)

//.metaClass.methods*.name.sort().unique().join(' ')
