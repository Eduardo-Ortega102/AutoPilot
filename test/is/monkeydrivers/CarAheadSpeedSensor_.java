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
            private double carAheadSpeed = Double.MAX_VALUE;
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
        double speed = 50d;
        carAheadSpeedSensor.receive(createMessageOfType("ownSpeed").withContent(speed));
        assertThat(carAheadSpeedSensor.getOwnSpeed(), is(speed));
    }

    @Test
    public void ahead_car_plate_received_should_be_equal_to_ahead_car_plate_set() throws Exception {
        String plate = "1234HPDS";
        carAheadSpeedSensor.receive(createMessageOfType("carAheadPlate").withContent(plate));
        assertThat(carAheadSpeedSensor.getCarAheadPlate(), is(plate));
    }

    @Test
    public void car_ahead_plate_must_update() throws Exception {
        String plate1 = "1234HPDS", plate2 = "1234GS1";
        carAheadSpeedSensor.receive(createMessageOfType("carAheadPlate").withContent(plate1));
        carAheadSpeedSensor.receive(createMessageOfType("carAheadPlate").withContent(plate2));
        assertThat(carAheadSpeedSensor.getCarAheadPlate(), not(plate1));
        assertThat(carAheadSpeedSensor.getCarAheadPlate(), is(plate2));
    }

    @Test
    public void car_ahead_distance_received_should_be_equal_to_car_ahead_distance_set() throws Exception {
        double distance = 48.2d;
        carAheadSpeedSensor.receive(createMessageOfType("carAheadDistance").withContent(distance));
        assertThat(carAheadSpeedSensor.getCarAheadDistance(), is(distance));
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











}
