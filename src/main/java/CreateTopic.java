import io.confluent.shaded.com.google.common.collect.ImmutableMap;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Properties;

public class CreateTopic {
    private static final Logger logger = LoggerFactory.getLogger(CreateTopic.class);


    public static void createTopic(String topicName,ImmutableMap<String, Object> properties)  {
        try (Admin admin = Admin.create(properties)) {
            int partitions = 1;
            short replicationFactor = 3;
            NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);

            CreateTopicsResult result = admin.createTopics(Collections.singleton(newTopic));

            // get the async result for the new topic creation
            KafkaFuture<Void> future = result.values()
                    .get(topicName);

            // call get() to block until topic creation has completed or failed
            future.get();
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
}
