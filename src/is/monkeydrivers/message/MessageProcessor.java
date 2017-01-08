package is.monkeydrivers.message;

@FunctionalInterface
public interface MessageProcessor {
    void processMessage(Message message);
}
