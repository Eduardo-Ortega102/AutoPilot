package is.monkeydrivers;

@FunctionalInterface
public interface MessageProcessor {
    void processMessage(Message message);
}