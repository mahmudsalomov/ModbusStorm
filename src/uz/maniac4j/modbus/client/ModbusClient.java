package uz.maniac4j.modbus.client;


import uz.maniac4j.io.*;
import uz.maniac4j.modbus.exceptions.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ModbusClient {
    private Socket tcpClientSocket = new Socket();
    protected String ipAddress = "190.201.100.100";
    protected int port = 502;
    private byte[] transactionIdentifier = new byte[2];
    private byte[] protocolIdentifier = new byte[2];
    private byte[] length = new byte[2];
    private byte[] crc = new byte[2];
    private byte unitIdentifier = 1;
    private byte functionCode;
    private byte[] startingAddress = new byte[2];
    private byte[] quantity = new byte[2];
    private boolean udpFlag = false;
    private boolean serialFlag = false;
    private int connectTimeout = 500;
    private InputStream inStream;
    private DataOutputStream outStream;
    public byte[] receiveData;
    public byte[] sendData;
    private final List<ReceiveDataChangedListener> receiveDataChangedListener = new ArrayList();
    private final List<SendDataChangedListener> sendDataChangedListener = new ArrayList();
    private SerialPort serialPort;
    OutputStream out;
    InputStream in;
    CommPortIdentifier portIdentifier;

    public ModbusClient(String ipAddress, int port) {
        System.out.println("Modbus Storm Client Library");
        System.out.println("Maniac4j");
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public ModbusClient() {
        System.out.println("Modbus Storm Client Library");
        System.out.println("Maniac4j");
    }

    public void Connect() throws UnknownHostException, IOException {
        if (!this.udpFlag) {
            this.tcpClientSocket = new Socket(this.ipAddress, this.port);
            this.tcpClientSocket.setSoTimeout(this.connectTimeout);
            this.outStream = new DataOutputStream(this.tcpClientSocket.getOutputStream());
            this.inStream = this.tcpClientSocket.getInputStream();
        }

    }

    public void Connect(String ipAddress, int port) throws UnknownHostException, IOException {
        this.ipAddress = ipAddress;
        this.port = port;
        this.tcpClientSocket = new Socket(ipAddress, port);
        this.tcpClientSocket.setSoTimeout(this.connectTimeout);
        this.outStream = new DataOutputStream(this.tcpClientSocket.getOutputStream());
        this.inStream = this.tcpClientSocket.getInputStream();
    }

    public void Connect(String comPort) throws Exception {
        this.portIdentifier = CommPortIdentifier.getPortIdentifier(comPort);
        if (this.portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        } else {
            int timeout = 2000;
            CommPort commPort = this.portIdentifier.open(this.getClass().getName(), timeout);
            if (commPort instanceof SerialPort) {
                this.serialPort = (SerialPort)commPort;
                this.serialPort.setSerialPortParams(9600, 8, 1, 2);
                this.serialPort.enableReceiveTimeout(1000);
                this.serialPort.disableReceiveThreshold();
                this.in = this.serialPort.getInputStream();
                this.out = this.serialPort.getOutputStream();
                this.serialFlag = true;
            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }

    }

    public static float ConvertRegistersToFloat(int[] registers) throws IllegalArgumentException {
        if (registers.length != 2) {
            throw new IllegalArgumentException("Input Array length invalid");
        } else {
            int highRegister = registers[1];
            int lowRegister = registers[0];
            byte[] highRegisterBytes = toByteArray(highRegister);
            byte[] lowRegisterBytes = toByteArray(lowRegister);
            byte[] floatBytes = new byte[]{highRegisterBytes[1], highRegisterBytes[0], lowRegisterBytes[1], lowRegisterBytes[0]};
            return ByteBuffer.wrap(floatBytes).getFloat();
        }
    }

    public static double ConvertRegistersToDoublePrecisionFloat(int[] registers) throws IllegalArgumentException {
        if (registers.length != 4) {
            throw new IllegalArgumentException("Input Array length invalid");
        } else {
            byte[] highRegisterBytes = toByteArray(registers[3]);
            byte[] highLowRegisterBytes = toByteArray(registers[2]);
            byte[] lowHighRegisterBytes = toByteArray(registers[1]);
            byte[] lowRegisterBytes = toByteArray(registers[0]);
            byte[] doubleBytes = new byte[]{highRegisterBytes[1], highRegisterBytes[0], highLowRegisterBytes[1], highLowRegisterBytes[0], lowHighRegisterBytes[1], lowHighRegisterBytes[0], lowRegisterBytes[1], lowRegisterBytes[0]};
            return ByteBuffer.wrap(doubleBytes).getDouble();
        }
    }

    public static double ConvertRegistersToDoublePrecisionFloat(int[] registers, RegisterOrder registerOrder) throws IllegalArgumentException {
        if (registers.length != 4) {
            throw new IllegalArgumentException("Input Array length invalid");
        } else {
            int[] swappedRegisters = new int[]{registers[0], registers[1], registers[2], registers[3]};
            if (registerOrder == RegisterOrder.HighLow) {
                swappedRegisters = new int[]{registers[3], registers[2], registers[1], registers[0]};
            }

            return ConvertRegistersToDoublePrecisionFloat(swappedRegisters);
        }
    }

    public static float ConvertRegistersToFloat(int[] registers, RegisterOrder registerOrder) throws IllegalArgumentException {
        int[] swappedRegisters = new int[]{registers[0], registers[1]};
        if (registerOrder == RegisterOrder.HighLow) {
            swappedRegisters = new int[]{registers[1], registers[0]};
        }

        return ConvertRegistersToFloat(swappedRegisters);
    }

    public static long ConvertRegistersToLong(int[] registers) throws IllegalArgumentException {
        if (registers.length != 4) {
            throw new IllegalArgumentException("Input Array length invalid");
        } else {
            byte[] highRegisterBytes = toByteArray(registers[3]);
            byte[] highLowRegisterBytes = toByteArray(registers[2]);
            byte[] lowHighRegisterBytes = toByteArray(registers[1]);
            byte[] lowRegisterBytes = toByteArray(registers[0]);
            byte[] longBytes = new byte[]{highRegisterBytes[1], highRegisterBytes[0], highLowRegisterBytes[1], highLowRegisterBytes[0], lowHighRegisterBytes[1], lowHighRegisterBytes[0], lowRegisterBytes[1], lowRegisterBytes[0]};
            return ByteBuffer.wrap(longBytes).getLong();
        }
    }

    public static long ConvertRegistersToLong(int[] registers, RegisterOrder registerOrder) throws IllegalArgumentException {
        if (registers.length != 4) {
            throw new IllegalArgumentException("Input Array length invalid");
        } else {
            int[] swappedRegisters = new int[]{registers[0], registers[1], registers[2], registers[3]};
            if (registerOrder == RegisterOrder.HighLow) {
                swappedRegisters = new int[]{registers[3], registers[2], registers[1], registers[0]};
            }

            return ConvertRegistersToLong(swappedRegisters);
        }
    }

    public static int ConvertRegistersToDouble(int[] registers) throws IllegalArgumentException {
        if (registers.length != 2) {
            throw new IllegalArgumentException("Input Array length invalid");
        } else {
            int highRegister = registers[1];
            int lowRegister = registers[0];
            byte[] highRegisterBytes = toByteArray(highRegister);
            byte[] lowRegisterBytes = toByteArray(lowRegister);
            byte[] doubleBytes = new byte[]{highRegisterBytes[1], highRegisterBytes[0], lowRegisterBytes[1], lowRegisterBytes[0]};
            return ByteBuffer.wrap(doubleBytes).getInt();
        }
    }

    public static int ConvertRegistersToDouble(int[] registers, RegisterOrder registerOrder) throws IllegalArgumentException {
        int[] swappedRegisters = new int[]{registers[0], registers[1]};
        if (registerOrder == RegisterOrder.HighLow) {
            swappedRegisters = new int[]{registers[1], registers[0]};
        }

        return ConvertRegistersToDouble(swappedRegisters);
    }

    public static int[] ConvertFloatToTwoRegisters(float floatValue) {
        byte[] floatBytes = toByteArray(floatValue);
        byte[] highRegisterBytes = new byte[]{0, 0, floatBytes[0], floatBytes[1]};
        byte[] lowRegisterBytes = new byte[]{0, 0, floatBytes[2], floatBytes[3]};
        return new int[]{ByteBuffer.wrap(lowRegisterBytes).getInt(), ByteBuffer.wrap(highRegisterBytes).getInt()};
    }

    public static int[] ConvertFloatToTwoRegisters(float floatValue, RegisterOrder registerOrder) {
        int[] registerValues = ConvertFloatToTwoRegisters(floatValue);
        int[] returnValue = registerValues;
        if (registerOrder == RegisterOrder.HighLow) {
            returnValue = new int[]{registerValues[1], registerValues[0]};
        }

        return returnValue;
    }

    public static int[] ConvertDoubleToTwoRegisters(int doubleValue) {
        byte[] doubleBytes = toByteArrayDouble(doubleValue);
        byte[] highRegisterBytes = new byte[]{0, 0, doubleBytes[0], doubleBytes[1]};
        byte[] lowRegisterBytes = new byte[]{0, 0, doubleBytes[2], doubleBytes[3]};
        return new int[]{ByteBuffer.wrap(lowRegisterBytes).getInt(), ByteBuffer.wrap(highRegisterBytes).getInt()};
    }

    public static int[] ConvertDoubleToTwoRegisters(int doubleValue, RegisterOrder registerOrder) {
        int[] registerValues = ConvertFloatToTwoRegisters((float)doubleValue);
        int[] returnValue = registerValues;
        if (registerOrder == RegisterOrder.HighLow) {
            returnValue = new int[]{registerValues[1], registerValues[0]};
        }

        return returnValue;
    }

    public static String ConvertRegistersToString(int[] registers, int offset, int stringLength) {
        byte[] result = new byte[stringLength];
        byte[] registerResult = new byte[2];

        for(int i = 0; i < stringLength / 2; ++i) {
            registerResult = toByteArray(registers[offset + i]);
            result[i * 2] = registerResult[0];
            result[i * 2 + 1] = registerResult[1];
        }

        return new String(result);
    }

    public static int[] ConvertStringToRegisters(String stringToConvert) {
        byte[] array = stringToConvert.getBytes();
        int[] returnarray = new int[stringToConvert.length() / 2 + stringToConvert.length() % 2];

        for(int i = 0; i < returnarray.length; ++i) {
            returnarray[i] = array[i * 2];
            if (i * 2 + 1 < array.length) {
                returnarray[i] |= array[i * 2 + 1] << 8;
            }
        }

        return returnarray;
    }

    public static byte[] calculateCRC(byte[] data, int numberOfBytes, int startByte) {
        byte[] auchCRCHi = new byte[]{0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64, 1, -64, -128, 65, 0, -63, -127, 64, 0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64, 0, -63, -127, 64, 1, -64, -128, 65, 0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64, 1, -64, -128, 65, 0, -63, -127, 64, 0, -63, -127, 64, 1, -64, -128, 65, 0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64, 0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64, 1, -64, -128, 65, 0, -63, -127, 64, 0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64, 0, -63, -127, 64, 1, -64, -128, 65, 0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64, 0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64, 1, -64, -128, 65, 0, -63, -127, 64, 0, -63, -127, 64, 1, -64, -128, 65, 0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64, 1, -64, -128, 65, 0, -63, -127, 64, 0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64, 0, -63, -127, 64, 1, -64, -128, 65, 0, -63, -127, 64, 1, -64, -128, 65, 1, -64, -128, 65, 0, -63, -127, 64};
        byte[] auchCRCLo = new byte[]{0, -64, -63, 1, -61, 3, 2, -62, -58, 6, 7, -57, 5, -59, -60, 4, -52, 12, 13, -51, 15, -49, -50, 14, 10, -54, -53, 11, -55, 9, 8, -56, -40, 24, 25, -39, 27, -37, -38, 26, 30, -34, -33, 31, -35, 29, 28, -36, 20, -44, -43, 21, -41, 23, 22, -42, -46, 18, 19, -45, 17, -47, -48, 16, -16, 48, 49, -15, 51, -13, -14, 50, 54, -10, -9, 55, -11, 53, 52, -12, 60, -4, -3, 61, -1, 63, 62, -2, -6, 58, 59, -5, 57, -7, -8, 56, 40, -24, -23, 41, -21, 43, 42, -22, -18, 46, 47, -17, 45, -19, -20, 44, -28, 36, 37, -27, 39, -25, -26, 38, 34, -30, -29, 35, -31, 33, 32, -32, -96, 96, 97, -95, 99, -93, -94, 98, 102, -90, -89, 103, -91, 101, 100, -92, 108, -84, -83, 109, -81, 111, 110, -82, -86, 106, 107, -85, 105, -87, -88, 104, 120, -72, -71, 121, -69, 123, 122, -70, -66, 126, 127, -65, 125, -67, -68, 124, -76, 116, 117, -75, 119, -73, -74, 118, 114, -78, -77, 115, -79, 113, 112, -80, 80, -112, -111, 81, -109, 83, 82, -110, -106, 86, 87, -105, 85, -107, -108, 84, -100, 92, 93, -99, 95, -97, -98, 94, 90, -102, -101, 91, -103, 89, 88, -104, -120, 72, 73, -119, 75, -117, -118, 74, 78, -114, -113, 79, -115, 77, 76, -116, 68, -124, -123, 69, -121, 71, 70, -122, -126, 66, 67, -125, 65, -127, -128, 64};
        short usDataLen = (short)numberOfBytes;
        byte uchCRCHi = -1;
        byte uchCRCLo = -1;

        for(int i = 0; usDataLen > 0; ++i) {
            --usDataLen;
            int uIndex = uchCRCLo ^ data[i + startByte];
            if (uIndex < 0) {
                uIndex += 256;
            }

            uchCRCLo = (byte)(uchCRCHi ^ auchCRCHi[uIndex]);
            uchCRCHi = auchCRCLo[uIndex];
        }

        return new byte[]{uchCRCLo, uchCRCHi};
    }

    public boolean[] ReadDiscreteInputs(int startingAddress, int quantity) throws ModbusStormException, UnknownHostException, SocketException, IOException {
        if (this.tcpClientSocket == null) {
            throw new ConnectionException("connection Error");
        } else if (startingAddress > 65535 | quantity > 2000) {
            throw new IllegalArgumentException("Starting address must be 0 - 65535; quantity must be 0 - 2000");
        } else {
            boolean[] response = null;
            this.transactionIdentifier = toByteArray(1);
            this.protocolIdentifier = toByteArray(0);
            this.length = toByteArray(6);
            this.functionCode = 2;
            this.startingAddress = toByteArray(startingAddress);
            this.quantity = toByteArray(quantity);
            byte[] data = new byte[]{this.transactionIdentifier[1], this.transactionIdentifier[0], this.protocolIdentifier[1], this.protocolIdentifier[0], this.length[1], this.length[0], this.unitIdentifier, this.functionCode, this.startingAddress[1], this.startingAddress[0], this.quantity[1], this.quantity[0], this.crc[0], this.crc[1]};
            if (this.serialFlag) {
                this.crc = calculateCRC(data, 6, 6);
                data[data.length - 2] = this.crc[0];
                data[data.length - 1] = this.crc[1];
            }

            byte[] serialdata = null;
            if (this.serialFlag) {
                this.out.write(data, 6, 8);
                long dateTimeSend = DateTime.getDateTimeTicks();
                byte receivedUnitIdentifier = -1;
                boolean len = true;
                byte[] serialBuffer = new byte[256];
                serialdata = new byte[256];
                int expectedlength = 5 + quantity / 8 + 1;
                if (quantity % 8 == 0) {
                    expectedlength = 5 + quantity / 8;
                }

                for(int currentLength = 0; receivedUnitIdentifier != this.unitIdentifier & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout); receivedUnitIdentifier = serialdata[0]) {
                    while(currentLength < expectedlength & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout)) {
                        len = true;

                        int len2;
                        while((len2 = this.in.read(serialBuffer)) <= 0 & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout)) {
                        }

                        for(int i = 0; i < len2; ++i) {
                            serialdata[currentLength] = serialBuffer[i];
                            ++currentLength;
                        }
                    }
                }

                if (receivedUnitIdentifier != this.unitIdentifier) {
                    serialdata = new byte[256];
                }
            }

            if (serialdata != null) {
                data = new byte[262];
                System.arraycopy(serialdata, 0, data, 6, serialdata.length);
            }

            int i;
            if (this.tcpClientSocket.isConnected() | this.udpFlag) {
                if (this.udpFlag) {
                    InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length - 2, ipAddress, this.port);
                    DatagramSocket clientSocket = new DatagramSocket();
                    clientSocket.setSoTimeout(500);
                    clientSocket.send(sendPacket);
                    data = new byte[2100];
                    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                    clientSocket.receive(receivePacket);
                    clientSocket.close();
                    data = receivePacket.getData();
                } else {
                    this.outStream.write(data, 0, data.length - 2);
                    if (this.sendDataChangedListener.size() > 0) {
                        this.sendData = new byte[data.length - 2];
                        System.arraycopy(data, 0, this.sendData, 0, data.length - 2);

                        for (SendDataChangedListener hl : this.sendDataChangedListener) {
                            hl.SendDataChanged();
                        }
                    }

                    data = new byte[2100];
                    i = this.inStream.read(data, 0, data.length);
                    if (this.receiveDataChangedListener.size() > 0) {
                        this.receiveData = new byte[i];
                        System.arraycopy(data, 0, this.receiveData, 0, i);

                        for (ReceiveDataChangedListener hl : this.receiveDataChangedListener) {
                            hl.ReceiveDataChanged();
                        }
                    }
                }
            }

            if ((data[7] & 255) == 130 & data[8] == 1) {
                throw new NotSupportedFunctionCodeException("Function code not supported by master");
            } else if ((data[7] & 255) == 130 & data[8] == 2) {
                throw new InvalidStartAddressException("Starting address invalid or starting address + quantity invalid");
            } else if ((data[7] & 255) == 130 & data[8] == 3) {
                throw new InvalidQuantityException("Quantity invalid");
            } else if ((data[7] & 255) == 130 & data[8] == 4) {
                throw new ModbusStormException("Error reading");
            } else {
                response = new boolean[quantity];

                for(i = 0; i < quantity; ++i) {
                    int intData = data[9 + i / 8];
                    int mask = (int)Math.pow(2.0D, (double)(i % 8));
                    intData = (intData & mask) / mask;
                    response[i] = intData > 0;
                }

                return response;
            }
        }
    }

    public boolean[] ReadCoils(int startingAddress, int quantity) throws ModbusStormException, UnknownHostException, SocketException, IOException {
        if (this.tcpClientSocket == null) {
            throw new ConnectionException("connection Error");
        } else if (startingAddress > 65535 | quantity > 2000) {
            throw new IllegalArgumentException("Starting address must be 0 - 65535; quantity must be 0 - 2000");
        } else {
            boolean[] response = new boolean[quantity];
            this.transactionIdentifier = toByteArray(1);
            this.protocolIdentifier = toByteArray(0);
            this.length = toByteArray(6);
            this.functionCode = 1;
            this.startingAddress = toByteArray(startingAddress);
            this.quantity = toByteArray(quantity);
            byte[] data = new byte[]{this.transactionIdentifier[1], this.transactionIdentifier[0], this.protocolIdentifier[1], this.protocolIdentifier[0], this.length[1], this.length[0], this.unitIdentifier, this.functionCode, this.startingAddress[1], this.startingAddress[0], this.quantity[1], this.quantity[0], this.crc[0], this.crc[1]};
            if (this.serialFlag) {
                this.crc = calculateCRC(data, 6, 6);
                data[data.length - 2] = this.crc[0];
                data[data.length - 1] = this.crc[1];
            }

            byte[] serialdata = null;
            if (this.serialFlag) {
                this.out.write(data, 6, 8);
                long dateTimeSend = DateTime.getDateTimeTicks();
                byte receivedUnitIdentifier = -1;
                boolean len = true;
                byte[] serialBuffer = new byte[256];
                serialdata = new byte[256];
                int expectedlength = 5 + quantity / 8 + 1;
                if (quantity % 8 == 0) {
                    expectedlength = 5 + quantity / 8;
                }

                for(int currentLength = 0; receivedUnitIdentifier != this.unitIdentifier & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout); receivedUnitIdentifier = serialdata[0]) {
                    while(currentLength < expectedlength & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout)) {
                        len = true;

                        int len2;
                        while((len2 = this.in.read(serialBuffer)) <= 0 & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout)) {
                        }

                        for(int i = 0; i < len2; ++i) {
                            serialdata[currentLength] = serialBuffer[i];
                            ++currentLength;
                        }
                    }
                }

                if (receivedUnitIdentifier != this.unitIdentifier) {
                    serialdata = new byte[256];
                }
            }

            if (serialdata != null) {
                data = new byte[262];
                System.arraycopy(serialdata, 0, data, 6, serialdata.length);
            }

            int i;
            if (this.tcpClientSocket.isConnected() | this.udpFlag) {
                if (this.udpFlag) {
                    InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
                    DatagramSocket clientSocket = new DatagramSocket();
                    clientSocket.setSoTimeout(500);
                    clientSocket.send(sendPacket);
                    data = new byte[2100];
                    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                    clientSocket.receive(receivePacket);
                    clientSocket.close();
                    data = receivePacket.getData();
                } else {
                    this.outStream.write(data, 0, data.length - 2);
                    if (this.sendDataChangedListener.size() > 0) {
                        this.sendData = new byte[data.length - 2];
                        System.arraycopy(data, 0, this.sendData, 0, data.length - 2);

                        for (SendDataChangedListener hl : this.sendDataChangedListener) {
                            hl.SendDataChanged();
                        }
                    }

                    data = new byte[2100];
                    i = this.inStream.read(data, 0, data.length);
                    if (this.receiveDataChangedListener.size() > 0) {
                        this.receiveData = new byte[i];
                        System.arraycopy(data, 0, this.receiveData, 0, i);

                        for (ReceiveDataChangedListener hl : this.receiveDataChangedListener) {
                            hl.ReceiveDataChanged();
                        }
                    }
                }
            }

            if ((data[7] & 255) == 129 & data[8] == 1) {
                throw new NotSupportedFunctionCodeException("Function code not supported by master");
            } else if ((data[7] & 255) == 129 & data[8] == 2) {
                throw new InvalidStartAddressException("Starting address invalid or starting address + quantity invalid");
            } else if ((data[7] & 255) == 129 & data[8] == 3) {
                throw new InvalidQuantityException("Quantity invalid");
            } else if ((data[7] & 255) == 129 & data[8] == 4) {
                throw new ModbusStormException("Error reading");
            } else {
                for(i = 0; i < quantity; ++i) {
                    int intData = data[9 + i / 8];
                    int mask = (int)Math.pow(2.0D, (double)(i % 8));
                    intData = (intData & mask) / mask;
                    response[i] = intData > 0;
                }

                return response;
            }
        }
    }

    public int[] ReadHoldingRegisters(int startingAddress, int quantity) throws ModbusStormException, UnknownHostException, SocketException, IOException {
        if (this.tcpClientSocket == null) {
            throw new ConnectionException("connection Error");
        } else if (startingAddress > 65535 | quantity > 125) {
            throw new IllegalArgumentException("Starting address must be 0 - 65535; quantity must be 0 - 125");
        } else {
            int[] response = new int[quantity];
            this.transactionIdentifier = toByteArray(1);
            this.protocolIdentifier = toByteArray(0);
            this.length = toByteArray(6);
            this.functionCode = 3;
            this.startingAddress = toByteArray(startingAddress);
            this.quantity = toByteArray(quantity);
            byte[] data = new byte[]{this.transactionIdentifier[1], this.transactionIdentifier[0], this.protocolIdentifier[1], this.protocolIdentifier[0], this.length[1], this.length[0], this.unitIdentifier, this.functionCode, this.startingAddress[1], this.startingAddress[0], this.quantity[1], this.quantity[0], this.crc[0], this.crc[1]};
            if (this.serialFlag) {
                this.crc = calculateCRC(data, 6, 6);
                data[data.length - 2] = this.crc[0];
                data[data.length - 1] = this.crc[1];
            }

            byte[] serialdata = null;
            if (this.serialFlag) {
                this.out.write(data, 6, 8);
                long dateTimeSend = DateTime.getDateTimeTicks();
                byte receivedUnitIdentifier = -1;
                boolean len = true;
                byte[] serialBuffer = new byte[256];
                serialdata = new byte[256];
                int expectedlength = 5 + 2 * quantity;

                int i;
                for(int currentLength = 0; receivedUnitIdentifier != this.unitIdentifier & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout); receivedUnitIdentifier = serialdata[0]) {
                    while(currentLength < expectedlength & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout)) {
                        len = true;

                        int len2;
                        while((len2 = this.in.read(serialBuffer)) <= 0 & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout)) {
                        }

                        for(i = 0; i < len2; ++i) {
                            serialdata[currentLength] = serialBuffer[i];
                            ++currentLength;
                        }
                    }
                }

                if (receivedUnitIdentifier != this.unitIdentifier) {
                    data = new byte[256];
                }

                data = new byte[262];
                System.arraycopy(serialdata, 0, data, 6, serialdata.length);

                for(i = 0; i < quantity; ++i) {
                    byte[] bytes = new byte[]{data[3 + i * 2], data[3 + i * 2 + 1]};
                    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                    response[i] = byteBuffer.getShort();
                }
            }

            int i;
            if (this.tcpClientSocket.isConnected() | this.udpFlag) {
                if (this.udpFlag) {
                    InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
                    DatagramSocket clientSocket = new DatagramSocket();
                    clientSocket.setSoTimeout(500);
                    clientSocket.send(sendPacket);
                    data = new byte[2100];
                    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                    clientSocket.receive(receivePacket);
                    clientSocket.close();
                    data = receivePacket.getData();
                } else {
                    this.outStream.write(data, 0, data.length - 2);
                    if (this.sendDataChangedListener.size() > 0) {
                        this.sendData = new byte[data.length - 2];
                        System.arraycopy(data, 0, this.sendData, 0, data.length - 2);

                        for (SendDataChangedListener hl : this.sendDataChangedListener) {
                            hl.SendDataChanged();
                        }
                    }

                    data = new byte[2100];
                    i = this.inStream.read(data, 0, data.length);
                    if (this.receiveDataChangedListener.size() > 0) {
                        this.receiveData = new byte[i];
                        System.arraycopy(data, 0, this.receiveData, 0, i);

                        for (ReceiveDataChangedListener hl : this.receiveDataChangedListener) {
                            hl.ReceiveDataChanged();
                        }
                    }
                }
            }

            for(i = 0; i < quantity; ++i) {
                byte[] bytes = new byte[]{data[9 + i * 2], data[9 + i * 2 + 1]};
                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                response[i] = byteBuffer.getShort();
            }

            return response;
        }
    }

    public int[] ReadInputRegisters(int startingAddress, int quantity) throws ModbusStormException, UnknownHostException, SocketException, IOException {
        if (this.tcpClientSocket == null) {
            throw new ConnectionException("connection Error");
        } else if (startingAddress > 65535 | quantity > 125) {
            throw new IllegalArgumentException("Starting address must be 0 - 65535; quantity must be 0 - 125");
        } else {
            int[] response = new int[quantity];
            this.transactionIdentifier = toByteArray(1);
            this.protocolIdentifier = toByteArray(0);
            this.length = toByteArray(6);
            this.functionCode = 4;
            this.startingAddress = toByteArray(startingAddress);
            this.quantity = toByteArray(quantity);
            byte[] data = new byte[]{this.transactionIdentifier[1], this.transactionIdentifier[0], this.protocolIdentifier[1], this.protocolIdentifier[0], this.length[1], this.length[0], this.unitIdentifier, this.functionCode, this.startingAddress[1], this.startingAddress[0], this.quantity[1], this.quantity[0], this.crc[0], this.crc[1]};
            if (this.serialFlag) {
                this.crc = calculateCRC(data, 6, 6);
                data[data.length - 2] = this.crc[0];
                data[data.length - 1] = this.crc[1];
            }

            byte[] serialdata = null;
            if (this.serialFlag) {
                this.out.write(data, 6, 8);
                long dateTimeSend = DateTime.getDateTimeTicks();
                byte receivedUnitIdentifier = -1;
                boolean len = true;
                byte[] serialBuffer = new byte[256];
                serialdata = new byte[256];
                int expectedlength = 5 + 2 * quantity;

                int i;
                for(int currentLength = 0; receivedUnitIdentifier != this.unitIdentifier & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout); receivedUnitIdentifier = serialdata[0]) {
                    while(currentLength < expectedlength & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout)) {
                        len = true;

                        int len2;
                        while((len2 = this.in.read(serialBuffer)) <= 0 & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout)) {
                        }

                        for(i = 0; i < len2; ++i) {
                            serialdata[currentLength] = serialBuffer[i];
                            ++currentLength;
                        }
                    }
                }

                if (receivedUnitIdentifier != this.unitIdentifier) {
                    data = new byte[256];
                }

                data = new byte[262];
                System.arraycopy(serialdata, 0, data, 6, serialdata.length);

                for(i = 0; i < quantity; ++i) {
                    byte[] bytes = new byte[]{data[3 + i * 2], data[3 + i * 2 + 1]};
                    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                    response[i] = byteBuffer.getShort();
                }
            }

            int numberOfBytes;
            if (this.tcpClientSocket.isConnected() | this.udpFlag) {
                if (this.udpFlag) {
                    InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
                    DatagramSocket clientSocket = new DatagramSocket();
                    clientSocket.setSoTimeout(500);
                    clientSocket.send(sendPacket);
                    data = new byte[2100];
                    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                    clientSocket.receive(receivePacket);
                    clientSocket.close();
                    data = receivePacket.getData();
                } else {
                    this.outStream.write(data, 0, data.length - 2);
                    if (this.sendDataChangedListener.size() > 0) {
                        this.sendData = new byte[data.length - 2];
                        System.arraycopy(data, 0, this.sendData, 0, data.length - 2);

                        for (SendDataChangedListener hl : this.sendDataChangedListener) {
                            hl.SendDataChanged();
                        }
                    }

                    data = new byte[2100];
                    numberOfBytes = this.inStream.read(data, 0, data.length);
                    if (this.receiveDataChangedListener.size() > 0) {
                        this.receiveData = new byte[numberOfBytes];
                        System.arraycopy(data, 0, this.receiveData, 0, numberOfBytes);

                        for (ReceiveDataChangedListener hl : this.receiveDataChangedListener) {
                            hl.ReceiveDataChanged();
                        }
                    }
                }

                if ((data[7] & 255) == 132 & data[8] == 1) {
                    throw new NotSupportedFunctionCodeException("Function code not supported by master");
                }

                if ((data[7] & 255) == 132 & data[8] == 2) {
                    throw new InvalidStartAddressException("Starting address invalid or starting address + quantity invalid");
                }

                if ((data[7] & 255) == 132 & data[8] == 3) {
                    throw new InvalidQuantityException("Quantity invalid");
                }

                if ((data[7] & 255) == 132 & data[8] == 4) {
                    throw new ModbusStormException("Error reading");
                }
            }

            for(numberOfBytes = 0; numberOfBytes < quantity; ++numberOfBytes) {
                byte[] bytes = new byte[]{data[9 + numberOfBytes * 2], data[9 + numberOfBytes * 2 + 1]};
                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                response[numberOfBytes] = byteBuffer.getShort();
            }

            return response;
        }
    }

    public void WriteSingleCoil(int startingAddress, boolean value) throws ModbusStormException, UnknownHostException, SocketException, IOException {
        if (this.tcpClientSocket == null & !this.udpFlag) {
            throw new ConnectionException("connection error");
        } else {
            byte[] coilValue = new byte[2];
            this.transactionIdentifier = toByteArray(1);
            this.protocolIdentifier = toByteArray(0);
            this.length = toByteArray(6);
            this.functionCode = 5;
            this.startingAddress = toByteArray(startingAddress);
            if (value) {
                coilValue = toByteArray(65280);
            } else {
                coilValue = toByteArray(0);
            }

            byte[] data = new byte[]{this.transactionIdentifier[1], this.transactionIdentifier[0], this.protocolIdentifier[1], this.protocolIdentifier[0], this.length[1], this.length[0], this.unitIdentifier, this.functionCode, this.startingAddress[1], this.startingAddress[0], coilValue[1], coilValue[0], this.crc[0], this.crc[1]};
            if (this.serialFlag) {
                this.crc = calculateCRC(data, 6, 6);
                data[data.length - 2] = this.crc[0];
                data[data.length - 1] = this.crc[1];
            }

            byte[] serialdata = null;
            if (this.serialFlag) {
                this.out.write(data, 6, 8);
                long dateTimeSend = DateTime.getDateTimeTicks();
                byte receivedUnitIdentifier = -1;
                boolean len = true;
                byte[] serialBuffer = new byte[256];
                serialdata = new byte[256];
                int expectedlength = 8;

                for(int currentLength = 0; receivedUnitIdentifier != this.unitIdentifier & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout); receivedUnitIdentifier = serialdata[0]) {
                    while(currentLength < expectedlength & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout)) {
                        len = true;

                        int len2;
                        while((len2 = this.in.read(serialBuffer)) <= 0 & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout)) {
                        }

                        for(int i = 0; i < len2; ++i) {
                            serialdata[currentLength] = serialBuffer[i];
                            ++currentLength;
                        }
                    }
                }

                if (receivedUnitIdentifier != this.unitIdentifier) {
                    data = new byte[256];
                }
            }

            if (serialdata != null) {
                data = new byte[262];
                System.arraycopy(serialdata, 0, data, 6, serialdata.length);
            }

            assert this.tcpClientSocket != null;
            if (this.tcpClientSocket.isConnected() | this.udpFlag) {
                if (this.udpFlag) {
                    InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
                    DatagramSocket clientSocket = new DatagramSocket();
                    clientSocket.setSoTimeout(500);
                    clientSocket.send(sendPacket);
                    data = new byte[2100];
                    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                    clientSocket.receive(receivePacket);
                    clientSocket.close();
                    data = receivePacket.getData();
                } else {
                    this.outStream.write(data, 0, data.length - 2);
                    if (this.sendDataChangedListener.size() > 0) {
                        this.sendData = new byte[data.length - 2];
                        System.arraycopy(data, 0, this.sendData, 0, data.length - 2);

                        for (SendDataChangedListener hl : this.sendDataChangedListener) {
                            hl.SendDataChanged();
                        }
                    }

                    data = new byte[2100];
                    int numberOfBytes = this.inStream.read(data, 0, data.length);
                    if (this.receiveDataChangedListener.size() > 0) {
                        this.receiveData = new byte[numberOfBytes];
                        System.arraycopy(data, 0, this.receiveData, 0, numberOfBytes);

                        for (ReceiveDataChangedListener hl : this.receiveDataChangedListener) {
                            hl.ReceiveDataChanged();
                        }
                    }
                }
            }

            if ((data[7] & 255) == 133 & data[8] == 1) {
                throw new NotSupportedFunctionCodeException("Function code not supported by master");
            } else if ((data[7] & 255) == 133 & data[8] == 2) {
                throw new InvalidStartAddressException("Starting address invalid or starting address + quantity invalid");
            } else if ((data[7] & 255) == 133 & data[8] == 3) {
                throw new InvalidQuantityException("quantity invalid");
            } else if ((data[7] & 255) == 133 & data[8] == 4) {
                throw new ModbusStormException("error reading");
            }
        }
    }

    public void WriteSingleRegister(int startingAddress, int value) throws ModbusStormException, UnknownHostException, SocketException, IOException {
        if (this.tcpClientSocket == null & !this.udpFlag) {
            throw new ConnectionException("connection error");
        } else {
            byte[] registerValue = new byte[2];
            this.transactionIdentifier = toByteArray(1);
            this.protocolIdentifier = toByteArray(0);
            this.length = toByteArray(6);
            this.functionCode = 6;
            this.startingAddress = toByteArray(startingAddress);
            registerValue = toByteArray((short)value);
            byte[] data = new byte[]{this.transactionIdentifier[1], this.transactionIdentifier[0], this.protocolIdentifier[1], this.protocolIdentifier[0], this.length[1], this.length[0], this.unitIdentifier, this.functionCode, this.startingAddress[1], this.startingAddress[0], registerValue[1], registerValue[0], this.crc[0], this.crc[1]};
            if (this.serialFlag) {
                this.crc = calculateCRC(data, 6, 6);
                data[data.length - 2] = this.crc[0];
                data[data.length - 1] = this.crc[1];
            }

            byte[] serialdata = null;
            if (this.serialFlag) {
                this.out.write(data, 6, 8);
                long dateTimeSend = DateTime.getDateTimeTicks();
                byte receivedUnitIdentifier = -1;
                boolean len = true;
                byte[] serialBuffer = new byte[256];
                serialdata = new byte[256];
                int expectedlength = 8;

                for(int currentLength = 0; receivedUnitIdentifier != this.unitIdentifier & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout); receivedUnitIdentifier = serialdata[0]) {
                    while(currentLength < expectedlength & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout)) {
                        len = true;

                        int len2;
                        while((len2 = this.in.read(serialBuffer)) <= 0 & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout)) {
                        }

                        for(int i = 0; i < len2; ++i) {
                            serialdata[currentLength] = serialBuffer[i];
                            ++currentLength;
                        }
                    }
                }

                if (receivedUnitIdentifier != this.unitIdentifier) {
                    data = new byte[256];
                }
            }

            if (serialdata != null) {
                data = new byte[262];
                System.arraycopy(serialdata, 0, data, 6, serialdata.length);
            }

            assert this.tcpClientSocket != null;
            if (this.tcpClientSocket.isConnected() | this.udpFlag) {
                if (this.udpFlag) {
                    InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
                    DatagramSocket clientSocket = new DatagramSocket();
                    clientSocket.setSoTimeout(500);
                    clientSocket.send(sendPacket);
                    data = new byte[2100];
                    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                    clientSocket.receive(receivePacket);
                    clientSocket.close();
                    data = receivePacket.getData();
                } else {
                    this.outStream.write(data, 0, data.length - 2);
                    if (this.sendDataChangedListener.size() > 0) {
                        this.sendData = new byte[data.length - 2];
                        System.arraycopy(data, 0, this.sendData, 0, data.length - 2);

                        for (SendDataChangedListener hl : this.sendDataChangedListener) {
                            hl.SendDataChanged();
                        }
                    }

                    data = new byte[2100];
                    int numberOfBytes = this.inStream.read(data, 0, data.length);
                    if (this.receiveDataChangedListener.size() > 0) {
                        this.receiveData = new byte[numberOfBytes];
                        System.arraycopy(data, 0, this.receiveData, 0, numberOfBytes);

                        for (ReceiveDataChangedListener hl : this.receiveDataChangedListener) {
                            hl.ReceiveDataChanged();
                        }
                    }
                }
            }

            if ((data[7] & 255) == 134 & data[8] == 1) {
                throw new NotSupportedFunctionCodeException("Function code not supported by master");
            } else if ((data[7] & 255) == 134 & data[8] == 2) {
                throw new InvalidStartAddressException("Starting address invalid or starting address + quantity invalid");
            } else if ((data[7] & 255) == 134 & data[8] == 3) {
                throw new InvalidQuantityException("quantity invalid");
            } else if ((data[7] & 255) == 134 & data[8] == 4) {
                throw new ModbusStormException("error reading");
            }
        }
    }

    public void WriteMultipleCoils(int startingAddress, boolean[] values) throws ModbusStormException, UnknownHostException, SocketException, IOException {
        byte byteCount = (byte)(values.length / 8 + 1);
        if (values.length % 8 == 0) {
            --byteCount;
        }

        byte[] quantityOfOutputs = toByteArray(values.length);
        byte singleCoilValue = 0;
        if (this.tcpClientSocket == null & !this.udpFlag) {
            throw new ConnectionException("connection error");
        } else {
            this.transactionIdentifier = toByteArray(1);
            this.protocolIdentifier = toByteArray(0);
            this.length = toByteArray(7 + values.length / 8 + 1);
            this.functionCode = 15;
            this.startingAddress = toByteArray(startingAddress);
            byte[] data = new byte[16 + byteCount - 1];
            data[0] = this.transactionIdentifier[1];
            data[1] = this.transactionIdentifier[0];
            data[2] = this.protocolIdentifier[1];
            data[3] = this.protocolIdentifier[0];
            data[4] = this.length[1];
            data[5] = this.length[0];
            data[6] = this.unitIdentifier;
            data[7] = this.functionCode;
            data[8] = this.startingAddress[1];
            data[9] = this.startingAddress[0];
            data[10] = quantityOfOutputs[1];
            data[11] = quantityOfOutputs[0];
            data[12] = byteCount;

            for(int i = 0; i < values.length; ++i) {
                if (i % 8 == 0) {
                    singleCoilValue = 0;
                }

                byte CoilValue;
                if (values[i]) {
                    CoilValue = 1;
                } else {
                    CoilValue = 0;
                }

                singleCoilValue = (byte)(CoilValue << i % 8 | singleCoilValue);
                data[13 + i / 8] = singleCoilValue;
            }

            if (this.serialFlag) {
                this.crc = calculateCRC(data, data.length - 8, 6);
                data[data.length - 2] = this.crc[0];
                data[data.length - 1] = this.crc[1];
            }

            byte[] serialdata = null;
            if (this.serialFlag) {
                this.out.write(data, 6, 9 + byteCount);
                long dateTimeSend = DateTime.getDateTimeTicks();
                byte receivedUnitIdentifier = -1;
                boolean len = true;
                byte[] serialBuffer = new byte[256];
                serialdata = new byte[256];
                int expectedlength = 8;

                for(int currentLength = 0; receivedUnitIdentifier != this.unitIdentifier & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout); receivedUnitIdentifier = serialdata[0]) {
                    while(currentLength < expectedlength & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout)) {
                        len = true;

                        int len2;
                        while((len2 = this.in.read(serialBuffer)) <= 0 & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout)) {
                        }

                        for(int i = 0; i < len2; ++i) {
                            serialdata[currentLength] = serialBuffer[i];
                            ++currentLength;
                        }
                    }
                }

                if (receivedUnitIdentifier != this.unitIdentifier) {
                    data = new byte[256];
                }
            }

            if (serialdata != null) {
                data = new byte[262];
                System.arraycopy(serialdata, 0, data, 6, serialdata.length);
            }

            assert this.tcpClientSocket != null;
            if (this.tcpClientSocket.isConnected() | this.udpFlag) {
                if (this.udpFlag) {
                    InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
                    DatagramSocket clientSocket = new DatagramSocket();
                    clientSocket.setSoTimeout(500);
                    clientSocket.send(sendPacket);
                    data = new byte[2100];
                    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                    clientSocket.receive(receivePacket);
                    clientSocket.close();
                    data = receivePacket.getData();
                } else {
                    this.outStream.write(data, 0, data.length - 2);
                    if (this.sendDataChangedListener.size() > 0) {
                        this.sendData = new byte[data.length - 2];
                        System.arraycopy(data, 0, this.sendData, 0, data.length - 2);

                        for (SendDataChangedListener hl : this.sendDataChangedListener) {
                            hl.SendDataChanged();
                        }
                    }

                    data = new byte[2100];
                    int numberOfBytes = this.inStream.read(data, 0, data.length);
                    if (this.receiveDataChangedListener.size() > 0) {
                        this.receiveData = new byte[numberOfBytes];
                        System.arraycopy(data, 0, this.receiveData, 0, numberOfBytes);

                        for (ReceiveDataChangedListener hl : this.receiveDataChangedListener) {
                            hl.ReceiveDataChanged();
                        }
                    }
                }
            }

            if ((data[7] & 255) == 143 & data[8] == 1) {
                throw new NotSupportedFunctionCodeException("Function code not supported by master");
            } else if ((data[7] & 255) == 143 & data[8] == 2) {
                throw new InvalidStartAddressException("Starting address invalid or starting address + quantity invalid");
            } else if ((data[7] & 255) == 143 & data[8] == 3) {
                throw new InvalidQuantityException("quantity invalid");
            } else if ((data[7] & 255) == 143 & data[8] == 4) {
                throw new ModbusStormException("error reading");
            }
        }
    }

    public void WriteMultipleRegisters(int startingAddress, int[] values) throws ModbusStormException, UnknownHostException, SocketException, IOException {
        byte byteCount = (byte)(values.length * 2);
        byte[] quantityOfOutputs = toByteArray(values.length);
        if (this.tcpClientSocket == null & !this.udpFlag) {
            throw new ConnectionException("connection error");
        } else {
            this.transactionIdentifier = toByteArray(1);
            this.protocolIdentifier = toByteArray(0);
            this.length = toByteArray(7 + values.length * 2);
            this.functionCode = 16;
            this.startingAddress = toByteArray(startingAddress);
            byte[] data = new byte[15 + values.length * 2];
            data[0] = this.transactionIdentifier[1];
            data[1] = this.transactionIdentifier[0];
            data[2] = this.protocolIdentifier[1];
            data[3] = this.protocolIdentifier[0];
            data[4] = this.length[1];
            data[5] = this.length[0];
            data[6] = this.unitIdentifier;
            data[7] = this.functionCode;
            data[8] = this.startingAddress[1];
            data[9] = this.startingAddress[0];
            data[10] = quantityOfOutputs[1];
            data[11] = quantityOfOutputs[0];
            data[12] = byteCount;

            for(int i = 0; i < values.length; ++i) {
                byte[] singleRegisterValue = toByteArray(values[i]);
                data[13 + i * 2] = singleRegisterValue[1];
                data[14 + i * 2] = singleRegisterValue[0];
            }

            if (this.serialFlag) {
                this.crc = calculateCRC(data, data.length - 8, 6);
                data[data.length - 2] = this.crc[0];
                data[data.length - 1] = this.crc[1];
            }

            byte[] serialdata = null;
            if (this.serialFlag) {
                this.out.write(data, 6, 9 + byteCount);
                long dateTimeSend = DateTime.getDateTimeTicks();
                byte receivedUnitIdentifier = -1;
                boolean len = true;
                byte[] serialBuffer = new byte[256];
                serialdata = new byte[256];
                int expectedlength = 8;

                for(int currentLength = 0; receivedUnitIdentifier != this.unitIdentifier & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout); receivedUnitIdentifier = serialdata[0]) {
                    while(currentLength < expectedlength & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout)) {
                        len = true;

                        int len2;
                        while((len2 = this.in.read(serialBuffer)) <= 0 & DateTime.getDateTimeTicks() - dateTimeSend <= (long)(10000L * this.connectTimeout)) {
                        }

                        for(int i = 0; i < len2; ++i) {
                            serialdata[currentLength] = serialBuffer[i];
                            ++currentLength;
                        }
                    }
                }

                if (receivedUnitIdentifier != this.unitIdentifier) {
                    data = new byte[256];
                }
            }

            if (serialdata != null) {
                data = new byte[262];
                System.arraycopy(serialdata, 0, data, 6, serialdata.length);
            }

            assert this.tcpClientSocket != null;
            if (this.tcpClientSocket.isConnected() | this.udpFlag) {
                if (this.udpFlag) {
                    InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
                    DatagramSocket clientSocket = new DatagramSocket();
                    clientSocket.setSoTimeout(500);
                    clientSocket.send(sendPacket);
                    data = new byte[2100];
                    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                    clientSocket.receive(receivePacket);
                    clientSocket.close();
                    data = receivePacket.getData();
                } else {
                    this.outStream.write(data, 0, data.length - 2);
                    if (this.sendDataChangedListener.size() > 0) {
                        this.sendData = new byte[data.length - 2];
                        System.arraycopy(data, 0, this.sendData, 0, data.length - 2);

                        for (SendDataChangedListener hl : this.sendDataChangedListener) {
                            hl.SendDataChanged();
                        }
                    }

                    data = new byte[2100];
                    int numberOfBytes = this.inStream.read(data, 0, data.length);
                    if (this.receiveDataChangedListener.size() > 0) {
                        this.receiveData = new byte[numberOfBytes];
                        System.arraycopy(data, 0, this.receiveData, 0, numberOfBytes);

                        for (ReceiveDataChangedListener hl : this.receiveDataChangedListener) {
                            hl.ReceiveDataChanged();
                        }
                    }
                }
            }

            if ((data[7] & 255) == 144 & data[8] == 1) {
                throw new NotSupportedFunctionCodeException("Function code not supported by master");
            } else if ((data[7] & 255) == 144 & data[8] == 2) {
                throw new InvalidStartAddressException("Starting address invalid or starting address + quantity invalid");
            } else if ((data[7] & 255) == 144 & data[8] == 3) {
                throw new InvalidQuantityException("quantity invalid");
            } else if ((data[7] & 255) == 144 & data[8] == 4) {
                throw new ModbusStormException("error reading");
            }
        }
    }

    public int[] ReadWriteMultipleRegisters(int startingAddressRead, int quantityRead, int startingAddressWrite, int[] values) throws ModbusStormException, UnknownHostException, SocketException, IOException {
        byte[] startingAddressReadLocal = new byte[2];
        byte[] quantityReadLocal = new byte[2];
        byte[] startingAddressWriteLocal = new byte[2];
        byte[] quantityWriteLocal = new byte[2];
        byte writeByteCountLocal = 0;
        if (this.tcpClientSocket == null & !this.udpFlag) {
            throw new ConnectionException("connection error");
        } else if (startingAddressRead > 65535 | quantityRead > 125 | startingAddressWrite > 65535 | values.length > 121) {
            throw new IllegalArgumentException("Starting address must be 0 - 65535; quantity must be 0 - 125");
        } else {
            this.transactionIdentifier = toByteArray(1);
            this.protocolIdentifier = toByteArray(0);
            this.length = toByteArray(6);
            this.functionCode = 23;
            startingAddressReadLocal = toByteArray(startingAddressRead);
            quantityReadLocal = toByteArray(quantityRead);
            startingAddressWriteLocal = toByteArray(startingAddressWrite);
            quantityWriteLocal = toByteArray(values.length);
            writeByteCountLocal = (byte)(values.length * 2);
            byte[] data = new byte[19 + values.length * 2];
            data[0] = this.transactionIdentifier[1];
            data[1] = this.transactionIdentifier[0];
            data[2] = this.protocolIdentifier[1];
            data[3] = this.protocolIdentifier[0];
            data[4] = this.length[1];
            data[5] = this.length[0];
            data[6] = this.unitIdentifier;
            data[7] = this.functionCode;
            data[8] = startingAddressReadLocal[1];
            data[9] = startingAddressReadLocal[0];
            data[10] = quantityReadLocal[1];
            data[11] = quantityReadLocal[0];
            data[12] = startingAddressWriteLocal[1];
            data[13] = startingAddressWriteLocal[0];
            data[14] = quantityWriteLocal[1];
            data[15] = quantityWriteLocal[0];
            data[16] = writeByteCountLocal;

            for(int i = 0; i < values.length; ++i) {
                byte[] singleRegisterValue = toByteArray(values[i]);
                data[17 + i * 2] = singleRegisterValue[1];
                data[18 + i * 2] = singleRegisterValue[0];
            }

            if (this.serialFlag) {
                this.crc = calculateCRC(data, data.length - 8, 6);
                data[data.length - 2] = this.crc[0];
                data[data.length - 1] = this.crc[1];
            }

            byte[] serialdata = null;
            byte highByte;
            if (this.serialFlag) {
                this.out.write(data, 6, 13 + writeByteCountLocal);
                long dateTimeSend = DateTime.getDateTimeTicks();
                highByte = -1;
                boolean len = true;
                byte[] serialBuffer = new byte[256];
                serialdata = new byte[256];
                int expectedlength = 5 + quantityRead;

                for(int currentLength = 0; highByte != this.unitIdentifier & DateTime.getDateTimeTicks() - dateTimeSend <= (10000L * this.connectTimeout); highByte = serialdata[0]) {
                    while(currentLength < expectedlength & DateTime.getDateTimeTicks() - dateTimeSend <= (10000L * this.connectTimeout)) {

                        int len2;
                        while((len2 = this.in.read(serialBuffer)) <= 0 & DateTime.getDateTimeTicks() - dateTimeSend <= (10000L * this.connectTimeout)) {
                        }

                        for(int i = 0; i < len2; ++i) {
                            serialdata[currentLength] = serialBuffer[i];
                            ++currentLength;
                        }
                    }
                }

                if (highByte != this.unitIdentifier) {
                    data = new byte[256];
                }
            }

            if (serialdata != null) {
                data = new byte[262];
                System.arraycopy(serialdata, 0, data, 6, serialdata.length);
            }

            int i;
            assert this.tcpClientSocket != null;
            if (this.tcpClientSocket.isConnected() | this.udpFlag) {
                if (this.udpFlag) {
                    InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
                    DatagramSocket clientSocket = new DatagramSocket();
                    clientSocket.setSoTimeout(500);
                    clientSocket.send(sendPacket);
                    data = new byte[2100];
                    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                    clientSocket.receive(receivePacket);
                    clientSocket.close();
                    data = receivePacket.getData();
                } else {
                    this.outStream.write(data, 0, data.length - 2);
                    if (this.sendDataChangedListener.size() > 0) {
                        this.sendData = new byte[data.length - 2];
                        System.arraycopy(data, 0, this.sendData, 0, data.length - 2);

                        for (SendDataChangedListener hl : this.sendDataChangedListener) {
                            hl.SendDataChanged();
                        }
                    }

                    data = new byte[2100];
                    i = this.inStream.read(data, 0, data.length);
                    if (this.receiveDataChangedListener.size() > 0) {
                        this.receiveData = new byte[i];
                        System.arraycopy(data, 0, this.receiveData, 0, i);

                        for (ReceiveDataChangedListener hl : this.receiveDataChangedListener) {
                            hl.ReceiveDataChanged();
                        }
                    }
                }
            }

            if ((data[7] & 255) == 151 & data[8] == 1) {
                throw new NotSupportedFunctionCodeException("Function code not supported by master");
            } else if ((data[7] & 255) == 151 & data[8] == 2) {
                throw new InvalidStartAddressException("Starting address invalid or starting address + quantity invalid");
            } else if ((data[7] & 255) == 151 & data[8] == 3) {
                throw new InvalidQuantityException("quantity invalid");
            } else if ((data[7] & 255) == 151 & data[8] == 4) {
                throw new ModbusStormException("error reading");
            } else {
                int[] response = new int[quantityRead];

                for(i = 0; i < quantityRead; ++i) {
                    highByte = data[9 + i * 2];
                    byte lowByte = data[9 + i * 2 + 1];
                    byte[] bytes = new byte[]{highByte, lowByte};
                    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                    response[i] = byteBuffer.getShort();
                }

                return response;
            }
        }
    }

    public void Disconnect() throws IOException {
        if (!this.serialFlag) {
            if (this.inStream != null) {
                this.inStream.close();
            }

            if (this.outStream != null) {
                this.outStream.close();
            }

            if (this.tcpClientSocket != null) {
                this.tcpClientSocket.close();
            }

            this.tcpClientSocket = null;
        } else if (this.serialPort != null) {
            this.serialPort.close();
        }

    }

    public static byte[] toByteArray(int value) {
        return new byte[]{(byte)value, (byte)(value >> 8)};
    }

    public static byte[] toByteArrayDouble(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static byte[] toByteArray(float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    public boolean isConnected() {
        if (this.serialFlag) {
            if (this.portIdentifier == null) {
                return false;
            } else {
                return this.portIdentifier.isCurrentlyOwned();
            }
        } else {
            boolean returnValue = false;
            if (this.tcpClientSocket == null) {
                returnValue = false;
            } else returnValue = this.tcpClientSocket.isConnected();

            return returnValue;
        }
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean getUDPFlag() {
        return this.udpFlag;
    }

    public void setUDPFlag(boolean udpFlag) {
        this.udpFlag = udpFlag;
    }

    public int getConnectionTimeout() {
        return this.connectTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectTimeout = connectionTimeout;
    }

    public void setSerialFlag(boolean serialFlag) {
        this.serialFlag = serialFlag;
    }

    public boolean getSerialFlag() {
        return this.serialFlag;
    }

    public void setUnitIdentifier(byte unitIdentifier) {
        this.unitIdentifier = unitIdentifier;
    }

    public byte getUnitIdentifier() {
        return this.unitIdentifier;
    }

    public void addReceiveDataChangedListener(ReceiveDataChangedListener toAdd) {
        this.receiveDataChangedListener.add(toAdd);
    }

    public void addSendDataChangedListener(SendDataChangedListener toAdd) {
        this.sendDataChangedListener.add(toAdd);
    }

    public static enum RegisterOrder {
        LowHigh,
        HighLow;

        private RegisterOrder() {
        }
    }
}
