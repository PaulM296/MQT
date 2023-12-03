package streams.RabbitMQConsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class RabbitMQCompanyConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQCompanyConsumer.class);
    private final static String QUEUE_NAME = "CompanyQueue1";
    public static void main(String[] argv) throws Exception {
        ObjectMapper objectMapper =  new ObjectMapper();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        LOG.info("ddddddddddddd Waiting for Companies. dddddddddddd");
        System.out.println("zzzzzzzzzzzzz Waiting for companies. zzzzzzzzzzzzzzzzzz");

        DeliverCallback deliverCallback = ((consumerTag, message) -> {
            String receivedMessage = new String(message.getBody(), StandardCharsets.UTF_8);
            Company receivedComp = objectMapper.readValue(receivedMessage, Company.class);

        //    Company obj = SerializationUtils.deserialize(receivedMessage.getBytes(StandardCharsets.UTF_8));
            LOG.info("--------- RECEIVED  {} ----- {}  {}-----------", receivedComp.getCompany(), receivedComp.registeredName, receivedComp.getRegisteredName());
        });
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});

    }
}
