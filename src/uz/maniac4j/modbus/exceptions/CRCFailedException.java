package uz.maniac4j.modbus.exceptions;

public class CRCFailedException extends ModbusStormException{
    public CRCFailedException() {
    }

    public CRCFailedException(String s) {
        super(s);
    }
}
