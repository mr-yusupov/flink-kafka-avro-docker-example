# Flink stateful example with Kafka on Docker

Flink stateful application for local environment, which includes Kafka (zookeeper and broker), SchemaRegistry and Flink cluster (JobManager and TaskManager).
There is a kafka container [kafka-topic-setup](src/docker/docker-compose.yml) used to create kafka topics that are required by the sample application.
Flink application is running in [Application Mode](https://nightlies.apache.org/flink/flink-docs-master/docs/deployment/overview/#deployment-modes) which requires jars to be available on the classpath.
For simplicity, cluster uses only one broker and zookeeper, one SchemaRegistry.

Flink Stateful application simply takes event [InputEvent](src/main/avro/dev/yusupov/flink/avro/event/InputEvent.avsc) from Kafka topic (`dev.yusupov.flink.kafka.input`), counts number of occurrences based on unique `id`,
and sends out the [OutputEvent](src/main/avro/dev/yusupov/flink/avro/event/OutputEvent.avsc) to different kafka topic (`dev.yusupov.flink.kafka.output`).

## Pre-requisites

1. Docker
2. Java 11+

## Getting started

### Quickstart
To be able to start the cluster flink-stataful-application has to be build:
```
./gradle build
```
which will create `build/lib` folder with complete artifact (`*.jar`).

This artifact will be mounted to the Flink (`/opt/flink/usrlib`).
Next, start the docker container (from the folder where docker-compose is [located](src/docker/docker-compose.yml):
```bash
docker-compose up -d
```
`-d` - detached mode

This will start Flink cluster on [localhost:18081](http://localhost:18081/). Flink application will be up and running.
Stateful flink application will run with state in memory.
Kafka BootstrapServers on `'127.0.0.1:19092'` and SchemaRegistry on` 'http://0.0.0.0:28081'`

```bash
> docker-compose ps -a --format table

NAME                IMAGE                                             COMMAND                                   SERVICE             CREATED          STATUS                     PORTS
zookeeper           confluentinc/cp-zookeeper:7.3.5                   "/etc/confluent/docker/run"               zookeeper           12 seconds ago   Up 11 seconds              2888/tcp, 0.0.0.0:2181->2181/tcp, 3888/tcp
kafka               confluentinc/cp-kafka:7.3.5                       "/etc/confluent/docker/run"               kafka               12 seconds ago   Up 11 seconds              9092/tcp, 0.0.0.0:19092->19092/tcp
schema-registry     confluentinc/cp-schema-registry:7.3.5             "/etc/confluent/docker/run"               schema-registry     12 seconds ago   Up 10 seconds              8081/tcp, 0.0.0.0:28081->28081/tcp
kafka-topic-setup   confluentinc/cp-kafka:7.3.5                       "/bin/sh -c '\n# blocks until kafka is"   kafka-topic-setup   11 seconds ago   Exited (0) 2 seconds ago
control-center      confluentinc/cp-enterprise-control-center:7.3.5   "/etc/confluent/docker/run"               control-center      11 seconds ago   Up 10 seconds              0.0.0.0:9021->9021/tcp
jobmanager          flink:1.17.1-java11                               "/docker-entrypoint.sh standalone-job"    jobmanager          11 seconds ago   Up 10 seconds              6123/tcp, 0.0.0.0:18081->8081/tcp
taskmanager         flink:1.17.1-java11                               "/docker-entrypoint.sh taskmanager"       taskmanager         11 seconds ago   Up 10 seconds              6123/tcp, 8081/tcp

```
### Testing
To be able to test if the Flink application is able to process events we have to sent events to the Input topic, and we can expect events in output topic.
For this purpose, there is [Sandbox](src/test/groovy/dev/yusupov/sandbox/Sandbox.groovy) created with useful commands to try-out.