

import io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor;

import java.io.IOException;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.confluent.shaded.com.google.common.collect.ImmutableMap;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class TestProducer {


    /**
     * Java producer.
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("Create topic");
        final ImmutableMap<String, Object> props = ImmutableMap.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "172.20.0.7:19092",
                AdminClientConfig.RETRIES_CONFIG, 5);
        CreateTopic.createTopic("SSL-PLAIN-SCRAM-TOPIC",props);
        System.out.println("Starting Java producer.");

        final Properties properties = PropertiesLoader.load("tls/client-creds/client-scram.properties");

        //properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,
                List.of(MonitoringProducerInterceptor.class));

        KafkaProducer<Long, String> producer = new KafkaProducer<>(properties);

        // Adding a shutdown hook to clean up when the application exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Closing producer.");
            producer.close();
        }));

         long time = System.currentTimeMillis();
        int sendMessageCount=Integer.parseInt(args[0]);
        final CountDownLatch countDownLatch = new CountDownLatch(sendMessageCount);

                try {
                    for (long index = time; index < time + sendMessageCount; index++){
                        final ProducerRecord<Long, String> record = new ProducerRecord<>("SSL-PLAIN-SCRAM-TOPIC", index, "Hello World " + index);
                        producer.send(record, (metadata, exception) -> {
                            long elapsedTime = System.currentTimeMillis() - time;
                            if (metadata != null) {
                                System.out.printf("sent record(key=%s value=%s " +
                                    "meta(partition=%d, offset=%d) time=%d\n",
                                        record.key(), record.value(), metadata.partition(),
                                        metadata.offset(), elapsedTime);
                            } else {
                                exception.printStackTrace();
                            }
                            countDownLatch.countDown();
                        });
                    }
                    countDownLatch.await(25, TimeUnit.SECONDS);
                } finally {
                    producer.flush();
                    producer.close();
                }
            }
    }

