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
   This Nexus 3 REST function will configure Nexus repositories (hosted, proxy,
   group) and blob stores.  This way Nexus can be quickly configured.
 */

import groovy.json.JsonSlurper
import java.lang.NumberFormatException
import java.net.MalformedURLException
import java.util.regex.Pattern
import org.sonatype.nexus.repository.config.Configuration

blobStoreManager = blobStore.blobStoreManager
repositoryManager = repository.repositoryManager

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

void checkForEmptyValidation(String message, List<String> bad_values) {
    if(bad_values) {
        throw new MyException("Found invalid ${message}: ${bad_values.join(', ')}")
    }
}

List<String> getKnownDesiredBlobStores(Map json) {
    json['repositories'].collect { provider_key, provider ->
        provider.collect { repo_type_key, repo_type ->
            repo_type.collect { repo_name_key, repo_name ->
                (repo_name['blobstore']?.get('name', null))?: repo_name_key
            }
        }
    }.flatten().sort().unique()
}

void checkValueInList(String provider, String type, String name, String key, def value, List<String> allowed_values) {
    if(!(value in allowed_values)) {
        throw new MyException("${provider} ${type} ${name} ${key} must be one of: ${allowed_values.join(', ')}.  Found: '${value}'")
    }
}

void checkIntValue(String provider, String type, String name, String key, def value, int lowerBound, def upperBound = null) {
    int parsedValue
    try {
        parsedValue = Integer.parseInt(value)
    }
    catch(NumberFormatException e) {
        throw new MyException("${provider} ${type} ${name} ${key} must be a number.  Invalid value: '${value}'")
    }
    if(upperBound == null) {
        if(parsedValue < lowerBound) {
            throw new MyException("${provider} ${type} ${name} ${key} must be greater or equal to ${lowerBound}.  Invalid value: ${parsedValue}")
        }
    }
    else if(parsedValue < lowerBound || parsedValue > (upperBound as Integer)) {
        throw new MyException("${provider} ${type} ${name} ${key} must be between ${lowerBound}-${upperBound}.  Invalid value: ${parsedValue}")
    }
}

void checkRepositorFormat(Map json) {
    String valid_name = '^[a-zA-Z0-9][-_.a-zA-Z0-9]*$'
    Map found = [:]
    json['repositories'].each { provider, provider_value ->
        provider_value.each { type, type_value ->
            type_value.each { name, repo ->
                if(name in found) {
                    throw new MyException("Repository name conflict.  ${[provider, type, name].join(' -> ')} conflicts with ${found[name]}.")
                }
                else {
                    found[name] = [provider, type, name].join(' -> ')
                }
                if(!Pattern.compile(valid_name).matcher(name).matches()) {
                    throw new MyException("Invalid characters in name: '${name}'.  Only letters, digits, underscores(_), hyphens(-), and dots(.) are allowed and may not start with underscore or dot.")
                }
                if(type == 'hosted') {
                    checkValueInList(provider, type, name, 'write_policy', repo.get('write_policy', 'allow_once').toLowerCase(), ['allow_once', 'allow', 'deny'])
                }
                else if(type == 'proxy') {
                    try {
                        new URL(repo['remote']?.get('url', null)?: '')
                    }
                    catch(MalformedURLException e) {
                        throw new MyException("${provider} proxy ${name} does not have a valid remote.url defined.")
                    }
                    checkIntValue(provider, type, name, 'remote.content_max_age', repo['remote'].get('content_max_age', '-1'), -1)
                    checkIntValue(provider, type, name, 'remote.metadata_max_age', repo['remote'].get('metadata_max_age', '1440'), -1)
                    if(repo['connection']?.get('retries', null)) {
                        checkIntValue(provider, type, name, 'connection.retries', repo['connection']?.get('retries', null), 0)
                    }
                    if(repo['connection']?.get('timeout', null)) {
                        checkIntValue(provider, type, name, 'connection.retries', repo['connection']?.get('timeout', null), 0)
                    }
                }
                if(provider == 'maven2') {
                    checkValueInList(provider, type, name, 'version_policy', repo.get('version_policy', 'release').toLowerCase(), ['mixed', 'snapshot', 'release'])
                    checkValueInList(provider, type, name, 'layout_policy', repo.get('layout_policy', 'permissive').toLowerCase(), ['strict', 'permissive'])
                }
                else if(provider == 'nuget' && type == 'proxy') {
                    checkIntValue(provider, type, name, 'nuget_proxy.query_cache_item_max_age', ((repo['nuget_proxy']?.get('query_cache_item_max_age', null))?: '3600'), 0)
                }
            }
        }
    }
}

void validateConfiguration(def json) {
    List<String> supported_root_keys = ['repositories', 'blobstores']
    List<String> supported_blobstores = ['file']
    List<String> supported_repository_providers = ['bower', 'docker', 'gitlfs', 'maven2', 'npm', 'nuget', 'pypi', 'raw', 'rubygems']
    List<String> supported_repository_types = ['proxy', 'hosted', 'group']
    if(!(json in Map)) {
        throw new MyException("Configuration is not valid.  It must be a JSON object.  Instead, found a JSON array.")
    }
    checkForEmptyValidation('root keys', ((json.keySet() as List) - supported_root_keys))
    checkForEmptyValidation('blobstore types', ((json['blobstores']?.keySet() as List) - supported_blobstores))
    if(!(json['blobstores']?.get('file') in List) || false in json['blobstores']?.get('file').collect { it in String }) {
        throw new MyException('blobstore file type must contain a list of Strings.')
    }
    checkForEmptyValidation('repository providers', ((json['repositories']?.keySet() as List) - supported_repository_providers))
    checkForEmptyValidation('repository types', (json['repositories'].collect { k, v -> v.keySet() as List }.flatten().sort().unique() - supported_repository_types))
    checkForEmptyValidation('blobstores defined in repositories.  The following must be listed in the blobstores',
            (getKnownDesiredBlobStores(json) - json['blobstores']['file']))
    checkRepositorFormat(json)
}

void createRepository(String provider, String type, String name, Map json) {
    Configuration repo_config
    Boolean exists = repositoryManager.get(name) as Boolean
    if(exists) {
        repo_config = repositoryManager.get(name).configuration
    }
    else {
        repo_config = new Configuration()
    }
    def storage = repo_config.attributes('storage')
    if(!exists) {
        repo_config.repositoryName = name
        repo_config.recipeName = "${provider}-${type}".toString()
        storage.set('blobStoreName', (json['blobstore']?.get('name', null))?: name)
    }
    repo_config.online = Boolean.parseBoolean(json.get('online', 'true'))
    storage.set('strictContentTypeValidation', Boolean.parseBoolean((json['blobstore']?.get('strict_content_type_validation', null))?: 'false'))
    if(type == 'group') {
        def group = repo_config.attributes('group')
        group.set('memberNames', json.get('repositories', []))
    }
    else {
        if(type == 'hosted') {
            //can be ALLOW_ONCE (allow write once), ALLOW (allow write), or DENY (read only) ALLOW, DENY, ALLOW_ONCE
            storage.set('writePolicy', json.get('write_policy', 'ALLOW_ONCE').toUpperCase())
        }
        else if(type == 'proxy') {
            def proxy = repo_config.attributes('proxy')
            proxy.set('remoteUrl', json['remote']['url'])
            proxy.set('contentMaxAge', Integer.parseInt(json['remote'].get('content_max_age', '-1')))
            proxy.set('metadataMaxAge', Integer.parseInt(json['remote'].get('metadata_max_age', '1440')))
            def httpclient = repo_config.attributes('httpclient')
            httpclient.set('autoBlock', Boolean.parseBoolean(json['remote'].get('auto_block', 'true')))
            httpclient.set('blocked', Boolean.parseBoolean(json['remote'].get('blocked', 'false')))
            def negativeCache = repo_config.attributes('negativeCache')
            negativeCache.set('enabled', Boolean.parseBoolean((json['negative_cache']?.get('enabled', null))?: 'true'))
            negativeCache.set('timeToLive', Integer.parseInt((json['negative_cache']?.get('time_to_live', null))?: '1440'))
            def connection = httpclient.child('connection')
            connection.set('useTrustStore', Boolean.parseBoolean(json['remote'].get('use_trust_store', 'false')))
            connection.set('enableCircularRedirects', Boolean.parseBoolean(json['connection']?.get('enable_circular_redirects', null)?: 'false'))
            connection.set('enableCookies', Boolean.parseBoolean(json['connection']?.get('enable_cookies', null)?: 'false'))
            if(json['connection']?.get('retries', null)) {
                connection.set('retries', Integer.parseInt(json['connection']?.get('retries', null)))
            }
            else {
                connection.set('retries', null)
            }
            if(json['connection']?.get('timeout', null)) {
                connection.set('timeout', Integer.parseInt(json['connection']?.get('timeout', null)))
            }
            else {
                connection.set('timeout', null)
            }
            connection.set('userAgentSuffix', json['connection']?.get('user_agent_suffix', ''))
            String auth_type = json['remote'].get('auth_type', 'none')
            switch(auth_type) {
                case ['username', 'ntml']:
                    def authentication = httpclient.child('authentication')
                    authentication.set('type', auth_type);
                    authentication.set('username', json['remote'].get('user', ''))
                    authentication.set('password', json['remote'].get('password', ''))
                    authentication.set('ntlmHost', json['remote'].get('ntlm_host', ''))
                    authentication.set('ntlmDomain', json['remote'].get('ntlm_domain', ''))
                    break
                default:
                    break
            }
            if(provider == 'nuget') {
                def nugetProxy = repo_config.attributes('nugetProxy')
                nugetProxy.set('queryCacheItemMaxAge', Integer.parseInt((json['nuget_proxy']?.get('query_cache_item_max_age', null))?: '3600'))
            }
            else if(provider == 'bower') {
                def bower = repo_config.attributes('bower')
                bower.set('rewritePackageUrls', Boolean.parseBoolean((json['bower']?.get('rewrite_package_urls', null))?:'true'))
            }
        }
        if(provider == 'maven2') {
            def maven = repo_config.attributes('maven')
            if(!exists) {
                maven.set('versionPolicy', json.get('version_policy', 'RELEASE').toUpperCase())
            }
            maven.set('layoutPolicy', json.get('layout_policy', 'PERMISSIVE').toUpperCase())
        }
        else if(provider == 'docker') {
            def docker = repo_config.attributes('docker')
            docker.set('forceBasicAuth', Boolean.parseBoolean((json['docker']?.get('force_basic_auth', null))?:'true'))
            docker.set('v1Enabled', Boolean.parseBoolean((json['docker']?.get('v1_enabled', null))?:'false'))
            if(json['docker']?.get('http_port', null)) {
                docker.set('httpPort', Integer.parseInt(json['docker']['http_port']))
            }
            if(json['docker']?.get('https_port', null)) {
                docker.set('httpsPort', Integer.parseInt(json['docker']['https_port']))
            }
            if(type == 'proxy') {
                def dockerProxy = repo_config.attributes('dockerProxy')
                //index_type can be REGISTRY, HUB, or CUSTOM
                def index_type = ((json['docker_proxy']?.get('index_type', null))?: 'REGISTRY').toUpperCase()
                dockerProxy.set('indexType', index_type)
                if(index_type == 'CUSTOM') {
                    dockerProxy.set('indexUrl', ((json['docker_proxy']?.get('index_url', null))?: ''))
                }
                if(index_type != 'REGISTRY') {
                    dockerProxy.set('useTrustStoreForIndexAccess', Boolean.parseBoolean((json['docker_proxy']?.get('use_trust_store_for_index_access', null))?: 'false'))
                }
            }
        }
    }
    if(exists) {
        repositoryManager.update(repo_config)
    }
    else {
        repositoryManager.create(repo_config)
    }
}

/*
 * Main execution
 */

try {
    config = (new JsonSlurper()).parseText(args)
}
catch(Exception e) {
    throw new MyException("Configuration is not valid.  It must be a valid JSON object.")
}
validateConfiguration(config)
//we've come this far so it is probably good?

//create blob stores first
config['blobstores']['file'].each { String store ->
    if(!blobStoreManager.get(store)) {
        blobStore.createFileBlobStore(store, store)
    }
}

//create non-group repositories second
config['repositories'].each { provider, provider_value ->
    provider_value.findAll { k, v ->
        k != 'group'
    }.each { type, type_value ->
        type_value.each { name, name_value ->
            createRepository(provider, type, name, name_value)
        }
    }
}

//create repository groups last
config['repositories'].each { provider, provider_value ->
    provider_value['group'].each { name, name_value ->
        createRepository(provider, 'group', name, name_value)
    }
}

//.metaClass.methods*.name.sort().unique().join(' ')
'success'
