package dev.yusupov.flink;

import dev.yusupov.flink.avro.event.InputEvent;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import static dev.yusupov.kafka.KafkaClientProvider.aKafkaSink;
import static dev.yusupov.kafka.KafkaClientProvider.aKafkaSource;
import static org.apache.flink.api.common.eventtime.WatermarkStrategy.noWatermarks;
import static org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION;

public class FlinkStatefulApplication {

    public static void main(String[] args) throws Exception {
        ParameterTool parameters = ParameterTool.fromArgs(args);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.getConfig().setGlobalJobParameters(parameters);

        env.setParallelism(parameters.getInt("parallelism", 1));

        env.enableCheckpointing(parameters.getInt("checkpoint.interval", 1_000), CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setExternalizedCheckpointCleanup(RETAIN_ON_CANCELLATION);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(parameters.getInt("min.pause.between.checkpoints", 1_000));
        env.getCheckpointConfig().setCheckpointTimeout(parameters.getInt("checkpoint.timeout", 90_000));

        env.fromSource(aKafkaSource(parameters.getRequired("input-topic")), noWatermarks(), "kafka-source").uid("kafka-source")
                .keyBy(InputEvent::getId)
                .process(new StatefulCounter()).name("process").uid("process")
                .sinkTo(aKafkaSink(parameters.getRequired("output-topic"))).name("kafka-sink").uid("kafka-sink");

        env.execute("flink-stateful-application");
    }

}
