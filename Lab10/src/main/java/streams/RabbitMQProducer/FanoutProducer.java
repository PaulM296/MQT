package streams.RabbitMQProducer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class FanoutProducer {
    private static final Logger LOG = LoggerFactory.getLogger(FanoutProducer.class);
    private final static String EXCHANGE_NAME = "fanoutExch";
    private final static String QUEUE_NAME1 = "broadcastQueue1";
    private final static String QUEUE_NAME2 = "broadcastQueue2";
    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
         Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
            channel.queueDeclare(QUEUE_NAME1, false, false, false, null);
            channel.queueBind(QUEUE_NAME1, EXCHANGE_NAME, "");

            channel.queueDeclare(QUEUE_NAME2, false, false, false, null);
            channel.queueBind(QUEUE_NAME2, EXCHANGE_NAME, "");

            for (int i = 0; i < 10; i++) {
                String sentMessage = "message " + i;
                channel.basicPublish(EXCHANGE_NAME, "", null, sentMessage.getBytes(StandardCharsets.UTF_8));
                // channel.basicPublish("",QUEUE_NAME, null, message.getBytes());
                LOG.info("--------- Message sent {} -----------------", sentMessage);
            }
        }
    }
}
