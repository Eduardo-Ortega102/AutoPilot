package is.monkeydrivers;

public interface Publisher {

    void publish(Message message);

    void setPublicationBus(Bus bus);

}
