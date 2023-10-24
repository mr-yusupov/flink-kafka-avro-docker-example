package dev.yusupov.sandbox

import dev.yusupov.flink.avro.event.InputEvent
import dev.yusupov.flink.avro.event.OutputEvent
import dev.yusupov.kafka.Kafka
import dev.yusupov.kafka.KafkaUtils
import dev.yusupov.kafka.SchemaRegistryUtils
import dev.yusupov.kafka.SimpleKafkaConsumer
import dev.yusupov.kafka.SimpleKafkaProducer
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

//@Disabled
class Sandbox {
    final Kafka KAFKA = new Kafka(bootstrapServers: '127.0.0.1:19092', schemaRegistryUrl: 'http://0.0.0.0:28081')

    @Test
    void setupKafka() {
        def topics = [
                "dev.yusupov.flink.kafka.input",
                "dev.yusupov.flink.kafka.output"
        ]
        topics.each {topic -> KafkaUtils.recreateTopic(KAFKA, topic, 1, 1, 1) }
    }

    @Test
    void listTopicNames() {
        KafkaUtils.getTopicNames(KAFKA).findAll().each {
            println it
        }
    }

    @Test
    void listTopicsWithDescription() {
        KafkaUtils.describeTopics(KAFKA, KafkaUtils.getTopicNames(KAFKA, true)).findAll().each {
            println it
        }
    }

    @Test
    void listSchemaSubjects() {
        SchemaRegistryUtils.getSubjects(KAFKA).each {
            println it
        }
    }

    @Test
    void listSchemas() {
        SchemaRegistryUtils.getSubjects(KAFKA).each { subject ->
            SchemaRegistryUtils.getSchemaMetadata(KAFKA, subject).each { metadata ->
                println "subject: $subject, version: $metadata.version, id: $metadata.id, schema, $metadata.schema"
            }
        }
    }

    @Test
    void deleteAllSchemas() {
        SchemaRegistryUtils.getSubjects(KAFKA).each { subject ->
            SchemaRegistryUtils.deleteSubject(KAFKA, subject)
        }
    }

    @Test
    void registerSchema() {
        SchemaRegistryUtils.registerSubjectSchema(KAFKA, 'dev.yusupov.flink.avro.event.InputEvent', InputEvent.SCHEMA$)
    }

    @Test
    void sendEventToKafka() {
        new SimpleKafkaProducer(KAFKA).withCloseable { producer ->
            10.times { number ->
                String id = 'eventId'
                producer.produce('dev.yusupov.flink.kafka.input',
                        InputEvent.newBuilder().setId(id).setVersion(number).build(), id)
            }
        }
    }

    @Test
    void readEventFromKafka() {
        new SimpleKafkaConsumer<OutputEvent>(KAFKA)
                .withCloseable { consumer ->
                    consumer.consume('dev.yusupov.flink.kafka.output', 3, 1)
                }
    }
}