package json;

import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExampleJProducer {

	//private static final Logger LOG = LoggerFactory.getLogger(ExampleProducer.class);

	private static final String BOOTSTRAP_SERVERS = ":9092";
	private static final String CLIENT_ID = "avroCl";
	private final static String TOPIC = "company-json";
	private static Producer<String, JCompany> producer;

	public static void main(String[] args) {

		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		props.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaJsonSchemaSerializer.class.getName());
		props.put("schema.registry.url","http://localhost:8081");

		producer = new KafkaProducer<>(props);

		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(() -> send(TOPIC), 0, 3, TimeUnit.SECONDS);

		Runtime.getRuntime().addShutdownHook(new Thread(producer::close));
	}

	@SuppressWarnings({ "boxing", "unused" })
	public static void send(String topic) {
		final int number = new Random().nextInt(10);
		JCompany comp = new JCompany();
		comp.setTradeNumber(number);
		comp.setRegisteredName("MyCompany"+Integer.toString(number));
		ProducerRecord<String, JCompany> data = new ProducerRecord<>(topic, "key" + number, comp);
		try {
			RecordMetadata meta = producer.send(data).get();
			System.out.println(
					String.format("-- json Producer --- key = %s, Company name = %s => partition = %d, offset= %d"
							,data.key(), data.value().getRegisteredName(), meta.partition(), meta.offset()));
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			producer.flush();
		}
	}

}
