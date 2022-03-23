package uz.maniac4j.modbus.exceptions;

public class InvalidStartAddressException extends ModbusStormException{
    public InvalidStartAddressException() {
    }

    public InvalidStartAddressException(String s) {
        super(s);
    }
}
