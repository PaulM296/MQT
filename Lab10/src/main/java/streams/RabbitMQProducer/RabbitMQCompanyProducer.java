package streams.RabbitMQProducer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class RabbitMQCompanyProducer {
    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQCompanyProducer.class);
    private static  String jsonItem = "{\"company\": \"BlueRiver\",  \"tradeNumber\": 23,  \"registeredName\" :\"BlueR43\"}";
    private static final  String QUEUE_NAME = "CompanyQueue1";
    public static void main(String[] argv) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
         Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            Company myComp = new Company("White brezel", 32651, "Fainosul SRL");
            String myCompString = objectMapper.writeValueAsString(myComp);

            byte[] myCompBytesArray = myCompString.getBytes(StandardCharsets.UTF_8);
            LOG.info("--------- Company sent {} -----------------", myCompString);
            channel.basicPublish("",QUEUE_NAME, null, myCompBytesArray);
        }
    }
}
