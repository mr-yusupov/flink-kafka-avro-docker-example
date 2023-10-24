package dev.yusupov.flink

class FlinkStatefulApplicationRunner {
    private static String[] PARAMETERS = ["--input-topic", "dev.yusupov.flink.kafka.input", "--output-topic", "dev.yusupov.flink.kafka.output"]

    static void main(String[] args) {
        FlinkStatefulApplication.main(PARAMETERS)
    }
}
