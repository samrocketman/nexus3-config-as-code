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
   Delete all repositories all blob stores, and all content selectors in Nexus
   configuration.  This is meant to set the repository configuration state to
   nothing.
 */

import org.sonatype.nexus.repository.types.GroupType
import org.sonatype.nexus.selector.SelectorConfiguration
import org.sonatype.nexus.selector.SelectorManager

blobStoreManager = blobStore.blobStoreManager
repositoryManager = repository.repositoryManager
selectorManager = container.lookup(SelectorManager.class.name)

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

void deleteAllContentSelectors() {
    List<SelectorConfiguration> selectors = selectorManager.browse()
    selectors.each {
        selectorManager.delete(it)
    }
}

if(args.trim() != 'delete') {
    return 'ERROR: must submit \'delete\' as POST data in order to really delete.  This is a protection from accidental wiping of an entire Nexus installation.'
}

deleteAllRepositories(GroupType)
deleteAllRepositories()
deleteAllBlobStores()
deleteAllContentSelectors()
'success'
