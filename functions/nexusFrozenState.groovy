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
   This Nexus 3 REST function will toggle Nexus read-write or read-only.  This
   is useful for system tasks which need to toggle Nexus state in order to
   perform maintenance outside of Nexus such as backing up blob stores.

   States:

     read-only  - Freezes Nexus database and blob stores preventing write
                  operations via a system initiator.
     read-write - Removes freeze request on database and blob stores frozen by
                  the system initiator.  This will not put Nexus in read-write
                  mode if other types of freeze requests exist.
 */
import org.sonatype.nexus.orient.freeze.DatabaseFreezeService
import org.sonatype.nexus.orient.freeze.DatabaseFrozenStateManager
import org.sonatype.nexus.orient.freeze.FreezeRequest
import org.sonatype.nexus.orient.freeze.FreezeRequest.InitiatorType

initiator_id = 'Script API script nexusFrozenState'
freezer = container.lookup(DatabaseFreezeService.class.name)
freezerManager = container.lookup(DatabaseFrozenStateManager.class.name)

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

void unFreeze() {
    if(freezer.isFrozen()) {
        freezerManager.state.findAll {
            it.initiatorType == InitiatorType.SYSTEM &&
            it.initiatorId == initiator_id
        }.each { FreezeRequest frozenState ->
            freezer.releaseRequest frozenState
        }
    }
}

void freeze() {
    FreezeRequest frozenState = freezerManager.state.find {
        it.initiatorType == InitiatorType.SYSTEM &&
        it.initiatorId == initiator_id
    }
    if(!frozenState) {
        freezer.requestFreeze InitiatorType.SYSTEM, initiator_id
    }
}

/*
 * Main execution
 */

String state = args.trim()
switch(state) {
    case 'read-only':
        freeze()
        break
    case 'read-write':
        unFreeze()
        break
    default:
        throw new MyException('Must pass one of the following values as POST data: read-only, read-write')
}

log.info("Script API script nexusFrozenState put Nexus into ${state} mode.")
"Nexus is in ${state} mode."
