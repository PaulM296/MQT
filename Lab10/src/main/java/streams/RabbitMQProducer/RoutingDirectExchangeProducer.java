package streams.RabbitMQProducer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class RoutingDirectExchangeProducer {
    private static final Logger LOG = LoggerFactory.getLogger(RoutingDirectExchangeProducer.class);
    private final static String EXCHANGE_NAME = "directExch";
    private final static String QUEUE_NAME1 = "routingsQueues1";
    private final static String QUEUE_NAME2 = "routingsQueues2";
    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
         Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            channel.queueDeclare(QUEUE_NAME1, false, false, false, null);
            channel.queueDeclare(QUEUE_NAME2, false, false, false, null);
            channel.queueBind(QUEUE_NAME1, EXCHANGE_NAME, "orange");
            channel.queueBind(QUEUE_NAME2, EXCHANGE_NAME, "black");
            channel.queueBind(QUEUE_NAME2, EXCHANGE_NAME, "green");

        for (int i=0; i<10; i++){
            String sentMessage = "message " + i;
            channel.basicPublish(EXCHANGE_NAME,"black", null, sentMessage.getBytes(StandardCharsets.UTF_8));
           // channel.basicPublish("",QUEUE_NAME, null, message.getBytes());
            LOG.info("--------- Message sent {} --------- black binding--------", sentMessage);

            sentMessage = "message " + (i *5);
            channel.basicPublish(EXCHANGE_NAME,"orange", null, sentMessage.getBytes(StandardCharsets.UTF_8));
            LOG.info("--------- Message sent {} --------- orange binding --------", sentMessage);

            sentMessage = "message " + (i *10);
            channel.basicPublish(EXCHANGE_NAME,"green", null, sentMessage.getBytes(StandardCharsets.UTF_8));
            LOG.info("--------- Message sent {} ---------- green binding -------", sentMessage);
        }
        }
    }
}
