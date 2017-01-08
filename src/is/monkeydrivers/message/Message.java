package is.monkeydrivers.message;

public interface Message<Content> {
    String type();
    Content getContent();
}
