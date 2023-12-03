package streams.RabbitMQConsumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class RabbitMQjsonConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQjsonConsumer.class);
    private final static String QUEUE_NAME = "jsonQueue";
    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        LOG.info("ddddddddddddd Waiting for json-s. dddddddddddd");
        System.out.println("zzzzzzzzzzzzz Waiting for json-s. zzzzzzzzzzzzzzzzzz");

        DeliverCallback deliverCallback = ((consumerTag, message) -> {
            String receivedMessage = new String(message.getBody(), StandardCharsets.UTF_8);
            LOG.info("--------- RECEIVED  {} -----------------", receivedMessage);
        });
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});

    }
}
