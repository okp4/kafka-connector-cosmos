# CØSMOS Kafka Connector

> Kafka Connect connector for CØSMOS based blockchains.

[![version](https://img.shields.io/github/v/release/okp4/kafka-connector-cosmos?style=for-the-badge&logo=github)](https://github.com/okp4/kafka-connector-cosmos/releases)
[![build](https://img.shields.io/github/workflow/status/okp4/kafka-connector-cosmos/Build?label=build&style=for-the-badge&logo=github)](https://github.com/okp4/kafka-connector-cosmos/actions/workflows/build.yml)
[![lint](https://img.shields.io/github/workflow/status/okp4/kafka-connector-cosmos/Lint?label=lint&style=for-the-badge&logo=github)](https://github.com/okp4/kafka-connector-cosmos/actions/workflows/lint.yml)
[![test](https://img.shields.io/github/workflow/status/okp4/kafka-connector-cosmos/Test?label=test&style=for-the-badge&logo=github)](https://github.com/okp4/kafka-connector-cosmos/actions/workflows/test.yml)
[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg?style=for-the-badge)](https://ktlint.github.io/)
[![conventional commits](https://img.shields.io/badge/Conventional%20Commits-1.0.0-yellow.svg?style=for-the-badge&logo=conventionalcommits)](https://conventionalcommits.org)
[![contributor covenant](https://img.shields.io/badge/Contributor%20Covenant-2.1-4baaaa.svg?style=for-the-badge)](https://github.com/okp4/.github/blob/main/CODE_OF_CONDUCT.md)
[![License](https://img.shields.io/badge/License-BSD_3--Clause-blue.svg?style=for-the-badge)](https://opensource.org/licenses/BSD-3-Clause)

## Purpose

The Kafka Connect CØSMOS Source connector is
a [Kafka connector](https://docs.confluent.io/platform/current/connect/concepts.html#connect-connectors)
used to move [blocks](https://docs.cosmos.network/master/intro/sdk-app-architecture.html) from a Cosmos blockchain (in
priority the [økp4 chain](https://github.com/okp4/okp4d)) into an Apache Kafka® topic.

<p align="center">
  <img src="./docs/overview.png">
</p>

The connector can be used when there is a need for connecting CØSMOS blockchains with Kafka in order to build a real-time
data pipelines based on blocks.

## Build

### Prerequsites

To build the project, you will need the following dependencies:

- [Java 11+](https://openjdk.java.net/)

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
