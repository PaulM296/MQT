package consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ConsumerToProducer {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerToProducer.class);
    private static final String OUR_BOOTSTRAP_SERVERS = ":9092";
    private static final String OFFSET_RESET = "earliest";
    private static final String OUR_CONSUMER_GROUP_ID = "group_1";
    private static final String OUR_CLIENT_ID = "firstProducer";
    private static Producer<String, String> producer;
    KafkaConsumer<String, String> kafkaConsumer;

    public ConsumerToProducer(Properties consumerPropsMap, Properties producerPropsMap){
        kafkaConsumer = new KafkaConsumer<>(consumerPropsMap);
        producer = new KafkaProducer<>(producerPropsMap);
    }

    public static Properties buildProducerPropsMap(){
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, OUR_BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, OUR_CLIENT_ID);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return props;
    }
    public static Properties buildConsumerPropsMap(){
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, OUR_BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, OUR_CONSUMER_GROUP_ID);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OFFSET_RESET);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        return props;
    }

    public void pollKafka(String kafkaTopicName){

        kafkaConsumer.subscribe(Collections.singleton(kafkaTopicName));
        //kafkaConsumer.subscribe(List.of(topicName, topicName2));

        Duration pollingTime = Duration.of(2, ChronoUnit.SECONDS);
        while (true){
            // get records from kafka
            // The poll method is a blocking method waiting for specified time in seconds.
            // If no records are available after the time period specified, the poll method returns an empty ConsumerRecords.
            ConsumerRecords<String, String> records = kafkaConsumer.poll(pollingTime);

            // consume the records
            records.forEach(crtRecord -> {
                LOG.info("------ ConsumerToProducer ------------- topic ={}  key = {}, value = {} => partition = {}, offset = {}", kafkaTopicName, crtRecord.key(), crtRecord.value(), crtRecord.partition(), crtRecord.offset());

                if(crtRecord.value().equals("v7"))
                {
                    sendFilteredMessages(crtRecord.key(), crtRecord.value());
                }
            });
        }
    }

    public static void sendFilteredMessages(String key, String value){
        ProducerRecord<String, String> data = new ProducerRecord<>("events1", key, value);
        try {
            RecordMetadata meta = producer.send(data).get();
            LOG.info("key = {}, value = {} ==> partition = {}, offset = {}", data.key(), data.value(), meta.partition(), meta.offset());
        }catch (InterruptedException | ExecutionException e){
            producer.flush();
        }
    }

    public static void main(String[] args) {
        ConsumerToProducer consumer = new ConsumerToProducer(buildConsumerPropsMap(), buildProducerPropsMap());
        consumer.pollKafka("events2");
    }

}
