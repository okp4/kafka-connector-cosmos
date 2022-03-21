# CØSMOS Kafka Connector

> Kafka Connect connector for CØSMOS based blockchains.

[![conventional commits](https://img.shields.io/badge/Conventional%20Commits-1.0.0-yellow.svg)](https://conventionalcommits.org)

## Purpose

The Kafka Connect CØSMOS Source connector is
a [Kafka connector](https://docs.confluent.io/platform/current/connect/concepts.html#connect-connectors)
used to move [blocks](https://docs.cosmos.network/master/intro/sdk-app-architecture.html) from a Cosmos blockchain (in
priority the [økp4 chain](https://github.com/okp4/okp4d)) into an Apache Kafka® topic.

## Build

### Prerequsites

To build the project, you'll need to have Java 11+ properly installed.

## Building

This project relies on the [Gradle](https://gradle.org/) build system.

If you are on windows then open a command line, go into the root directory and run:

```sh
.\gradlew build
```

If you are on linux/mac then open a terminal, go into the root directory and run:

```sh
./gradlew build
```
