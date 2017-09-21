# Spring Streams

This project contains POC Projects on how to produce/consume Kafka Streams using
the Spring Framework.

The two main implementations are through the following projects:

* Spring Kafka
* Spring Cloud Streams

## Methodology

To test the use of streaming production/consumption, a stream of quotes is
produced.

### Producing messages

Following the Kafka message pattern, every quote produced has a key/value
values. For this POC, quotes of Game of Thrones and Chuck Norris facts are
randomly generated. The message contains a `GoT` key if a Game Of Thrones quote
is produced or a `Chuck` key in case a fact is produced.

### Processing messages

The processing phase accumulates messages during a 2 second period and generates
new messages (maximum of 2). Every new message contain one of the produced keys
(`GoT` or `Chuck`) with all quotes on that topic concatenated (with a ` *** `
between them) and upper cased.

For example, the following input:

    Message(key="GoT", value="All dwarfs are bastards in their father's eyes")
    Message(key="Chuck", value="Chuck Norris doesn't delete files, he blows them away.")
    Message(key="GoT", value="Some old wounds never truly heal, and bleed again at the slightest word.")

has the following output:

    Message(key="GoT", value="ALL DWARFS ARE BASTARDS IN THEIR FATHER'S EYES *** SOME OLD WOUNDS NEVER TRULY HEAL, AND BLEED AGAIN AT THE SLIGHTEST WORD.")
    Message(key="Chuck", value="CHUCK NORRIS DOESN'T DELETE FILES, HE BLOWS THEM AWAY.")

### Consuming messages

The consumption phase if a simply stdout print of a message key and value separated by
`|`.

For example:

    GoT | ALL DWARFS ARE BASTARDS IN THEIR FATHER'S EYES *** SOME OLD WOUNDS NEVER TRULY HEAL, AND BLEED AGAIN AT THE SLIGHTEST WORD.

## Differences

* Cloud Stream is an abstraction over the underlying message bus (Kafka, RabbitMQ, Kinesis).
* Kafka Stream uses an easy-to-use API to work directly with Kafka (and sub projects, like Kafka Streams).
* Kafka Stream also gives more control with Kafka Production/Consumer configuration.

### Message Production

With Kafka Stream:

```java
@Scheduled(fixedRate = 500)
public void sendMessage() {
    Faker fkr = new Faker();
    String topic = "streamingTopic1";
    if (Math.random() <= 0.5) kafkaTemplate.send(topic, "GoT", fkr.gameOfThrones().quote());
    else kafkaTemplate.send(topic, "Chuck", fkr.chuckNorris().fact());
}
```
With Cloud Stream:

```groovy
@Bean
@InboundChannelAdapter(value = MultiOutputSource.OUTPUT1, poller = @Poller(fixedDelay = "500", maxMessagesPerPoll = "1"))
public MessageSource<String> messageSource() {
    Faker fkr = new Faker()
    new MessageSource<String>() {
        Message<String> receive() {
            def data = Math.random() <= 0.5 ? ['GoT', fkr.gameOfThrones().quote()] : ['Chuck', fkr.chuckNorris().fact()]
            MessageBuilder.withPayload(data.last())
                    .setHeader(KafkaHeaders.MESSAGE_KEY, data.first().bytes).build()
        }
    }
}
```

### Message Processing

With Kafka Stream:

```java
@Bean
public KStream<String, String> kStream(KStreamBuilder kStreamBuilder) {
    Serde<String> strSerde = Serdes.String();
    KStream<String, String> stream = kStreamBuilder.stream(strSerde, strSerde, "streamingTopic1");
    KStream<String, String> newStream = stream.mapValues(String::toUpperCase)
            .groupByKey()
            .reduce((String value1, String value2) -> value1 + " *** " + value2,
                    TimeWindows.of(2000), "windowStore")
            .toStream()
            .map((windowedId, value) -> new KeyValue<>(windowedId.key(), value));
    newStream.to("streamingTopic2");
    return stream;
}
```
With Cloud Stream:

```groovy
@StreamListener
@Output(MultiOutputSource.OUTPUT2)
Observable<Message<String>> receive(@Input(MultiInputSink.INPUT1) Observable<Message<String>> input) {
    input.buffer(2, TimeUnit.SECONDS).flatMap { el ->
        Observable.from(el).groupBy { it.headers[KafkaHeaders.RECEIVED_MESSAGE_KEY] as List<Byte> }.flatMap { kv ->
            kv.map { it.payload }.reduce { a, b -> "$a *** $b" }.map {
                MessageBuilder.withPayload(it)
                        .setHeader(KafkaHeaders.MESSAGE_KEY, kv.key as byte[]).build()
            }
        }
    }
}
```

### Message Consumption

With Kafka Stream:

```java
@KafkaListener(id = "consumer", topics = "streamingTopic2")
public void consume(String msg, @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key) {
    LOGGER.info(key + " | " + msg);
}
```
With Cloud Stream:

```groovy
@StreamListener(MultiInputSink.INPUT2)
public void receive(Message<String> message) {
    def key = new String(message.headers[KafkaHeaders.RECEIVED_MESSAGE_KEY] as byte[], 'UTF-8')
    log.info("$key | ${message.payload}")
}
```

## Execution

Every POC is a gradle project. To run the examples, just run the following
command on the subproject sub directory:

    ./gradlew bootRun
