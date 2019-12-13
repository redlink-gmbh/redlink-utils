# Redlink Utils
_An assortment of useful utils_

[![Build Status](https://travis-ci.org/redlink-gmbh/redlink-utils.svg?branch=master)](https://travis-ci.org/redlink-gmbh/redlink-utils)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=io.redlink.utils%3Aredlink-utils&metric=alert_status)](https://sonarcloud.io/dashboard?id=io.redlink.utils%3Aredlink-utils)

[![Maven Central](https://img.shields.io/maven-central/v/io.redlink.utils/redlink-utils.png)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.redlink.utils%22)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/io.redlink.utils/redlink-utils.png)](https://oss.sonatype.org/#nexus-search;gav~io.redlink.utils~~~~)
[![Javadocs](https://www.javadoc.io/badge/io.redlink.utils/redlink-utils.svg)](https://www.javadoc.io/doc/io.redlink.utils/redlink-utils)
[![Apache 2.0 License](https://img.shields.io/github/license/redlink-gmbh/redlink-utils.svg)](http://www.apache.org/licenses/LICENSE-2.0)

## Modules

### `Utils`

A mixed set of useful helpers, e.g. for calculating hashes/checksums, copying files and working with classpath-resources.

### `Lang DE`

Some helpers to handle German specifics, currently only (de-)genering is available:
`"Einzelhandelskaufmann/-frau" --> [ "Einzelhandelskaufmann", "Einzelhandelskauffrau" ]`

### Logging

A `LoggingContext` for [SLF4J](http://www.slf4j.org/) that encapsulates a [`MDC`](http://www.slf4j.org/api/org/slf4j/MDC.html). 
When the LoggingContext is closed, the MDC is reset to the state it was when the LoggingContext was created.

### Test

#### `Testcontainers`

Convenience-Wrapper for commonly used [testcontainers](https://github.com/testcontainers/testcontainers-java):

* **SolrContainer**
* **MongoContainer**
* **[VindContainer](https://github.com/RBMHTechnology/vind)**, 
based on [vind-solr-server](https://github.com/redlink-gmbh/vind-solr-server).

## License
Free use of this software is granted under the terms of the Apache License Version 2.0.
See the [License](LICENSE.txt) for more details.
