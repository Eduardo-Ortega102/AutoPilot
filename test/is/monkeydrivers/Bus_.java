package is.monkeydrivers;

import is.monkeydrivers.message.MapBus;
import is.monkeydrivers.message.Message;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class Bus_ {

    private Bus bus;
    private List<Subscriber> subscribers = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        bus = new MapBus();
    }

    @Test
    public void should_send_a_message_to_a_subscriber() throws Exception {
        Message message = messageOfType("foo");

        String type = "foo";
        Subscriber subscriber = createSubscriberToType(type);

        bus.send(message);
        verify(subscriber).receive(message);
    }


    @Test
    public void should_not_send_a_message_to_a_subscriber_that_is_not_subscribed() throws Exception {
        Message message = messageOfType("faa");
        Subscriber subscriber = createSubscriberToType("foo");
        bus.send(message);
        verify(subscriber, times(0)).receive(message);
    }

    @Test
    public void should_send_a_message_to_all_subscribers() throws Exception {
        createSubscriberToType("foo");
        createSubscriberToType("foo");
        createSubscriberToType("foo");
        createSubscriberToType("foo");

        Message message = messageOfType("foo");
        bus.send(message);

        subscribers.forEach(s->verify(s).receive(message));
    }


    @Test
    public void should_not_send_a_message_to_subscribers_that_are_not_subscribed() throws Exception {
        createSubscriberToType("faa");
        createSubscriberToType("faa");
        createSubscriberToType("faa");
        Subscriber target = createSubscriberToType("faa", "foo");

        Message message = messageOfType("foo");
        bus.send(message);

        subscribers.forEach(s->verify(s,times(s==target ? 1 : 0)).receive(message));
    }

    private Subscriber createSubscriberToType(String... types) {
        Subscriber subscriber = mock(Subscriber.class);
        for (String type : types) bus.subscribe(subscriber).to(type);
        subscribers.add(subscriber);
        return subscriber;
    }

    private Message messageOfType(String type) {
        Message message = mock(Message.class);
        doReturn(type).when(message).type();
        return message;
    }


}
