package streams.RabbitMQConsumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class RoutingDirectExchangeConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(RoutingDirectExchangeConsumer.class);
    private final static String EXCHANGE_NAME = "directExch";
    private final static String QUEUE_NAME1 = "routingsQueues1";
    private final static String QUEUE_NAME2 = "routingsQueues2";
    public static void main(String[] argv) throws Exception {

        if (argv.length < 1) {
            System.err.println("Usage: RoutingDirectExchangeProducer [orange] [green] [black]");
            System.exit(1);
        }

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        channel.queueDeclare(QUEUE_NAME1, false, false, false, null);
        channel.queueDeclare(QUEUE_NAME2, false, false, false, null);
        channel.queueBind(QUEUE_NAME1, EXCHANGE_NAME, "orange");
        channel.queueBind(QUEUE_NAME2, EXCHANGE_NAME, "black");
        channel.queueBind(QUEUE_NAME2, EXCHANGE_NAME, "green");
        LOG.info("ppppppppppp Direct exchange. Waiting for messages ppppppppppppp");
        System.out.println("yyyyyyyyy Direct exchange. Waiting for messages. yyyyyyyyyyyyyyyy");

        DeliverCallback deliverCallbackQ1 = ((consumerTagQ1, messageQ1) -> {
            String receivedMessage = new String(messageQ1.getBody(), StandardCharsets.UTF_8);
            LOG.info("--------- RECEIVED  {} ------- queue Q1 -- {} ---", receivedMessage, messageQ1.getEnvelope().getRoutingKey());
        });
        channel.basicConsume(QUEUE_NAME1, true, deliverCallbackQ1, consumerTag -> {});

        DeliverCallback deliverCallbackQ2 = ((consumerTagQ2, messageQ2) -> {
            String receivedMessage = new String(messageQ2.getBody(), StandardCharsets.UTF_8);
            LOG.info("--------- RECEIVED  {} --------- queue Q2 ---- {} ----", receivedMessage, messageQ2.getEnvelope().getRoutingKey());
        });
        channel.basicConsume(QUEUE_NAME2, true, deliverCallbackQ2, consumerTag -> {});
    }
}
