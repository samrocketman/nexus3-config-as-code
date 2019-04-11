# Backup and Restore Guide for Nexus 3

This document is intended as a reference for backing up and restoring Nexus
Repository Manager 3 in conjuction with using configuration as code.

# WARNING

This guide assumes a restore occurs on a fresh Nexus instance.  Do not perform
restore operations on an existing Nexus because it will delete items and may
have unintended consequences.

# Additional notes and warnings

- Blob stores are the only information which needs to be backed up via tar.  The
  rest of the Nexus configuration should be stored as configuration as code.
  Because configuration will be stored as code there is no need to backup the
  Nexus database.  **Nexus configuration as code is not comprehensive enough for
  this statement to be completely true for all Nexus installations.**
- Configuration blob stores must always match the backed up blob stores.
- Any backup or restore operation involving direct access to blob stores should
  occur while Nexus is in READ-ONLY mode.
- When Nexus is in read-only mode, scripts which perform write operations such
  deleting, creating, or configuring repositories are blocked from running.
  This means configuration can't be restored while Nexus is in read-only mode.
- Do not create other tasks before reconciling the blob stores to restore Nexus
  (a.k.a. restoring the blob stores).

# Before running a backup or restore

Set environment variables on the destination Nexus to be restored.

    export NEXUS_ENDPOINT='https://nexus.example.com/'
    export NEXUS_USER='admin'
    export NEXUS_PASSWORD='admin123'

Upload all required functions to the [Nexus Script API][nexus-script].

    ls ./functions/*.groovy | xargs -n1 -- ./scripts/upload_function.py -f

# Backup Nexus

1. Put Nexus into read-only mode.  This prevents write operations on the blob
   stores while attempting to create a backup.

   ```
   ./scripts/upload_function.py -srf nexusFrozenState -S read-only
   ```

2. Create a `tar` backup of the `blobs/` directory within the Nexus data
   directory.  This will backup all blob stores.  For example, if you're running
   Nexus in docker, then the `/nexus-data` directory is the location of the
   Nexus data directory.  The following is an example taking a backup of blobs
   while [running Nexus within a docker container][nexus-docker].

   ```bash
   CONTAINER_ID=$(docker-compose ps -q nexus3)
   docker run --init --volumes-from "${CONTAINER_ID}" --rm centos:7 /usr/bin/tar -C /nexus-data -cv blobs > backup.tar
   ```

3. Take Nexus out of read-only mode.  This will allow Nexus to be used again for
   publishing artifacts.

   ```
   ./scripts/upload_function.py -srf nexusFrozenState -S read-write
   ```

# Restore Nexus

This section assumes the worst case scenario where:

- Your original Nexus was completely deleted including all repository
  configuration and blob store data.
- Your original Nexus configuration is written in configuration as code.
- You have a `tar` backup of the original `blobs/` directory.
- You're restoring to a fresh Nexus installation which has the default set of
  repositories configured.

Steps to restore:

1. Delete all default repositories.

   ```
   ./scripts/upload_function.py -srf deleteAllRepositoryConfiguration -S delete
   ```

2. Execute configuration as code to configure repositories and blob stores.
   This will restore repository configuration to a working state but it _will
   not_ restore the repository data, yet.  To learn more about
   [`repositories.json` see the Nexus Repository Configuration as Code
   document](repositories.md).

   ```
   ./scripts/upload_function.py -srf repositoryConfiguration -d ./path/to/repositories.json
   ```

3. (Skip if only S3 repos are configured). We're now ready to restore repository data.  Put Nexus into read-only mode.

   ```
   ./scripts/upload_function.py -srf nexusFrozenState -S read-only
   ```

4. (Skip if only S3 repos are configured) Wipe out the existing blobs directory.  Again, assuming [Nexus in a docker
   container][nexus-docker] refer to the following example.

   ```bash
   CONTAINER_ID=$(docker-compose ps -q nexus3)
   docker run -i --init --volumes-from "${CONTAINER_ID}" centos:7 rm -rf /nexus-data/blobs
   ```

5. (Skip if only S3 repos are configured) Restore the blob stores from the `tar` backup.

   ```
   CONTAINER_ID=$(docker-compose ps -q nexus3)
   docker run -i --init --volumes-from "${CONTAINER_ID}" centos:7 /usr/bin/tar -C /nexus-data -xv < backup.tar
   ```

6. (Skip if only S3 repos are configured) Take Nexus out of read-only mode.

   ```
   ./scripts/upload_function.py -srf nexusFrozenState -S read-write
   ```

6. Reconcile the Nexus DB for each blob store.  The following command will do
   this automatically.  For each blob store, create and run a manual admin task
   of type _Repair - Reconcile component database from blob store_.  This will
   create and run one task per blob store.

   ```
   ./scripts/upload_function.py -srf autoRestoreNexusBlobstores -S restore
   ```

7. Wait for all of the tasks to finish.  You can check on progress of the tasks
   by visiting the _Nexus settings > Tasks_.  When all tasks finish running they
   will be in the state **Waiting**.

8. Delete the tasks created by step 6.

   ```
   ./scripts/upload_function.py -srf autoRestoreNexusBlobstores -S delete-tasks
   ```

# Learn more about the functions

All of the above functions can be found in the [`functions/`
directory](../functions/) of this repository. When restoring from S3 new *-metrics.properties files will be created in you S3 bucket.
This is because each time Nexus3 starts it creates new ID for ElasticSearch database. This is is part of metrics provided for Nexus3.
You can delete all old *-metrics.properties on your S3 manually safely (leave the one with newest timestamp).


[nexus-docker]: https://github.com/samrocketman/docker-compose-nexus3-proxy
[nexus-script]: https://help.sonatype.com/repomanager3/rest-and-integration-api/script-api
