package ma.enset.springcloudkafkastreams.web;

import ma.enset.springcloudkafkastreams.entites.PageEvent;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
public class PageEventRestController {
    private StreamBridge streamBridge;
    private InteractiveQueryService interactiveQueryService;


    public PageEventRestController(StreamBridge streamBridge, InteractiveQueryService interactiveQueryService) {
        this.streamBridge = streamBridge;
        this.interactiveQueryService = interactiveQueryService;
    }


    @GetMapping("/publish/{topic}/{name}")
    public PageEvent publish(@PathVariable String topic, @PathVariable String name){
        PageEvent pageEvent = new PageEvent(name, Math.random()>0.5?"U1":"U2", new Date(), (long) new Random().nextInt(9000));
        streamBridge.send(topic, pageEvent);
        return pageEvent;
    }

    @GetMapping(value = "/analytics",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, Long>> analytics() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(seq -> {
                            Map<String, Long> map = new HashMap<>();
                            ReadOnlyWindowStore<String, Long> windowStore = interactiveQueryService.getQueryableStore("count-pages", QueryableStoreTypes.windowStore());
                            Instant now = Instant.now();
                            Instant from = now.minusMillis(5000);
                            KeyValueIterator<Windowed<String>, Long> fetchAll = windowStore.fetchAll(from, now);
                            while (fetchAll.hasNext()) {
                                KeyValue<Windowed<String>, Long> next = fetchAll.next();
                                map.put(next.key.key(),next.value);
                            }
                            return map;
                        }
                ).share();
    }

}
