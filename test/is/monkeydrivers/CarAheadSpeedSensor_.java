package is.monkeydrivers;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class CarAheadSpeedSensor_ {

    private CarAheadSpeedSensor carAheadSpeedSensor;

    @Before
    public void setUp() throws Exception {
        this.carAheadSpeedSensor = new CarAheadSpeedSensor() {
            private double carAheadDistance = Double.MAX_VALUE;
            private String carAheadPlate;
            private double ownSpeed;
            private Integer carAheadSpeed;
            private Bus bus;
            private final String publicationType = "CarAheadSpeed";

            @Override
            public double getOwnSpeed() {
                return ownSpeed;
            }

            @Override
            public String getCarAheadPlate() {
                return carAheadPlate;
            }

            @Override
            public double getCarAheadDistance() {
                return carAheadDistance;
            }

            @Override
            public void setPublicationBus(Bus bus) {
                this.bus = bus;
            }

            @Override
            public void publish(Message message) {
                if (publicationType.equals(message.type())) bus.send(message);
            }

            @Override
            public void receive(Message message) {
                if (message.type().equals("ownSpeed")) ownSpeed = (double) message.getContent();
                if (message.type().equals("carAheadPlate")) carAheadPlate = (String) message.getContent();
                if (message.type().equals("carAheadDistance")) carAheadDistance = (double) message.getContent();
                /*
                *
                * velocidad que publica: (distanciaFinal - distanciaInicial) / (tiempoFinal - tiempoInicial)
                *
                * */
            }

            @Override
            public Message createMessage() {
                return new Message<Integer>() {
                    @Override
                    public String type() {
                        return publicationType;
                    }

                    @Override
                    public Integer getContent() {
                        return carAheadSpeed;
                    }
                };
            }
        };
    }

    @Test
    public void should_publish_CarAheadSpeed_message() throws Exception {
        Bus bus = mock(Bus.class);
        Message message = createMessageOfType("CarAheadSpeed").withContent(null);
        carAheadSpeedSensor.setPublicationBus(bus);
        carAheadSpeedSensor.publish(message);
        verify(bus, times(1)).send(message);
    }

    @Test
    public void should_not_publish_a_message_if_type_is_not_CarAheadSpeed() throws Exception {
        Bus bus = mock(Bus.class);
        Message message = createMessageOfType("foo").withContent(null);
        carAheadSpeedSensor.setPublicationBus(bus);
        carAheadSpeedSensor.publish(message);
        verify(bus, times(0)).send(message);
    }

    @Test
    public void car_speed_received_should_be_equal_to_own_speed_set() throws Exception {
        Message message = createMessageOfType("ownSpeed").withContent(50d);
        carAheadSpeedSensor.receive(message);
        assertThat(carAheadSpeedSensor.getOwnSpeed(), is(message.getContent()));
    }

    @Test
    public void ahead_car_plate_received_should_be_equal_to_ahead_car_plate_set() throws Exception {
        Message message = createMessageOfType("carAheadPlate").withContent("1234HPDS");
        carAheadSpeedSensor.receive(message);
        assertThat(carAheadSpeedSensor.getCarAheadPlate(), is(message.getContent()));
    }

    @Test
    public void car_ahead_plate_must_update() throws Exception {
        String old_plate = "1234HPDS", new_plate = "1234GS1";
        carAheadSpeedSensor.receive(createMessageOfType("carAheadPlate").withContent(old_plate));
        carAheadSpeedSensor.receive(createMessageOfType("carAheadPlate").withContent(new_plate));
        assertThat(carAheadSpeedSensor.getCarAheadPlate(), not(old_plate));
        assertThat(carAheadSpeedSensor.getCarAheadPlate(), is(new_plate));
    }

    @Test
    public void car_ahead_distance_received_should_be_equal_to_car_ahead_distance_set() throws Exception {
        Message message = createMessageOfType("carAheadDistance").withContent(48.2d);
        carAheadSpeedSensor.receive(message);
        assertThat(carAheadSpeedSensor.getCarAheadDistance(), is(message.getContent()));
    }

    private MessageFiller createMessageOfType(String type) {
        Message message = mock(Message.class);
        doReturn(type).when(message).type();
        return content -> {
            doReturn(content).when(message).getContent();
            return message;
        };
    }

    private interface MessageFiller{
        Message withContent(Object content);
    }











}
