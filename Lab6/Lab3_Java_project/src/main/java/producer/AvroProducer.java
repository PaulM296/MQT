package producer;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class AvroProducer {
    private static final Logger LOG = LoggerFactory.getLogger(CompanyProducer.class);
    private final static String BOOTSTRAP_SERVERS = ":9092";

    private final static String CLIENT_ID = "avro-test";

    private static Producer<String, AvroCompany> producer;

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url","http://localhost:8081");

        producer = new KafkaProducer<>(props);

        send("events2");
    }

    public static void send(String topic){
        final int number = new Random().nextInt(10);
        AvroCompany my_avroComp = new AvroCompany();
        my_avroComp.setRegisteredName("PcGarage");
        my_avroComp.setTradeNumber(55);

        ProducerRecord<String, AvroCompany> data = new ProducerRecord<>(topic, "key"+number, my_avroComp);
        try {
            RecordMetadata meta = producer.send(data).get();
            LOG.info("key = {}, value = {} ==> partition = {}, offset = {}", data.key(), data.value(), meta.partition(), meta.offset());
        }catch (InterruptedException | ExecutionException e){
            producer.flush();
        }
    }

}
