package is.monkeydrivers;

import is.monkeydrivers.actuators.Actuator;
import is.monkeydrivers.actuators.SpeedActuator;
import is.monkeydrivers.mechanics.Pedal;
import is.monkeydrivers.message.Message;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SpeedActuator_ {

    private Actuator actuator;
    private double ownSpeed;

    @Before
    public void setUp() throws Exception {
        actuator = new SpeedActuator(createBrakePedal(), createGasPedal());
    }

    private Pedal createBrakePedal() {
        Pedal brakePedal = mock(Pedal.class);
        doAnswer(invocationOnMock -> {
            ownSpeed = ownSpeed - 0.25;
            actuator.receive(createMessageOfType("ownSpeed").withContent(ownSpeed));
            return null;
        }).when(brakePedal).press();
        return brakePedal;
    }

    private Pedal createGasPedal() {
        Pedal gasPedal = mock(Pedal.class);
        doAnswer(invocationOnMock -> {
            ownSpeed = ownSpeed + 0.25;
            actuator.receive(createMessageOfType("ownSpeed").withContent(ownSpeed));
            return null;
        }).when(gasPedal).press();
        return gasPedal;
    }


    @Test
    public void should_not_do_speeding() throws Exception {
        ownSpeed = 80d;
        double roadMaxSpeed = 31;
        actuator.receive(createMessageOfType("ownSpeed").withContent(ownSpeed));
        actuator.receive(createMessageOfType("roadMaximumSpeed").withContent(roadMaxSpeed));
        Field ownSpeed = SpeedActuator.class.getDeclaredField("ownSpeed");
        ownSpeed.setAccessible(true);
        assertThat(ownSpeed.get(actuator), is(roadMaxSpeed));
        ownSpeed.setAccessible(false);
    }

    @Test
    public void should_not_do_slow_driving_on_motorways() throws Exception {
        ownSpeed = 3;
        double motorwaysMinSpeed = 17, motorwaysMaxSpeed = 22;
        actuator.receive(createMessageOfType("roadMaximumSpeed").withContent(motorwaysMaxSpeed));
        actuator.receive(createMessageOfType("ownSpeed").withContent(ownSpeed));
        actuator.receive(createMessageOfType("roadMinimumSpeed").withContent(motorwaysMinSpeed));
        Field ownSpeed = SpeedActuator.class.getDeclaredField("ownSpeed");
        ownSpeed.setAccessible(true);
        assertThat(ownSpeed.get(actuator), not(motorwaysMinSpeed));
        ownSpeed.setAccessible(false);
    }

    @Test
    public void should_stop_when_arrives_to_red_traffic_light() throws Exception {
        ownSpeed = 28d;
        actuator.receive(createMessageOfType("ownSpeed").withContent(ownSpeed));
        actuator.receive(createMessageOfType("trafficLight").withContent("red"));
        Field ownSpeed = SpeedActuator.class.getDeclaredField("ownSpeed");
        ownSpeed.setAccessible(true);
        assertThat(ownSpeed.get(actuator), is(0d));
        ownSpeed.setAccessible(false);
    }

    @Test
    public void should_stop_when_arrives_to_amber_traffic_light() throws Exception {
        ownSpeed = 28d;
        actuator.receive(createMessageOfType("ownSpeed").withContent(ownSpeed));
        actuator.receive(createMessageOfType("trafficLight").withContent("amber"));
        Field ownSpeed = SpeedActuator.class.getDeclaredField("ownSpeed");
        ownSpeed.setAccessible(true);
        assertThat(ownSpeed.get(actuator), is(0d));
        ownSpeed.setAccessible(false);
    }

    @Test
    public void should_start_when_traffic_light_change_to_green() throws Exception {
        ownSpeed = 0d;
        double roadMaxSpeed = 10;
        actuator.receive(createMessageOfType("ownSpeed").withContent(ownSpeed));
        actuator.receive(createMessageOfType("roadMaximumSpeed").withContent(roadMaxSpeed));
        actuator.receive(createMessageOfType("trafficLight").withContent("green"));
        Field ownSpeed = SpeedActuator.class.getDeclaredField("ownSpeed");
        ownSpeed.setAccessible(true);
        assertThat(ownSpeed.get(actuator), is(roadMaxSpeed));
        ownSpeed.setAccessible(false);
    }

    @Test
    public void should_adjust_own_speed_if_car_ahead_speed_is_lower() throws Exception {
        ownSpeed = 28d;
        double carAheadSpeed = 18, roadMaxSpeed = 20;
        actuator.receive(createMessageOfType("roadMaximumSpeed").withContent(roadMaxSpeed));
        actuator.receive(createMessageOfType("ownSpeed").withContent(ownSpeed));
        actuator.receive(createMessageOfType("CarAheadSpeed").withContent(carAheadSpeed));
        Field ownSpeed = SpeedActuator.class.getDeclaredField("ownSpeed");
        ownSpeed.setAccessible(true);
        assertThat(ownSpeed.get(actuator), is(carAheadSpeed));
        ownSpeed.setAccessible(false);
    }

    @Test
    public void should_adjust_own_speed_if_car_ahead_speed_is_higher_without_speeding() throws Exception {
        ownSpeed = 28d;
        double carAheadSpeed = 40d, roadMaxSpeed = 35d;
        actuator.receive(createMessageOfType("ownSpeed").withContent(ownSpeed));
        actuator.receive(createMessageOfType("roadMaximumSpeed").withContent(roadMaxSpeed));
        actuator.receive(createMessageOfType("CarAheadSpeed").withContent(carAheadSpeed));
        Field ownSpeed = SpeedActuator.class.getDeclaredField("ownSpeed");
        ownSpeed.setAccessible(true);
        assertThat(ownSpeed.get(actuator), is(roadMaxSpeed));
        ownSpeed.setAccessible(false);
    }

    private MessageFiller createMessageOfType(String type) {
        Message message = mock(Message.class);
        doReturn(type).when(message).type();
        return content -> {
            doReturn(content).when(message).getContent();
            return message;
        };
    }


}
