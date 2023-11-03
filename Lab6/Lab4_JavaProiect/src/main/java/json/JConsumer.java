package json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class JConsumer {

	private final static String BOOTSTRAP_SERVERS = ":9092";
	private final static String GROUP_ID = "avro-test";
	private final static String OFFSET_RESET = "earliest";
	private final static String TOPIC = "company-json";

	public static void main(String[] args) {
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OFFSET_RESET);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaJsonSchemaDeserializer.class.getName());
		props.put(KafkaJsonSchemaDeserializerConfig.JSON_VALUE_TYPE,JCompany.class.getName());
		//Class TypeReference<T> - This generic abstract class is used for obtaining full generics type information by sub-classing

		props.put("schema.registry.url", "http://localhost:8081");

		final KafkaConsumer<String, JCompany> consumer = new KafkaConsumer<>(props);

		try {
			consumer.subscribe(Collections.singleton(TOPIC));
			while (true) {
				ConsumerRecords<String, JCompany> records = consumer.poll(Duration.ofSeconds(2));
				for (ConsumerRecord<String, JCompany> data : records) {

					System.out.printf("------------------- key=%s, company name=%s => partition=%d, offset=%d\n", data.key(), data.value().getRegisteredName(),
							data.partition(), data.offset());
				}
			}
		} catch (Exception e) {
			System.out.printf("Exception %s\n", e.getMessage());
		} finally {
			consumer.close();
		}
	}
}
