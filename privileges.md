# Privileges

### Application Privilege

Fields:

- `description`
- `id` - same as `name`
- `name`
- `permission` = `nexus:example.com:hello`
- `properties`
  - `actions` = `hello`
  - `domain` = `example.com`
- `readOnly` = `false`
- `type` = `application
- `version` = `""`

### Repository Admin Privilege

Fields:

- `description`
- `id` = same as `name`
- `name`
- `permission` = `nexus:repository-admin:someformat:hosted-pypi-test:fooaction`
- `properties`
  - `actions` = `fooaction`
  - `format` = `someformat`
  - `repository` = `hosted-pypi-test` (can also be ```*```; all or one)
- `readOnly` = `false`
- `type` = `repository-admin`
- `version` = `""`

### Repository Content Selector Privilege

- `description`
- `id` = same as `name`
- `name`
- `permission` =
  ```
  nexus:repository-content-selector:foo-csel:*:maven-releases:fooaction
  ```
- `properties`
  - `actions` = `fooaction`
  - `contentSelector` = `foo-csel`
  - `repository` = `maven-releases`; may also be ```*``` (all repositories),
    ```*-maven2``` (all maven2 repositories)
- `readOnly` = `false`
- `type` = `repository-content-selector`
- `version` = `1`

### Repository View Privilege

- `description`
- `id` = same as `name`
- `name`
- `permission` = `nexus:repository-view:someformat:hosted-pypi:read`
- `properties`
  - actions: "read"
  - format: "someformat"
  - repository: "hosted-pypi"
- `readOnly` = `false`
- `type` = `repository-view`
- `version` = `""`

### Script Privilege

- `description`
- `id` = same as `name`
- `name`
- `permission` = `nexus:script:somescript:read,execute"`
- `properties`
  - `actions` = `read,execute`
  - `name` = `someScript`
- `readOnly` = `false`
- `type` - `script`
- `version` = `""`

### Wildcard Privilege

- `description`
- `id` = same as `name`
- `name`
- `permission` = ```.*```
- `properties`
  - `pattern` = ```.*```
- `readOnly` = `false`
- `type` = `wildcard`
- `version` = `""`
