# CØSMOS Kafka Connector

# 1.0.0 (2022-04-11)


### Bug Fixes

* check invalid status by its code ([284aa9a](https://github.com/okp4/kafka-connector-cosmos/commit/284aa9a6f29ffafbbb851c9c6eefd752f9f2b54d))
* consider client closed when channel shutdowned ([d1c2237](https://github.com/okp4/kafka-connector-cosmos/commit/d1c22376f606a1a3f2ca66a6a40f34acc3e9f281))
* **grpc:** add version var to cosmosSdk dependency ([2d96af1](https://github.com/okp4/kafka-connector-cosmos/commit/2d96af1c9b95d165489a7797e969b5557f28d8be))
* **grpc:** catch exception using status.code instead of status ([286555b](https://github.com/okp4/kafka-connector-cosmos/commit/286555b682f19bebc9401f609174bc11a14d8ef7))
* **grpc:** change config vars documentation from tendermint to cosmos ([42af3e7](https://github.com/okp4/kafka-connector-cosmos/commit/42af3e7c359e95293a2820cadbf31a21efc0e04c))
* handle poll without existing offset ([e7f4cf5](https://github.com/okp4/kafka-connector-cosmos/commit/e7f4cf53290bf420e48f8274ba7cae16a6e79469))
* request block height 1-based ([d40c28d](https://github.com/okp4/kafka-connector-cosmos/commit/d40c28d2892043a6947da12dc3e42e9b530d8148))
* save offset state between poll calls ([b4da165](https://github.com/okp4/kafka-connector-cosmos/commit/b4da165e6b052725ca217d9e40154dc9a7907ed6))
* test grpc status on code ([b3f0347](https://github.com/okp4/kafka-connector-cosmos/commit/b3f034757e20df32173ec3d8e3c87a2b99dbc71a))


### Features

* **grpc:** add CosmosServiceClient ([54b9cd9](https://github.com/okp4/kafka-connector-cosmos/commit/54b9cd95fcc6d0ccfed510bae19277da83ca1aee))
* **grpc:** add tls option on CosmosServiceClient ([73069de](https://github.com/okp4/kafka-connector-cosmos/commit/73069de186cad8d51d4f7e9bedb7cc9308380c4d))
* **grpc:** check if client isnt close on each call ([0ea7590](https://github.com/okp4/kafka-connector-cosmos/commit/0ea7590ff0298f7fded8d045ed29067709d3395a))
* **grpc:** implement grpc in SourceTask, update SourceConnector accordingly ([12a1a01](https://github.com/okp4/kafka-connector-cosmos/commit/12a1a01178d84ac6fade5483067ad9ec40035ac6))
* implement faked source connector ([b3b90a4](https://github.com/okp4/kafka-connector-cosmos/commit/b3b90a48b5fb6b2fb00ac2903c2ef8ca4b797309))
* implement hello-world 😁 ([3ae69ec](https://github.com/okp4/kafka-connector-cosmos/commit/3ae69ec4d8bd55d215e82e1e37176395d29eec80))
* set the maximum number of blocks to 50 (was 1000) ([ec5e63c](https://github.com/okp4/kafka-connector-cosmos/commit/ec5e63c466b4cac0c99a944f51871e1725439318))
