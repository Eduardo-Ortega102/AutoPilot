package is.monkeydrivers.sensors;

import is.monkeydrivers.*;
import is.monkeydrivers.CarAheadDistance;
import is.monkeydrivers.message.Message;
import is.monkeydrivers.message.MessageProcessor;

import java.util.HashMap;
import java.util.Map;

public class CarAheadSpeedSensor implements VirtualSensor {
    private final String publicationType;
    private double carAheadSpeed;
    private double ownSpeed;
    private Bus bus;
    private CarAheadDistance carAheadDistance;
    private Map<String, MessageProcessor> processors;

    public CarAheadSpeedSensor(Bus bus) {
        publicationType = "CarAheadSpeed";
        carAheadDistance = null;
        this.bus = bus;
        ownSpeed = 0;
        carAheadSpeed = Double.MAX_VALUE;
        processors = new HashMap<>();
        processors.put("ownSpeed", this::processOwnSpeed);
        processors.put("carAheadDistance", this::processCarAheadDistance);
    }

    @Override
    public void publish(Message message) {
        if (publicationType.equals(message.type())) bus.send(message);
    }

    @Override
    public void receive(Message message) {
        processors.get(message.type()).processMessage(message);
    }

    private void processCarAheadDistance(Message message) {
        CarAheadDistance newDistance = (CarAheadDistance) message.getContent();
        if (carAheadDistance != null && carAheadDistance.getPlate().equals(newDistance.getPlate())) {
            calculateCarAheadSpeed(newDistance);
            publish(createMessage());
        } else if (newDistance.getPlate().equals("--")) {
            carAheadSpeed = Double.MAX_VALUE;
            publish(createMessage());
        }
        carAheadDistance = newDistance;
    }

    private void processOwnSpeed(Message message) {
        ownSpeed = (double) message.getContent();
    }

    private void calculateCarAheadSpeed(CarAheadDistance newDistance) {
        double finalDistance = newDistance.getMetersToCar();
        double initialDistance = carAheadDistance.getMetersToCar();
        long finalInstant = newDistance.getTimeInMilliseconds();
        long initialInstant = carAheadDistance.getTimeInMilliseconds();
        carAheadSpeed = ownSpeed + (finalDistance - initialDistance) / ((finalInstant - initialInstant)/ 1000);
    }

    @Override
    public Message createMessage() {
        return new Message<Double>() {
            @Override
            public String type() {
                return publicationType;
            }

            @Override
            public Double getContent() {
                return carAheadSpeed;
            }
        };
    }
}
