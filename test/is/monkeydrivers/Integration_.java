package is.monkeydrivers;

import is.monkeydrivers.actuators.SpeedActuator;
import is.monkeydrivers.mechanics.Pedal;
import is.monkeydrivers.message.Message;
import is.monkeydrivers.sensors.CarAheadSpeedSensor;
import is.monkeydrivers.sensors.VirtualSensor;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class Integration_ {

    private Bus bus;
    private SpeedActuator actuator;
    private VirtualSensor virtualSensor;
    private double ownSpeed;

    @Before
    public void setUp() throws Exception {
        bus = new MapBus();
        virtualSensor = new CarAheadSpeedSensor(bus);
        actuator = new SpeedActuator(createBrakePedal(), createGasPedal());
        subscribeSensors();
        subscribeActuators();
    }

    private Pedal createBrakePedal() {
        return () -> {
            ownSpeed = ownSpeed - 0.25;
            bus.send(createMessageOfType("ownSpeed").withContent(ownSpeed));
        };
    }

    private Pedal createGasPedal() {
        return () -> {
            ownSpeed = ownSpeed + 0.25;
            bus.send(createMessageOfType("ownSpeed").withContent(ownSpeed));
        };
    }

    private void subscribeSensors() {
        bus.subscribe(virtualSensor).to("ownSpeed");
        bus.subscribe(virtualSensor).to("carAheadDistance");
    }

    private void subscribeActuators() {
        bus.subscribe(actuator).to("ownSpeed");
        bus.subscribe(actuator).to("CarAheadSpeed");
        bus.subscribe(actuator).to("roadMaximumSpeed");
        bus.subscribe(actuator).to("roadMinimumSpeed");
        bus.subscribe(actuator).to("trafficLight");
    }

    private MessageFiller createMessageOfType(String type) {
        Message message = mock(Message.class);
        doReturn(type).when(message).type();
        return content -> {
            doReturn(content).when(message).getContent();
            return message;
        };
    }

    private CarAheadDistance createCarAheadDistance(final double metersToCarAhead, Instant now, final String plate) {
        return new CarAheadDistance() {
            @Override
            public String getPlate() {
                return plate;
            }

            @Override
            public double getMetersToCar() {
                return metersToCarAhead;
            }

            @Override
            public long getTimeInMilliseconds() {
                return now.toEpochMilli();
            }
        };
    }

    @Test
    public void acceptation() throws Exception {
        ownSpeed = 8;
        bus.send(createMessageOfType("ownSpeed").withContent(ownSpeed));
        assertThat(ownSpeed, is(8d));
        bus.send(foundTrafficLightOfColour("amber"));
        assertThat(ownSpeed, is(0d));
        bus.send(foundTrafficLightOfColour("green"));
        assertThat(ownSpeed, is(8d));
        bus.send(maximumSpeedOfRoadChangeTo(22d));
        assertThat(ownSpeed, is(22d));
        bus.send(foundCarAhead(createCarAheadDistance(100, Instant.now(), "123-A")));
        bus.send(foundCarAhead(createCarAheadDistance(98, Instant.now(), "123-A")));
        assertThat(ownSpeed, is(20d));
        bus.send(foundCarAhead(createCarAheadDistance(130, Instant.now(), "123-A")));
        assertThat(ownSpeed, is(22d));
        bus.send(carAheadOutOfRange(createCarAheadDistance(150, Instant.now(), "--")));
        assertThat(ownSpeed, is(22d));
        bus.send(maximumSpeedOfRoadChangeTo(18d));
        assertThat(ownSpeed, is(18d));
        bus.send(foundTrafficLightOfColour("red"));
        assertThat(ownSpeed, is(0d));
    }

    private Message carAheadOutOfRange(CarAheadDistance carAheadDistance) throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
        return createMessageOfType("carAheadDistance").withContent(carAheadDistance);
    }

    private Message foundCarAhead(CarAheadDistance carAheadDistance) throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
        return createMessageOfType("carAheadDistance").withContent(carAheadDistance);
    }

    private Message maximumSpeedOfRoadChangeTo(double speed) {
        return createMessageOfType("roadMaximumSpeed").withContent(speed);
    }

    private Message foundTrafficLightOfColour(String colour) {
        return createMessageOfType("trafficLight").withContent(colour);
    }
}
