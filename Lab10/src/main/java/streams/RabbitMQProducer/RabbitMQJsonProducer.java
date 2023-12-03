package streams.RabbitMQProducer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class RabbitMQJsonProducer {
    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQJsonProducer.class);
    private static String jsonItem = "{\"company\": \"BlueRiviera\",  \"tradeNumber\": 43,  \"registeredName\" :\"BlueR326\"}";
    private final static String QUEUE_NAME = "jsonQueue";
    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
         Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            byte[] strFromjson = jsonItem.getBytes(StandardCharsets.UTF_8);
            channel.basicPublish("",QUEUE_NAME, null, strFromjson);
            LOG.info("--------- Message sent {} -----------------", jsonItem);
        }
    }
}
