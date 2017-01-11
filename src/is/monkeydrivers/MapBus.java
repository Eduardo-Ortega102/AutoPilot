package is.monkeydrivers;

import is.monkeydrivers.message.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapBus implements Bus {
    private Map<String, List<Subscriber>> subscribers = new HashMap<>();

    @Override
    public Subscription subscribe(Subscriber subscriber) {
        return type -> subscribersOf(type).add(subscriber);
    }

    private List<Subscriber> subscribersOf(String type) {
        createSubscribersIfNotExist(type);
        return subscribers.get(type);
    }

    private void createSubscribersIfNotExist(String type) {
        if (subscribers.containsKey(type)) return;
        subscribers.put(type, new ArrayList<>());
    }

    @Override
    public void send(Message message) {
        subscribersOf(message.type()).forEach(s -> s.receive(message));
    }
}
