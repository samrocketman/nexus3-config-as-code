# Nexus Repository Configuration as Code

This document covers repository configuration as code (CaC) format.

- Covers Repositories, Blob Stores, and Content Selectors.
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

Currently, both file blob stores and s3 are supported.  The configuration is simple in
that one need only provide a list of blob store names to create.

```json
{
    "blobstores": {
        "file": [
            "default"
        ],
        "s3": [
                {
                    "name": "s3-test-blob",
                    "config": {
                        //mandatory fields
                        "bucket" : "bucket-name",
                        "accessKeyId": "your access key id",
                        "secretAccessKey": "your secret access key",
                        "expiration": "expiration",
                        "region": "region",

                        //optional fields
                        "sessionToken": "session token",
                        "assumeRole": "assume role",
                        "endpoint": "endpoint",
                        "signertype": "signer type"
                }
            }
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

All repository settings must be JSON strings.  Please refer to the Nexus
documentation for the meaning of the settings.

The following repository settings apply to all repository types.

- `blobstore` object contains additional settings.
  - `name` - value of key is by default the _name_ of the repository.
  - `strict_content_type_validation` - Valid values include `true` or `false`.
    Default: `false`
- `online` - Valid values include `true` or `false`.  Default: `true`

The following repository settings apply only to `group` type repositories.

- `repositories` - A JSON Array of repository _names_.  Default: `[]` (an empty
  array)

The following repository settings apply only to `hosted` type repositories.

- `write_policy` - deployment policy must be one of: `allow_once`, `allow`, or
  `deny`.  Default: `allow_once`

The following repository settings apply only to `proxy` type repositories.

- `remote` object contains additional settings.
  - `url` - A URL to the remote.  Default: undefined
  - `use_trust_store` - Valid values include `true` or `false`.  Default:
    `false`
  - `blocked` - Valid values include `true` or `false`.  Default: `false`
  - `auto_block` - Valid values include `true` or `false`.  Default: `true`
  - `content_max_age` - Must be an integer greater than `-1`.  Default: `-1`
  - `metadata_max_age` - Must be an integer greater than `-1`.  Default: `1440`
  - `auth_type` - Must be one of: `none`, `username`, `ntml`.  Default: `none`
  - `user` - Available if `auth_type` is not `none`.  Default: undefined
  - `password` - Available if `auth_type` is not `none`.  Default: undefined
  - `ntlm_host` - Available if `auth_type` is not `none`.  Default: undefined
  - `ntlm_domain` - Available if `auth_type` is not `none`.  Default: undefined
- `negative_cache` object contains additional settings.
  - `enabled` - Valid values include `true` or `false`.  Default: `true`
  - `time_to_live` - Must be an integer greater than `-1`.  Default: `1440`
- `connection` object contains additional settings.
  - `user_agent_suffix` - A string.
  - `retries` - Must be an integer greater than `0`.  Default: undefined
  - `timeout` - Must be an integer greater than `0`.  Default: undefined
  - `enable_circular_redirects` - Valid values include `true` or `false`.
    Default: `false`
  - `enable_cookies` - Valid values include `true` or `false`.  Default: `false`

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
  - `http_port` - A value between `1-65535`.  Default: undefined
  - `https_port` - A value between `1-65535`.  Default: undefined

The following repository settings apply only to `docker` provider repositories
which are also `proxy` type.

- `docker_proxy` object contains additional settings.
  - `index_type` - Must be one of: `registry`, `hub`, or `custom`.  Default:
    `registry`
  - `index_url` - A URL to a docker index.  Ignored unless `index_type` is
    `custom`.  Default: undefined
  - `use_trust_store_for_index_access` - Ignored unless `index_type` is `hub` or
    `custom`.  Valid values include `true` or `false`.  Default: `false`

The following repository settings apply only to `nuget` provider repositories
which are also `proxy` type.

- `nuget_proxy` object contains additional settings.
  - `query_cache_item_max_age` - Must be an integer greater than `-1`.  Default:
    `3600`

The following repository settings apply only to `bower` provider repositories
which are also `proxy` type.

- `bower` object contains additional settings.
  - `rewrite_package_urls` - Valid values include `true` or `false`.  Default:
    `true`

A JSON example of the above defaults in an exhaustive list.  It's not an example
for practical use (it will fail validation).  Just for showing all of the
options defined above as they're laid out in JSON.

```json
{
    "repositories": {
        "maven2": {
            "group": {
                "maven-public": {
                    "blobstore": {
                        "name": "maven-public",
                        "strict_content_type_validation": "false"
                    },
                    "online": "true",
                    "repositories": [
                        "maven-releases",
                        "maven-central"
                    ],
                    "version_policy": "release",
                    "layout_policy": "permissive"
                }
            },
            "hosted": {
                "maven-releases": {
                    "blobstore": {
                        "name": "maven-releases",
                        "strict_content_type_validation": "false"
                    },
                    "online": "true",
                    "write_policy": "allow_once",
                    "version_policy": "release",
                    "layout_policy": "permissive"
                }
            },
            "proxy": {
                "maven-central": {
                    "blobstore": {
                        "name": "maven-central",
                        "strict_content_type_validation": "false"
                    },
                    "online": "true",
                    "remote": {
                        "url": "",
                        "use_trust_store": "false",
                        "blocked": "false",
                        "auto_block": "true",
                        "content_max_age": "-1",
                        "metadata_max_age": "1440",
                        "auth_type": "none",
                        "user": "",
                        "password": "",
                        "ntlm_host": "",
                        "ntlm_domain": ""
                    },
                    "negative_cache": {
                        "enabled": "true",
                        "time_to_live": "1440"
                    },
                    "version_policy": "release",
                    "layout_policy": "permissive"
                }
            }
        },
        "docker": {
            "group": {
                "docker-public": {
                    "blobstore": {
                        "name": "docker-public",
                        "strict_content_type_validation": "false"
                    },
                    "online": "true",
                    "repositories": [
                        "docker-releases",
                        "dockerhub"
                    ],
                    "docker": {
                        "force_basic_auth": "true",
                        "v1_enabled": "false",
                        "http_port": "",
                        "https_port": ""
                    }
                }
            },
            "hosted": {
                "docker-releases": {
                    "blobstore": {
                        "name": "docker-releases",
                        "strict_content_type_validation": "false"
                    },
                    "online": "true",
                    "docker": {
                        "force_basic_auth": "true",
                        "v1_enabled": "false",
                        "http_port": "",
                        "https_port": ""
                    }
                }
            },
            "proxy": {
                "dockerhub": {
                    "blobstore": {
                        "name": "dockerhub",
                        "strict_content_type_validation": "false"
                    },
                    "online": "true",
                    "remote": {
                        "url": "",
                        "use_trust_store": "false",
                        "blocked": "false",
                        "auto_block": "true",
                        "content_max_age": "-1",
                        "metadata_max_age": "1440",
                        "auth_type": "none",
                        "user": "",
                        "password": "",
                        "ntlm_host": "",
                        "ntlm_domain": ""
                    },
                    "negative_cache": {
                        "enabled": "true",
                        "time_to_live": "1440"
                    },
                    "docker": {
                        "force_basic_auth": "true",
                        "v1_enabled": "false",
                        "http_port": "",
                        "https_port": ""
                    },
                    "docker_proxy": {
                        "index_type": "registry",
                        "index_url": "",
                        "use_trust_store_for_index_access": "false"
                    }
                }
            }
        },
        "nuget": {
            "proxy": {
                "nuget.org-proxy": {
                    "blobstore": {
                        "name": "nuget.org-proxy",
                        "strict_content_type_validation": "false"
                    },
                    "online": "true",
                    "remote": {
                        "url": "",
                        "use_trust_store": "false",
                        "blocked": "false",
                        "auto_block": "true",
                        "content_max_age": "-1",
                        "metadata_max_age": "1440",
                        "auth_type": "none",
                        "user": "",
                        "password": "",
                        "ntlm_host": "",
                        "ntlm_domain": ""
                    },
                    "negative_cache": {
                        "enabled": "true",
                        "time_to_live": "1440"
                    },
                    "nuget_proxy": {
                        "query_cache_item_max_age": "3600"
                    }
                }
            }
        },
        "bower": {
            "proxy": {
                "registry.bower.io": {
                    "blobstore": {
                        "name": "registry.bower.io",
                        "strict_content_type_validation": "false"
                    },
                    "online": "true",
                    "remote": {
                        "url": "",
                        "use_trust_store": "false",
                        "blocked": "false",
                        "auto_block": "true",
                        "content_max_age": "-1",
                        "metadata_max_age": "1440",
                        "auth_type": "none",
                        "user": "",
                        "password": "",
                        "ntlm_host": "",
                        "ntlm_domain": ""
                    },
                    "negative_cache": {
                        "enabled": "true",
                        "time_to_live": "1440"
                    },
                    "bower": {
                        "rewrite_package_urls": "true"
                    }
                }
            }
    }
}
```

# Content Selector CaC

Content selectors can be configured via the `content_selectors` key.  A content
selector has three main components: a name, a description, and an expression.

```json
{
    "content_selectors": {
        "find-raw-csel": {
            "description": "Find all raw repositories.",
            "expression": "format == \"raw\""
        }
    }
}
```

[fxn]: ../functions/repositoryConfiguration.groovy
[json]: ../examples/repository.json
