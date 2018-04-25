#!/usr/bin/env python2.7
#Created by Sam Gleske
#License: ASLv2 https://github.com/samrocketman/nexus3-config-as-code
#Sun Apr 22 16:02:54 PDT 2018
#Ubuntu 16.04.4 LTS
#Linux 4.13.0-38-generic x86_64
#GNU bash, version 4.3.48(1)-release (x86_64-pc-linux-gnu)
#Python 2.7.12

# DESCRIPTION:
#     Upload script to Nexus 3 scripting endpoint.
#     Delete script from Nexus 3 scripting endpoint.

from httplib import HTTPSConnection
import argparse
import base64
import json
import os
import re
import ssl
import sys
import time

try:
    import socket
    import socks
    socks_supported = True
except ImportError:
    socks_supported = False

version = "0.1"

username = os.getenv('NEXUS_USER', 'admin')
password = os.getenv('NEXUS_PASSWORD', 'admin123')
additional_args = os.getenv('NEXUS_CALL_ARGS', '')
settings = {}

parser = argparse.ArgumentParser(description="Upload Groovy script to Nexus 3 scripting endpoint.")
parser.add_argument('-V', '--version', action='version', version="%(prog)s "+version)
parser.add_argument('-c', '--ca-file', default=os.getenv('NEXUS_CA_FILE'), metavar='CERT_PEM', dest='pinned_cert', help='Path to pinned CA chain in PEM format.  Can be self signed to guarantee secure connection.  It can also be set via NEXUS_CA_FILE environment variable.')
parser.add_argument('-f', '--function', action="append", default=[], metavar='groovy-script', dest='groovy_files', help='A groovy script to be uploaded as a Nexus function.  The file name (minus the extension) will be the name of the REST function in Nexus.')
parser.add_argument('-n', '--nexus', default=os.getenv('NEXUS_ENDPOINT', 'http://localhost:8081'), metavar='NEXUS_ENDPOINT', dest='nexus_endpoint', help='URL to the Nexus endpoint.')
parser.add_argument('-p', '--proxy', default=os.getenv('NEXUS_SOCKS_PROXY'), metavar='proxy', dest='socks_proxy', help='Define a SOCKS5 proxy to proxy traffic.  It can also be set via NEXUS_SOCKS_PROXY environment variable.')
parser.add_argument('-r', '--run', action='store_true', dest='run', help='Run function after uploading it.')
parser.add_argument('-d', '--data', default='', metavar='data-file', dest='data', help='Data file whose contents get submitted to the function being run.  Depends on --run.  If more than one function is specified for upload, then this option is ignored.')
parser.add_argument('--delete', action='store_true', dest='delete', help='Delete scripts instead of uploading them.')
parser.add_argument('-s', '--skip-upload', action='store_true', dest='skip', help='Skip uploading the function to Nexus (and proceed to only run or delete).')
parser.add_argument('-v', '--verbosity', action="count", dest='verbosity', help="Increase output verbosity.")

if len(additional_args) > 0:
  if '|' in list(additional_args):
      sys.argv = [sys.argv[0]] + additional_args.strip().split('|') + sys.argv[1:]
  else:
      sys.argv = [sys.argv[0]] + additional_args.strip().split() + sys.argv[1:]
args = parser.parse_args()

def trim_url_slash(url):
    return url[:-1] if url[-1:] == '/' else url

#print to stderr
def printErr(message=''):
    sys.stderr.write(message + '\n')
    sys.stderr.flush()

#
# ARGUMENT ERROR PROCESSING
#

if len(args.groovy_files) < 1:
    printErr('Must list at least one function to upload via --function option')
    printErr('See also --help.')
    sys.exit(1)

#
# BEGIN SET HTTP HEADERS
#
if 'headers' in settings:
    headers = settings['headers']
else:
    headers = {
        'Accept': '*/*',
        'Host': args.nexus_endpoint.split('/')[2],
    }
#always set the User-Agent
headers['User-Agent'] = 'upload_function.py %s' % version
if 'Authorization' in headers:
    if args.verbosity >= 2:
        printErr("Reusing Authorization from HTTP headers file.")
else:
    if not username == None:
        if args.verbosity >= 2:
            printErr("Logging in as user %s." % username)
        headers['Authorization'] = "Basic %s" % base64.b64encode("%s:%s" % (username, password)).decode('ascii')
#
# END SET HTTP HEADERS
#
#
# BEGIN CONFIGURE SOCKS5 PROXY
#
if args.socks_proxy != None:
    proxy = args.socks_proxy
elif 'socks_proxy' in settings:
    if args.verbosity >= 2:
        printErr("Reusing proxy configuration from headers file.")
    proxy = str(settings['socks_proxy'])
else:
    proxy = ''

if proxy and not re.match(r'[-0-9a-zA-Z.]+:[0-9]+', proxy):
    printErr("Invalid --proxy specified: %s" % args.socks_proxy)
    parser.print_help()
    sys.exit(1)

if proxy:
    if args.verbosity >= 1:
        printErr("Using SOCKS5 proxy: %s" % proxy)
    settings['socks_proxy'] = proxy
    proxy_host = proxy.split(':')[0]
    proxy_port = int(proxy.split(':')[1])
    if not socks_supported:
        printErr("WARNING: Python socks module not installed so socks is not supported")
    else:
        socks.setdefaultproxy(socks.PROXY_TYPE_SOCKS5, proxy_host, proxy_port)
        socket.socket = socks.socksocket
else:
    #if args.verbosity >= 3:
    #    printErr("Removing proxy configuration from headers file.")
    settings.pop('socks_proxy', None)

### POST-PROXY SETTINGS using urllib and urllib2
# See: https://stackoverflow.com/questions/2317849/how-can-i-use-a-socks-4-5-proxy-with-urllib2
# Note: SOCKS proxy must always be configured before importing urllib and urllib2
import urllib
import urllib2
#
# END CONFIGURE SOCKS5 PROXY
#
def getUrl(url, headers, data=None, method='GET'):
    if args.verbosity >= 5:
        printErr("Headers: %s" % headers)
    if args.verbosity >= 3:
        printErr('%s %s' % (method, url))
    if args.verbosity >= 4:
        printErr("DATA\n%s" % data)
    responseCode = -1
    responseString = ""
    responseErrorReason = None
    try:
        if url.startswith('https'):
            #https://docs.python.org/3/library/ssl.html#ssl-security
            context = ssl.create_default_context()
            if args.pinned_cert:
                context.load_verify_locations(cafile=args.pinned_cert)
            else:
                context.load_default_certs()
            req = urllib2.Request(url, data=data, headers=headers)
            if method != 'GET':
                req.get_method = lambda: method
            urlconn = urllib2.urlopen(req, context=context)
        else:
            req = urllib2.Request(url=url, data=data, headers=headers)
            if method != 'GET':
                req.get_method = lambda: method
            urlconn = urllib2.urlopen(req)
        responseString = urlconn.read()
        responseCode = urlconn.getcode()
        urlconn.close()
    except urllib2.HTTPError as e:
        responseCode = e.code
        responseString = e.read()
        responseErrorReason = e.reason
    return (responseCode, responseString, responseErrorReason)

def getScriptName(fileName=''):
    if not fileName:
        return ''
    return re.sub(re.compile(r'\.groovy$'), '', fileName).split('/')[-1]

def getJsonPayload(fileName=''):
    if not fileName:
        return ''
    scriptName = getScriptName(fileName)
    with open(fileName, 'r') as f:
        script = f.read()
    json_string = """
    {
        "name": "%s",
        "content": %s,
        "type": "groovy"
    }
    """.strip() % (scriptName, json.dumps(str(script)))
    return json.dumps(json.loads(json_string), indent=4, separators=(',', ': '))

def callNexusUrl(url, headers=headers, data=None, method='GET'):
    code, response, reason = getUrl(url, headers, data, method)
    if args.verbosity >= 1:
        printErr("Response (HTTP %s):\n%s" % (str(code), response))
    if reason:
        printErr("HTTP ERROR %s: %s\n%s\n%s" % (str(code), reason, url, response))
        sys.exit(1)
    return response

def getListOfExistingScripts():
    url = "%s/%s" % (args.nexus_endpoint, 'service/rest/v1/script')
    return map(lambda x: x['name'], json.loads(callNexusUrl(url)))

#
# ADD OR UPDATE REST FUNCTIONS
#
headers['Content-Type'] = 'application/json'
if not args.skip:
    nexus_scripts = getListOfExistingScripts()
    for script in args.groovy_files:
        url = "%s/%s" % (args.nexus_endpoint, 'service/rest/v1/script')
        method = 'POST'
        if getScriptName(script) in nexus_scripts:
            method = 'PUT'
            url += "/%s" % getScriptName(script)
        callNexusUrl(url, headers, getJsonPayload(script), method)

#
# RUN REST FUNCTIONS
#
if args.run:
    headers['Content-Type'] = 'text/plain'
    data = None
    if len(args.groovy_files) == 1 and len(args.data) > 0:
        with open(args.data, 'r') as f:
            data = f.read()
    for script in args.groovy_files:
        url = "%s/%s/%s/run" % (args.nexus_endpoint, 'service/rest/v1/script', getScriptName(script))
        print callNexusUrl(url, headers, data, 'POST')

#
# DELETE REST FUNCTIONS
#
if args.delete:
    headers['Content-Type'] = 'application/json'
    nexus_scripts = getListOfExistingScripts()
    headers.pop('Content-Type', None)
    for script in args.groovy_files:
        if getScriptName(script) in nexus_scripts:
            url = "%s/%s/%s" % (args.nexus_endpoint, 'service/rest/v1/script', getScriptName(script))
            callNexusUrl(url, headers, None, 'DELETE')
