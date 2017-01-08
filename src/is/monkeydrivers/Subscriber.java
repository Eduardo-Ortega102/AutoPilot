package is.monkeydrivers;

import is.monkeydrivers.message.Message;

public interface Subscriber {
    void receive(Message message);
}
