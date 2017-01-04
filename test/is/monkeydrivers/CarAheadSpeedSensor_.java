package is.monkeydrivers;

import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class CarAheadSpeedSensor_ {

    private CarAheadSpeedSensor carAheadSpeedSensor;
    private Bus bus;

    @Before
    public void setUp() throws Exception {
        bus = mock(Bus.class);
        this.carAheadSpeedSensor = new CarAheadSpeedSensor(bus);
    }


    @Test
    public void should_publish_CarAheadSpeed_message() throws Exception {
        Message message = createMessageOfType("CarAheadSpeed").withContent(null);
        carAheadSpeedSensor.publish(message);
        verify(bus, times(1)).send(message);
    }

    @Test
    public void should_not_publish_a_message_if_type_is_not_CarAheadSpeed() throws Exception {
        Message message = createMessageOfType("foo").withContent(null);
        carAheadSpeedSensor.publish(message);
        verify(bus, times(0)).send(message);
    }

    @Test
    public void should_store_own_speed_received() throws Exception {
        double speed = 50d;
        carAheadSpeedSensor.receive(createMessageOfType("ownSpeed").withContent(speed));
        assertThat(carAheadSpeedSensor.getOwnSpeed(), is(speed));
    }

    @Test
    public void should_store_car_ahead_distance_received() throws Exception {
        double metersToCarAhead = 48.2d;
        Instant instant = Instant.now();
        String plate = "1234HPDS";
        carAheadSpeedSensor.receive(createMessageOfType("carAheadDistance")
                                    .withContent(createCarAheadDistance(metersToCarAhead, instant, plate)));
        assertThat(carAheadSpeedSensor.getCarAheadDistance().getPlate(), is(plate));
        assertThat(carAheadSpeedSensor.getCarAheadDistance().getMetersToCar(), is(metersToCarAhead));
        assertThat(carAheadSpeedSensor.getCarAheadDistance().getTime(), is(instant));
    }

    @Test
    public void should_publish_its_own_speed_if_car_ahead_distance_is_constant() throws Exception {
        double speed = 50d;
        double carAheadDistance = 25d;
        carAheadSpeedSensor.receive(createMessageOfType("ownSpeed").withContent(speed));
        carAheadSpeedSensor.receive(createMessageOfType("carAheadDistance").withContent(
                createCarAheadDistance(carAheadDistance, Instant.now(), "1234GS1"))
        );
        Thread.sleep(1000);
        carAheadSpeedSensor.receive(createMessageOfType("carAheadDistance").withContent(
                createCarAheadDistance(carAheadDistance, Instant.now(), "1234GS1"))
        );
        assertThat(carAheadSpeedSensor.createMessage().getContent(), is(speed));
    }
    
    @Test
    public void should_publish_a_higher_speed_than_mine_if_distance_increments() throws Exception {
        final double initialDistance = 5d, distanceIncrement = 20d;
        carAheadSpeedSensor.receive(createMessageOfType("ownSpeed").withContent(50d));
        carAheadSpeedSensor.receive(createMessageOfType("carAheadDistance").withContent(
                createCarAheadDistance(initialDistance, Instant.now(), "1234GS1"))
        );
        Thread.sleep(1000);
        carAheadSpeedSensor.receive(createMessageOfType("carAheadDistance").withContent(
                createCarAheadDistance(initialDistance + distanceIncrement, Instant.now(), "1234GS1"))
        );
        verify(bus, times(1)).send(any(Message.class));
        assertThat(carAheadSpeedSensor.createMessage().getContent(), is(70d));
    }

    @Test
    public void should_publish_a_lower_speed_than_mine_if_distance_decrements() throws Exception {
        final double initialDistance = 20d, distanceDecrement = 5d;
        carAheadSpeedSensor.receive(createMessageOfType("ownSpeed").withContent(50d));
        carAheadSpeedSensor.receive(createMessageOfType("carAheadDistance").withContent(
                createCarAheadDistance(initialDistance, Instant.now(), "1234GS1"))
        );
        Thread.sleep(1000);
        carAheadSpeedSensor.receive(createMessageOfType("carAheadDistance").withContent(
                createCarAheadDistance(initialDistance - distanceDecrement, Instant.now(), "1234GS1"))
        );
        verify(bus, times(1)).send(any(Message.class));
        assertThat(carAheadSpeedSensor.createMessage().getContent(), is(45d));
    }

    @Test
    public void should_not_publish_speed_if_plate_change() throws Exception {
        carAheadSpeedSensor.receive(createMessageOfType("ownSpeed").withContent(50d));
        carAheadSpeedSensor.receive(createMessageOfType("carAheadDistance").withContent(
                createCarAheadDistance(5d, Instant.now(), "1234GS1"))
        );
        Thread.sleep(1000);
        carAheadSpeedSensor.receive(createMessageOfType("carAheadDistance").withContent(
                createCarAheadDistance(25d, Instant.now(), "1234HPDS"))
        );
        verify(bus, times(0)).send(any(Message.class));
    }



    private MessageFiller createMessageOfType(String type) {
        Message message = mock(Message.class);
        doReturn(type).when(message).type();
        return content -> {
            doReturn(content).when(message).getContent();
            return message;
        };
    }

    @FunctionalInterface
    private interface MessageFiller{
        Message withContent(Object content);
    }

    private CarAheadDistance createCarAheadDistance(final double metersToCarAhead, final Instant instant, final String plate) {
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
                public Instant getTime() {
                    return instant;
                }
            };
    }


}
