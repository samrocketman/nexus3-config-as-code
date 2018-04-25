# Nexus Repository Configuration as Code

This document covers repository configuration as code (CaC) format.

- Covers Repositories and Blob Stores.
- The Script API function [`repositoryConfiguration`][fxn] is responsible for
  configuring Repositories and Blob Stores.
- The configuration format is JSON.  See [an example `repository.json`][json]
  configuration.

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

> Note: repository names can be the name of blob stores.  Having a blob store
> configured in a repository does not mean it will be created.  It will still
> need to be listed in the `blobstores` configuration in order to be created.

# Repository CaC

### Introduction

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

Let's look at an example for the `maven-central` proxy if it were configuration
as code.  The repository name is `maven-central`, provider `maven2`, and type
`proxy`.  Any repository settings not defined have assumed defaults.

```json
{
    "repositories": {
        "maven2": {
            "proxy": {
                "maven-central": {
                    "remote": {
                        "url": "https://repo1.maven.org/maven2/"
                    },
                    "blobstore": {
                        "name": "default"
                    }
                },
            }
        }
    }
}
```

### Default repository settings

All repository settings must be JSON strings.

The following repository settings apply to all repository types.

- `blobstore` object contains additional settings.
  - `name` - value of key is by default the _name_ of the repository.
  - `strict_content_type_validation` - Valid values include `true` or `false`.
    Default: `false`
- `online` - Valid values include `true` or `false`.  Default: `true`

The following repository settings apply only to `hosted` type repositories.

- `write_policy` - deployment policy must be one of: `allow_once`, `allow`, or
  `deny`.  Default: `allow_once`

The following repository settings apply only to `proxy` type repositories.

- `remote` object contains additional settings.
  - `url` - A URL to the remote.  Default: undefined
  - `blocked` - Valid values include `true` or `false`.  Default: `false`
  - `auto_block` - Valid values include `true` or `false`.  Default: `true`
  - `auth_type` - Must be one of: `none`, `username`, `ntml`.  Default: `none`
  - `user` - Available if `auth_type` is not `none`.  Default: undefined
  - `password` - Available if `auth_type` is not `none`.  Default: undefined
  - `ntlm_host` - Available if `auth_type` is not `none`.  Default: undefined
  - `ntlm_domain` - Available if `auth_type` is not `none`.  Default: undefined

The following repository settings apply only to `maven2` provider repositories.

- `version_policy` - Must be one of: `mixed`, `snapshot`, or `release`.
  Default: `release`
- `layout_policy` - Must be one of: `strict` or `permissive`.  Default:
  `permissive`

The following repository settings apply only to `docker` provider repositories.

- `docker` object contains additional settings.
  - `force_basic_auth` - Valid values include `true` or `false`.  Default:
    `true`
  - `v1_enabled` - Valid values include `true` or `false`.  Default: `false`
  - `http_port` - A value between `0-65535`.  Default: undefined
  - `https_port` - A value between `0-65535`.  Default: undefined

Please refer to the Nexus documentation for the meaning of the settings.

[fxn]: ../functions/repositoryConfiguration.groovy
[json]: ../settings/repository.json