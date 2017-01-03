package is.monkeydrivers;

public interface Message<Content> {
    String type();
    Content getContent();
}
