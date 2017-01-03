package is.monkeydrivers;

/**
 * Created by Mictlan on 03/01/2017.
 */
public interface CarAheadSpeedSensor extends VirtualSensor{


    double getOwnSpeed();

    String getCarAheadPlate();

    double getCarAheadDistance();
}
