package is.monkeydrivers.actuators;

import is.monkeydrivers.Actuator;
import is.monkeydrivers.message.Message;
import is.monkeydrivers.message.MessageProcessor;
import is.monkeydrivers.mechanics.Pedal;

import java.util.HashMap;
import java.util.Map;

public class SpeedActuator implements Actuator {
    private Map<String, MessageProcessor> processors;
    private double ownSpeed;
    private double carAheadSpeed;
    private Pedal brakePedal;
    private Pedal gasPedal;
    private double roadMaximumSpeed;
    private double roadMinimumSpeed;
    private String currentTrafficLight;

    public SpeedActuator(Pedal brakePedal, Pedal gasPedal) {
        this.brakePedal = brakePedal;
        this.gasPedal = gasPedal;
        ownSpeed = 0;
        carAheadSpeed = Double.MAX_VALUE;
        roadMaximumSpeed = Double.MAX_VALUE;
        roadMinimumSpeed = 8;
        currentTrafficLight = "green";
        initMessageProcessors();
    }

    private void initMessageProcessors() {
        processors = new HashMap<>();
        processors.put("ownSpeed", this::processOwnSpeed);
        processors.put("CarAheadSpeed", this::processCarAheadSpeed);
        processors.put("roadMaximumSpeed", this::processRoadMaximumSpeed);
        processors.put("roadMinimumSpeed", this::processRoadMinimumSpeed);
        processors.put("trafficLight", this::processTrafficLight);
    }

    private void processTrafficLight(Message message) {
        currentTrafficLight = (String) message.getContent();
    }

    @Override
    public void receive(Message message) {
        processors.get(message.type()).processMessage(message);
        adjustSpeed();
    }

    private void processRoadMaximumSpeed(Message message) {
        roadMaximumSpeed = (double) message.getContent();
    }

    private void processRoadMinimumSpeed(Message message) {
        roadMinimumSpeed = (double) message.getContent();
    }

    private void processCarAheadSpeed(Message message) {
        carAheadSpeed = (double) message.getContent();
    }

    private void processOwnSpeed(Message message) {
        ownSpeed = (double) message.getContent();
    }

    private void adjustSpeed() {
        if (shouldPressBrake()) brakePedal.press();
        else if (shouldPressGas()) gasPedal.press();
    }

    private boolean shouldPressBrake() {
        return thereIsACarAhead() && ownSpeed > carAheadSpeed ||
                thereIsTrafficLight() && ownSpeed != 0 ||
                ownSpeed > roadMaximumSpeed;
    }

    private boolean shouldPressGas() {
        return !thereIsTrafficLight() && ownSpeed == 0 ||
                thereIsACarAhead() && ownSpeed < carAheadSpeed ||
                !thereIsTrafficLight() && !thereIsACarAhead() && ownSpeed < roadMinimumSpeed;
    }

    private boolean thereIsTrafficLight() {
        return !currentTrafficLight.equals("green");
    }

    private boolean thereIsACarAhead() {
        return carAheadSpeed != Double.MAX_VALUE;
    }

}
