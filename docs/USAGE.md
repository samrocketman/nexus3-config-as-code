# Additional examples and options

`upload_function.py` help doc.

```
usage: upload_function.py [-h] [-V] [-v] [-n NEXUS_ENDPOINT] [-p proxy]
                          [-c CERT_PEM] [-s] [-r] [-D] [-f groovy-script]
                          [-d data-file]

This program can upload, execute, and delete Groovy scripts to the Nexus
Repository Manager version 3 Script API. From now on, these scripts will be
called "REST functions" or just "functions". This allows for fast and easy
automation. By default, functions will only be uploaded. Additional options
can be specified to execute or delete functions.

optional arguments:
  -h, --help            show this help message and exit
  -V, --version         show program's version number and exit
  -v, --verbosity       Increase output verbosity. Can be specified multiple
                        times to increase verbosity (5 times for max).

Connection options:
  NEXUS_USER and NEXUS_PASSWORD environment variables can be set to
  configure authorization. Default: admin:admin123

  -n NEXUS_ENDPOINT, --nexus NEXUS_ENDPOINT
                        URL to the Nexus endpoint. It can also be set via
                        NEXUS_ENDPOINT environment variable. Default:
                        http://localhost:8081
  -p proxy, --proxy proxy
                        Define a SOCKS5 proxy to proxy traffic. It can also be
                        set via NEXUS_SOCKS_PROXY environment variable.
  -c CERT_PEM, --ca-file CERT_PEM
                        Path to pinned CA chain in PEM format. Can be self
                        signed to guarantee secure connection. It can also be
                        set via NEXUS_CA_FILE environment variable.

Nexus Script API Options:
  Uploading a script to a REST function will occur by default. The following
  options will either skip or occur after the upload if specified.

  -s, --skip-upload     Skip uploading the function to Nexus (and proceed to
                        only run or delete).
  -r, --run             Run function after uploading it.
  -D, --delete          Delete REST function from Nexus. Occurs after upload
                        and run. Can be combined with --run to upload, run,
                        and delete.

REST Function Options:
  -f groovy-script, --function groovy-script
                        A groovy script to be uploaded as a Nexus function.
                        The file name (minus the extension) will be the name
                        of the REST function in Nexus. If --skip-upload option
                        is specified, then this can simply be the name of the
                        REST function instead of a path to a Groovy script.
  -d data-file, --data data-file
                        Data file whose contents get submitted to the function
                        being run. Depends on --run. If more than one function
                        is specified for upload, then this option is ignored.

Created by Sam Gleske (c) 2018. Apache Standard License v2
https://github.com/samrocketman/nexus3-config-as-code
```

# Additional usage

Execute an existing REST function only.

    upload_function.py -srf someFunction

Upload, Execute, and Delete REST function all in one action.

    upload_function.py -Drf path/to/someFunction.groovy

Do the same thing but also pass POST data to the REST function.

    upload_function.py -Drf path/to/someFunction.groovy -d path/to/data.txt
