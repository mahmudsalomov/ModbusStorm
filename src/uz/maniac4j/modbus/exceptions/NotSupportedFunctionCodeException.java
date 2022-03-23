package uz.maniac4j.modbus.exceptions;

public class NotSupportedFunctionCodeException extends ModbusStormException {
    public NotSupportedFunctionCodeException() {
    }

    public NotSupportedFunctionCodeException(String s) {
        super(s);
    }
}
