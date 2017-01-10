package is.monkeydrivers;

import is.monkeydrivers.message.Message;

@FunctionalInterface
public interface MessageFiller {
    Message withContent(Object content);
}
