package streams.RabbitMQProducer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class RabbitMQProducer {
    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQProducer.class);
    private final static String QUEUE_NAME = "helloQueue";
    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
         Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            while (true){
                Scanner sc= new Scanner(System.in); //System.in is a standard input stream.
                System.out.print("Enter a string: ");
                String str= sc.nextLine(); //reads string.
                channel.basicPublish("",QUEUE_NAME, null, str.getBytes(StandardCharsets.UTF_8));
                // channel.basicPublish("",QUEUE_NAME, null, message.getBytes());
                LOG.info("--------- Message sent {} -----------------", str);
            }
        }
    }
}
