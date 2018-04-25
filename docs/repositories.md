# Nexus Repository Configuration As Code

This document covers repository configuration as code (CaC).

- The Script API function [`repositoryConfiguration`][fxn] is responsible for
  configuring Repositories and Blob Stores.
- The configuration format for storing settings for Repositories and Blob Stores
  is JSON.  See [an example `repository.json`][json] configuration.

The repository configuration has two root keys available: `repositories` and
`blobstores`.  Any extra keys will abort the configuration process during the
format validation.

```javascript
{
    "blobstores": {
        /* configuration as code goes here */
    },
    "repositories": {
        /* configuration as code goes here */
    }
}
```

# Blob Store CaC

Currently, only file blob stores are supported.  The configuration is simple in
that one need only provide a list of blob store names to create.

```json
{
    "blobstores": {
        "file": [
            "default"
        ]
    }
}
```

# Repository CaC

A repository has three main components associated with it: a _provider_, a
_type_, and a _name_.

- _provider_ - A provider is the repository format.  For example, a default
  installation of Nexus has `maven2` and `nuget` providers configured in
  repositories.
- _type_ - Currently, there are three types of repositories in Nexus: `proxy`,
  `hosted`, and `group`.
- _name_ - A unique identifier for this repository.

A generic format for repository configuration as code follows:

```javascript
{
    "repositories": {
        "<provider>": {
            "<type>": {
                "<name>": {
                    /* repository settings go here */
                }
            }
        }
    }
}
```

[fxn]: ../functions/repositoryConfiguration.groovy
[json]: ../settings/repository.json
