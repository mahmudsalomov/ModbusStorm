package uz.maniac4j.modbus.exceptions;

public class ConnectionException extends ModbusStormException{
    public ConnectionException() {
    }

    public ConnectionException(String s) {
        super(s);
    }
}
