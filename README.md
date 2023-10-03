# Event Driven Distributed Processing
- KAFKA, KAFKA STREAMS
- Spring Cloud Stream Functions

```
This project leverages Event-Driven Distributed Processing by employing Apache Kafka and Kafka Streams 
for real-time data ingestion, processing, and analysis. 
Additionally, it utilizes Spring Cloud Stream Functions
```

# Table of Contents
- [Prerequisites](#prerequisites)
- [Starting KAFKA Server](#starting-kafka-server)
    - [Start zookeeper](#start-zookeeper)
    - [Start KAFKA server](#start-kafka-server)
 
- [Services Types](#services-types)
    - [KAFKA Console](#kafka-console)
    - [Docker Compose](#docker-compose)
    - [Rest Controller with Stream Bridge](#rest-controller-with-stream-bridge)
    - [Cunsomer](#cunsomer)
    - [Supplier](#supplier)
    - [Function](#function)
    - [KAFKA Stream](#kafka-stream)
  


## Prerequisites
Before running this application, you need to have the following software installed on your system :

```java
- Java Development Kit (JDK) version 11 or later
- Kafka
```
## Project Structure
<pre>

D:.
│ 
├───src
│   ├───main
│   │   ├───java
│   │   │   └───ma
│   │   │       └───enset
│   │   │           └───springcloudkafkastreams
│   │   │               │   SpringcloudKafkaStreamsApplication.java
│   │   │               │
│   │   │               ├───entites
│   │   │               │       PageEvent.java
│   │   │               │
│   │   │               ├───service
│   │   │               │       PageEventService.java
│   │   │               │
│   │   │               └───web
│   │   │                       PageEventRestController.java
│   │   │
│   │   └───resources
│   │       │   application.properties
│   │       │
│   │       ├───static
│   │       └───templates
│ 
│   docker-compose.yml
│   pom.xml

</pre>

## Starting KAFKA Server

### Start zookeeper
`start bin\windows\zookeeper-server-start.bat config/zookeeper.properties`

![3](https://github.com/el-moudni-hicham/springcloud-kafka/assets/85403056/2c4904e4-2b0b-460e-b708-1a5de16d4dd0)

### Start KAFKA server
`start bin\windows\kafka-server-start.bat config/server.properties`

![5](https://github.com/el-moudni-hicham/springcloud-kafka/assets/85403056/ebef2645-7a5c-44b9-96b7-db65b3c6f701)

## Services Types

### Kafka Console

> Subscribing to a topic to consume messages.

`start kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic T1`

> Producing messages to the topic

`start kafka-console-producer.bat --broker-list localhost:9092 --topic T1`

![7](https://github.com/el-moudni-hicham/springcloud-kafka/assets/85403056/9689f156-8e81-4a31-af93-00d5eaad6f1e)

### Docker compose
`docker-compose up -d`

`docker exec --interactive --tty broker kafka-console-consumer --bootstrap-server broker:9092 --topic T2`

`docker exec --interactive --tty broker kafka-console-producer --bootstrap-server broker:9092 --topic T2`

![image](https://github.com/el-moudni-hicham/springcloud-kafka/assets/85403056/0a297665-af60-4a56-9065-daa963f57aed)

### Rest Controller with Stream Bridge

```java
    @GetMapping("/publish/{topic}/{name}")
    public PageEvent publish(@PathVariable String topic, @PathVariable String name){
        PageEvent pageEvent = new PageEvent(
                name,
                Math.random()>0.5?"U1":"U2",
                new Date(),
                (long) new Random().nextInt(9000));
        streamBridge.send(topic, pageEvent);
        return pageEvent;
    }
```

![9](https://github.com/el-moudni-hicham/springcloud-kafka/assets/85403056/21805011-e5a3-4f2c-a38a-577783f5ffb1)
![10](https://github.com/el-moudni-hicham/springcloud-kafka/assets/85403056/7f2e3e85-748e-43c3-bd81-8e308123d370)

### Cunsomer

```java
    @Bean
    public Consumer<PageEvent> pageEventConsumer(){
        return (input)->{
            System.out.println(input.toString());
        };
    }
```
![11](https://github.com/el-moudni-hicham/springcloud-kafka/assets/85403056/84b2fd1d-4356-4a13-92b8-b75b7e9096cc)

### Supplier

```java
    @Bean
    public Supplier<PageEvent> pageEventSupplier(){
        return ()-> new PageEvent(
                        Math.random()>0.5?"P1":"P2",
                        Math.random()>0.5?"U1":"U2",
                        new Date(),
                        (long) new Random().nextInt(9000)
                    );
    }
```
![12](https://github.com/el-moudni-hicham/springcloud-kafka/assets/85403056/52d1158a-0502-40a5-9b37-6cdd269bfc37)

### Function

```java
    @Bean
    public Function<PageEvent, PageEvent> pageEventFunction(){
        return (input)->{
            input.setName(input.getName()+"after");
            input.setUser("User");
            return input;
        };
    }
```
![13](https://github.com/el-moudni-hicham/springcloud-kafka/assets/85403056/3b87552a-702d-408d-97ec-5842c02eb3ff)

### KAFKA Stream

*  Data Analytics Real Time Stream Processing with Kaflka Streams

```java
    @Bean
    public Function<KStream<String, PageEvent>, KStream<String, Long>> kStreamFunction(){
        return (input)->{
            return input
                    .filter((k,v) -> v.getDuration()>100)
                    .map((k,v) -> new KeyValue<>(v.getName(), 0L))
                    .groupBy((k,v) -> k, Grouped.with(Serdes.String(), Serdes.Long()))
                    .windowedBy(TimeWindows.of(Duration.ofMillis(5000)))
                    .count(Materialized.as("count-pages"))
                    .toStream()
                    .map((k,v) -> new KeyValue<>(""+ k.window().startTime() + " -> " + k.window().endTime() + " , page : " + k.key() , v));
        };
    }
```

![image](https://github.com/el-moudni-hicham/springcloud-kafka/assets/85403056/74eb541e-39a4-4a36-8af8-ff7592a88563)



* Web application that displays the results of Stream Data Analytics in real time

![image](https://github.com/el-moudni-hicham/springcloud-kafka/assets/85403056/304184b8-398e-4a99-9a45-a83545eede36)

![image](https://github.com/el-moudni-hicham/springcloud-kafka/assets/85403056/c7fe942e-3533-4fe4-870b-40a3a7783ee8)







