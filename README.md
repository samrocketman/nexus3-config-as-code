# Nexus 3 Configuration As Code

- Tested against Nexus Repository Manager OSS 3.10.0-04

This project contains scripts and standards for configuration as code for Nexus.
This allows Nexus configuration changes to be reviewed in pull requests and
avoids the need of granting anybody admin access to Nexus.

# Configuration as Code

- [Repository Configuration as Code](./docs/repositories.md) format.  Covers
  repositories, blob stores, and content selectors.

# Organization of this repository

- `functions/` - a directory of Nexus scripts meant to be uploaded to the [Nexus
  Script API][nexus-script].  A script is what is called a "function" in this
  repository and will be referenced from now on as a function.
- `scripts/` - Local scripts designed to be run from automation or a local
  machine to interact with Nexus.

# Executing Configuration As Code

All configuration as code scripts are meant to be run via the [Nexus Script
API][nexus-script].  [`./scripts/upload_function.py`][upload-fxn] has been
provided to perform the following actions:

- Upload functions to the Nexus Script API.
- Delete functions from the Nexus Script API.
- Execute functions available in the Nexus Script API.  Option to submit data as
  an argument to the function is also possible.

Examples:

```bash
# Delete all default repositories and blob stores in Nexus
./scripts/upload_function.py -rf ./functions/deleteAllRepositoriesAndBlobstores.groovy

# Configure new repositories and blob stores
./scripts/upload_function.py -rf ./functions/repositoryConfiguration.groovy -d ./examples/repository.json
```

See [additional USAGE](docs/USAGE.md) for more examples and options.

# Backup and Restore Guide

For backing up and restoring a production Nexus installation, refer to the
[Backup and Restore Guide](./docs/backup_restore.md).

# License

[ASL v2](LICENSE)

```
Copyright (c) 2018 Sam Gleske - https://github.com/samrocketman/nexus3-config-as-code

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[nexus-script]: https://help.sonatype.com/repomanager3/rest-and-integration-api/script-api
[upload-fxn]: ./scripts/upload_function.py
