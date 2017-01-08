package is.monkeydrivers;

import is.monkeydrivers.message.Message;

public interface Bus {

    Subscription subscribe(Subscriber subscriber);

    void send(Message message);

    interface Subscription {
        void to(String type);
    }

}
