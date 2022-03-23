package uz.maniac4j.modbus.server;




import java.util.Calendar;

public class ModbusProtocol {
    public Calendar timeStamp;
    public boolean request;
    public boolean response;
    public int transactionIdentifier;
    public int protocolIdentifier;
    public int length;
    public byte unitIdentifier;
    public byte functionCode;
    public int startingAddress;
    public int quantity;
    public short byteCount;
    public byte exceptionCode;
    public byte errorCode;
    public short[] receiveCoilValues;
    public int[] receiveRegisterValues;
    public int[] sendRegisterValues;
    public boolean[] sendCoilValues;

    public ModbusProtocol() {
    }
}

