package streams;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import models.Purchase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;

import static org.apache.kafka.streams.state.RocksDBConfigSetter.LOG;

public class PurchaseStatisticsProduder {

	public static String[] products = { "kettle", "hair dryer", "toaster", "grill" };

	public static void main(final String[] args) throws IOException, InterruptedException {

		final Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
		props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");

		try (final KafkaProducer<String, Purchase> producer = new KafkaProducer<>(props)) {

			long id = 0;
			for (int j = 0; j < 40; j++) {
				final String product = products[new Random().nextInt(products.length)];
				final int customer = new Random().nextInt(5);
				final int randomNo = new Random().nextInt(10);
				final int amount = new Random().nextInt(8)+1;
				Purchase purchase = Purchase.newBuilder().setId(id++).setProduct(product).setAmount(amount).setSum(10)
						.setCustomerId(customer).build();
				LOG.info("4444444444444444444   + id = {}, product name = {}, amount = {}, sum = {}, customerId = {}, schema ={} ",
						purchase.getId(), purchase.getProduct(), purchase.getAmount(), purchase.getSum(),
						purchase.getCustomerId(), purchase.getSchema());
				producer.send(new ProducerRecord<>("Purchases", String.valueOf(randomNo), purchase));

			}

		} 
	}

}
