#!/bin/bash
#Created by Sam Gleske
#License: ASLv2 https://github.com/samrocketman/nexus3-config-as-code
#Mon Apr 23 00:13:22 PDT 2018
#Ubuntu 16.04.4 LTS
#Linux 4.13.0-38-generic x86_64
#GNU bash, version 4.3.48(1)-release (x86_64-pc-linux-gnu)
#curl 7.47.0 (x86_64-pc-linux-gnu) libcurl/7.47.0 GnuTLS/3.5.5 zlib/1.2.8 libidn/1.32 librtmp/2.3

# DESCRIPTION:
#     List functions currently uploaded to Nexus.

curl -u admin:admin123 -X GET http://localhost:8081/service/rest/v1/script
