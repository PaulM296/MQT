package streams;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import models.Purchase;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import static org.apache.kafka.streams.state.RocksDBConfigSetter.LOG;

public class PurchaseStatisticsKStreamConsumer {

	public static final String SCHEMA_REGISTRY_URL = "http://localhost:8081";

	public static void main(final String[] args) throws Exception {
		final Properties streamsConfiguration = new Properties();

		streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "purchase-statistics-example");
		streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "purchase-statistics-example-client");

		streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

		streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
		streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/streams/");
		streamsConfiguration.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL);

		final StreamsBuilder builder = new StreamsBuilder();

		final KStream<String, Purchase> purchases = builder.stream("Purchases");

		final Serde<Purchase> specificAvroSerde = new SpecificAvroSerde<>();
		final boolean isKeySerde = false;
		specificAvroSerde.configure(Collections.singletonMap(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
				SCHEMA_REGISTRY_URL), isKeySerde);

		// 1.1
		purchases.peek((key, value) -> LOG.info("Peek - Key: {}, Value: {}", key, value));

		// 1.2
		KStream<String, Purchase> filteredPurchases = purchases.filter((key, value) -> value.getAmount() > 100);

		// 1.3
		KStream<String, String> mappedValueStream = purchases.mapValues(value -> value.getProduct().toString());

		// 1.4
		KStream<String, String> mappedStream = purchases.map((key, value) ->
				KeyValue.pair(key.toUpperCase(), value.getProduct() + ": $" + value.getAmount()));

		filteredPurchases.to("FilteredPurchases", Produced.with(Serdes.String(), specificAvroSerde));
		mappedValueStream.to("MappedValuePurchases", Produced.with(Serdes.String(), Serdes.String()));
		mappedStream.to("MappedPurchases", Produced.with(Serdes.String(), Serdes.String()));

		// 2.1
		purchases.groupBy((key, value) -> value.getProduct().toString(),
						Grouped.with(Serdes.String(), specificAvroSerde))
				.count()
				.mapValues(v -> v.toString())
				.toStream()
				.peek((groupKey, value) -> LOG.info("GroupBy - Key: {}, Value: {}", groupKey, value))
				.to("GroupedPurchaseStatistics", Produced.with(Serdes.String(), Serdes.String()));

		// 2.2
		purchases.groupByKey()
				.reduce((value1, value2) -> {
					String mergedProduct = value1.getProduct().toString() + value2.getProduct().toString();
					return new Purchase(value1.getId(), mergedProduct, value1.getAmount() + value2.getAmount(), value1.getSum(), value1.getCustomerId());
				})
				.toStream()
				.peek((key, value) -> LOG.info("Reduce - Key: {}, Value: {}", key, value))
				.to("ReducedPurchaseStatistics", Produced.with(Serdes.String(), specificAvroSerde));

		// 2.3
		purchases.groupByKey()
				.count()
				.toStream()
				.peek((key, value) -> LOG.info("Count - Key: {}, Value: {}", key, value))
				.to("CountedPurchaseStatistics", Produced.with(Serdes.String(), Serdes.Long()));

		// 3.1
		purchases.groupByKey()
				.windowedBy(TimeWindows.of(Duration.ofMinutes(5)).advanceBy(Duration.ofMinutes(1)))
				.count()
				.toStream()
				.peek((key, value) -> LOG.info("Hopping Window - Key: {}, Value: {}", key, value))
				.to("HoppingWindowPurchaseStatistics", Produced.with(WindowedSerdes.timeWindowedSerdeFrom(String.class), Serdes.Long()));

		// 3.2
		purchases.groupByKey()
				.windowedBy(TimeWindows.of(Duration.ofMinutes(5)))
				.count()
				.toStream()
				.peek((key, value) -> LOG.info("Tumbling Window - Key: {}, Value: {}", key, value))
				.to("TumblingWindowPurchaseStatistics", Produced.with(WindowedSerdes.timeWindowedSerdeFrom(String.class), Serdes.Long()));

		// 3.3
		purchases.groupByKey()
				.windowedBy(SessionWindows.with(Duration.ofMinutes(10)))
				.count()
				.toStream()
				.peek((key, value) -> LOG.info("Session Window - Key: {}, Value: {}", key, value))
				.to("SessionWindowPurchaseStatistics", Produced.with(WindowedSerdes.timeWindowedSerdeFrom(String.class), Serdes.Long()));

		// 3.4
		purchases.groupByKey()
				.windowedBy(TimeWindows.of(Duration.ofMinutes(10)).advanceBy(Duration.ofMinutes(5)))
				.count()
				.toStream()
				.peek((key, value) -> LOG.info("Sliding Window - Key: {}, Value: {}", key, value))
				.to("SlidingWindowPurchaseStatistics", Produced.with(WindowedSerdes.timeWindowedSerdeFrom(String.class), Serdes.Long()));

		// 4.1
		purchases.groupByKey()
				.windowedBy(TimeWindows.of(Duration.ofMinutes(5)).advanceBy(Duration.ofMinutes(1)).grace(Duration.ofMinutes(1)))
				.count()
				.toStream()
				.peek((key, value) -> LOG.info("Hopping Window with Grace Period - Key: {}, Value: {}", key, value))
				.to("HoppingWindowWithGracePeriodPurchaseStatistics", Produced.with(WindowedSerdes.timeWindowedSerdeFrom(String.class), Serdes.Long()));

		// 4.2
		purchases.groupByKey()
				.windowedBy(TimeWindows.of(Duration.ofMinutes(5)).grace(Duration.ofMinutes(1)))
				.count()
				.toStream()
				.peek((key, value) -> LOG.info("Tumbling Window with Grace Period - Key: {}, Value: {}", key, value))
				.to("TumblingWindowWithGracePeriodPurchaseStatistics", Produced.with(WindowedSerdes.timeWindowedSerdeFrom(String.class), Serdes.Long()));

		// 5

		purchases.groupByKey()
				.aggregate(() -> 0.0,
						(key, purchase,aggregate) -> aggregate + purchase.getAmount(),
						Materialized.with(Serdes.String(), Serdes.Double()))
				.toStream()
				.to("SumOfAmountsByProduct", Produced.with(Serdes.String(), Serdes.Double()));

		// 6.1

		KStream<String, String> stream1 = builder.stream("stream1");
		KStream<String, String> stream2 = builder.stream("stream2");

		KTable<String, String> table2 = stream2.toTable();

		KStream<String, String> leftJoinStream = stream1.leftJoin(
				table2,
				(value1, value2) -> (value1 != null ? value1 : "null") + ", " + (value2 != null ? value2 : "null"),
				Joined.with(Serdes.String(), Serdes.String(), Serdes.String())
		);

		leftJoinStream.to("LeftJoinOutput");

		final KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);
		streams.cleanUp();
		streams.start();

		Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
	}
}
