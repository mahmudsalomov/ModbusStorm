package uz.maniac4j.modbus.exceptions;

public class InvalidQuantityException extends ModbusStormException{
    public InvalidQuantityException() {
    }

    public InvalidQuantityException(String s) {
        super(s);
    }
}
