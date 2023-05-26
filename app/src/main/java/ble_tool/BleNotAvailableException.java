package ble_tool;


/**
 * Indicates that Bluetooth Low Energy is not available on this device
 * @see ScanManager#checkAvailability()
 * @author David G. Young
 *
 */
public class BleNotAvailableException extends RuntimeException {

    private static final long serialVersionUID = 2242747823097637729L;

    public BleNotAvailableException(String message) {
        super(message);
    }

}

