package is.monkeydrivers.message;

import java.time.Instant;

public interface CarAheadDistance {

    String getPlate();

    double getMetersToCar();

    Instant getTime();

}
