# Redlink Utils
_An assortment of useful utils_

[![Build Status](https://github.com/redlink-gmbh/redlink-utils/actions/workflows/maven-build-and-deploy.yaml/badge.svg)](https://github.com/redlink-gmbh/redlink-utils/actions/workflows/maven-build-and-deploy.yaml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=io.redlink.utils%3Aredlink-utils&metric=alert_status)](https://sonarcloud.io/dashboard?id=io.redlink.utils%3Aredlink-utils)

[![Maven Central](https://img.shields.io/maven-central/v/io.redlink.utils/redlink-utils.png)](https://central.sonatype.com/namespace/io.redlink.utils)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/io.redlink.utils/redlink-utils.png)](https://oss.sonatype.org/#nexus-search;gav~io.redlink.utils~~~~)
[![Javadocs](https://www.javadoc.io/badge/io.redlink.utils/redlink-utils.svg)](https://www.javadoc.io/doc/io.redlink.utils/redlink-utils)
[![Apache 2.0 License](https://img.shields.io/github/license/redlink-gmbh/redlink-utils.svg)](https://www.apache.org/licenses/LICENSE-2.0)

> Starting with `v3.0.0` Redlink Utils requires Java 11+ and has module-support included.

## Modules

### Utils

A mixed set of useful helpers, e.g., for calculating hashes/checksums, copying files and working with classpath-resources.

```xml
<dependency>
    <groupId>io.redlink.utils</groupId>
    <artifactId>utils</artifactId>
    <version>${redlink.utils.version}</version>
</dependency>
```

### Logging

A `LoggingContext` for [SLF4J](http://www.slf4j.org/) that encapsulates a [`MDC`](http://www.slf4j.org/api/org/slf4j/MDC.html). 
When the LoggingContext is closed, the MDC is reset to the state it was when the LoggingContext was created.

```xml
<dependency>
    <groupId>io.redlink.utils</groupId>
    <artifactId>slf4j-utils</artifactId>
    <version>${redlink.utils.version}</version>
</dependency>
```

### Signal Handling

`SignalHelper` allows to register a signal handler wrapping `sun.misc.Signal`.

```xml
<dependency>
    <groupId>io.redlink.utils</groupId>
    <artifactId>signals</artifactId>
    <version>${redlink.utils.version}</version>
</dependency>
```

### Test

#### `Testcontainers`

Convenience-Wrapper for commonly used [testcontainers](https://github.com/testcontainers/testcontainers-java):

* **SolrContainer**
* **MongoContainer**
* **[VindContainer](https://github.com/RBMHTechnology/vind)**, 
based on [vind-solr-server](https://github.com/redlink-gmbh/vind-solr-server).
* **ZookeeperContainer**

```xml
<dependency>
    <groupId>io.redlink.utils</groupId>
    <artifactId>testcontainers</artifactId>
    <version>${redlink.utils.version}</version>
</dependency>
```

## License
Free use of this software is granted under the terms of the Apache License Version 2.0.
See the [License](LICENSE.txt) for more details.
