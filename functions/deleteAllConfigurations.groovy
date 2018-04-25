/**
 * Copyright (c) 2018 Sam Gleske - https://github.com/samrocketman/docker-compose-local-nexus3-proxy
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
   Delete all repositories and all blob stores in the default Nexus
   configuration.  This is meant to set the blob store and repository state to
   nothing.
 */

import groovy.json.JsonOutput
import org.sonatype.nexus.repository.types.GroupType

blobStoreManager = blobStore.blobStoreManager
repositoryManager = repository.repositoryManager

void deleteAllRepositories(Class clazz = null) {
    List<String> groups
    if(clazz) {
        groups = repositoryManager.browse().findAll {
            it.type in GroupType
        }*.name
    }
    else {
        groups = repositoryManager.browse()*.name
    }
    groups.each {
        repositoryManager.delete(it)
    }
}

void deleteAllBlobStores() {
    List<String> stores = blobStoreManager.browse()*.blobStoreConfiguration*.name
    stores.each {
        blobStoreManager.delete(it)
    }
}

deleteAllRepositories(GroupType)
deleteAllRepositories()
deleteAllBlobStores()
'success'
