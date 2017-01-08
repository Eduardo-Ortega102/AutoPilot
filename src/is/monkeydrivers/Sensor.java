package is.monkeydrivers;


import is.monkeydrivers.message.Message;

public interface Sensor extends Publisher {

    Message createMessage();

}
