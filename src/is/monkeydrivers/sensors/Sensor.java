package is.monkeydrivers.sensors;


import is.monkeydrivers.Publisher;
import is.monkeydrivers.message.Message;

public interface Sensor extends Publisher {

    Message createMessage();

}
