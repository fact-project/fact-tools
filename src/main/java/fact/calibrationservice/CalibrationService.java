package fact.calibrationservice;

import fact.container.PixelSet;
import stream.service.Service;

import java.time.ZonedDateTime;

/**
 * A CalibrationService provides informations about the hardware status of the camera for the current processed event.
 * At the moment it provides the informations which pixels are bad (and have to be interpolated) and which pixels
 * are not usable (cannot be interpolated, aren't used during cleaning).
 * Therefore a CalibrationService needs to implement getBadPixels() and getNotUsablePixels().
 * At the moment there is only the ConstantCalibService implemented which has hardcoded Hardwareconfigurations
 * providing the needed informations.
 * In the future there might be the possibility to implement another CalibrationService accessing a database with the
 * Hardwareconfigurations stored.
 *
 * @author ftemme
 */
public interface CalibrationService extends Service {

    /**
     * Returns an array, listing the chids of the bad pixels for the given event time stamp
     *
     * @param eventTimeStamp
     * @return array of chids
     */
    PixelSet getBadPixels(ZonedDateTime eventTimeStamp);

    /**
     * Returns an array, listing the chids of the pixels not usable for cleaning for the given event time stamp
     *
     * @param eventTimeStamp
     * @return array of chids, or null if doesn't exist
     */
    PixelSet getNotUsablePixels(ZonedDateTime eventTimeStamp);

}
