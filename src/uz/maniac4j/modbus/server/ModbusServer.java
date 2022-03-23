package uz.maniac4j.modbus.server;


import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;

public class ModbusServer extends Thread {
    private int port = 502;
    protected ModbusProtocol receiveData;
    protected ModbusProtocol sendData = new ModbusProtocol();
    public int[] holdingRegisters = new int['\uffff'];
    public int[] inputRegisters = new int['\uffff'];
    public boolean[] coils = new boolean['\uffff'];
    public boolean[] discreteInputs = new boolean['\uffff'];
    private int numberOfConnections = 0;
    public boolean udpFlag;
    private int clientConnectionTimeout = 10000;
    private final ModbusProtocol[] modbusLogData = new ModbusProtocol[100];
    private boolean functionCode1Disabled;
    private boolean functionCode2Disabled;
    private boolean functionCode3Disabled;
    private boolean functionCode4Disabled;
    private boolean functionCode5Disabled;
    private boolean functionCode6Disabled;
    private boolean functionCode15Disabled;
    private boolean functionCode16Disabled;
    private boolean serverRunning;
    private ListenerThread listenerThread;
    protected ICoilsChangedDelegator notifyCoilsChanged;
    protected IHoldingRegistersChangedDelegator notifyHoldingRegistersChanged;
    protected INumberOfConnectedClientsChangedDelegator notifyNumberOfConnectedClientsChanged;
    protected ILogDataChangedDelegator notifyLogDataChanged;

    public ModbusServer() {
        System.out.println("Modbus Storm Server Library");
        System.out.println("Maniac4j");
    }

    protected void finalize() {
        this.serverRunning = false;
        this.listenerThread.stop();
    }

    public void Listen() throws IOException {
        this.serverRunning = true;
        this.listenerThread = new ListenerThread(this);
        this.listenerThread.start();
    }

    public void StopListening() {
        this.serverRunning = false;
        this.listenerThread.stop();
    }

    protected void CreateAnswer(Socket socket) {
        switch(this.receiveData.functionCode) {
            case 1:
                if (!this.functionCode1Disabled) {
                    this.ReadCoils(socket);
                } else {
                    this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
                    this.sendData.exceptionCode = 1;
                    this.sendException(this.sendData.errorCode, this.sendData.exceptionCode, socket);
                }
                break;
            case 2:
                if (!this.functionCode2Disabled) {
                    this.ReadDiscreteInputs(socket);
                } else {
                    this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
                    this.sendData.exceptionCode = 1;
                    this.sendException(this.sendData.errorCode, this.sendData.exceptionCode, socket);
                }
                break;
            case 3:
                if (!this.functionCode3Disabled) {
                    this.ReadHoldingRegisters(socket);
                } else {
                    this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
                    this.sendData.exceptionCode = 1;
                    this.sendException(this.sendData.errorCode, this.sendData.exceptionCode, socket);
                }
                break;
            case 4:
                if (!this.functionCode4Disabled) {
                    this.ReadInputRegisters(socket);
                } else {
                    this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
                    this.sendData.exceptionCode = 1;
                    this.sendException(this.sendData.errorCode, this.sendData.exceptionCode, socket);
                }
                break;
            case 5:
                if (!this.functionCode5Disabled) {
                    this.WriteSingleCoil(socket);
                } else {
                    this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
                    this.sendData.exceptionCode = 1;
                    this.sendException(this.sendData.errorCode, this.sendData.exceptionCode, socket);
                }
                break;
            case 6:
                if (!this.functionCode6Disabled) {
                    this.WriteSingleRegister(socket);
                } else {
                    this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
                    this.sendData.exceptionCode = 1;
                    this.sendException(this.sendData.errorCode, this.sendData.exceptionCode, socket);
                }
                break;
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            default:
                this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
                this.sendData.exceptionCode = 1;
                this.sendException(this.sendData.errorCode, this.sendData.exceptionCode, socket);
                break;
            case 15:
                if (!this.functionCode15Disabled) {
                    this.WriteMultipleCoils(socket);
                } else {
                    this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
                    this.sendData.exceptionCode = 1;
                    this.sendException(this.sendData.errorCode, this.sendData.exceptionCode, socket);
                }
                break;
            case 16:
                if (!this.functionCode16Disabled) {
                    this.WriteMultipleRegisters(socket);
                } else {
                    this.sendData.errorCode = (byte)(this.receiveData.functionCode + 144);
                    this.sendData.exceptionCode = 1;
                    this.sendException(this.sendData.errorCode, this.sendData.exceptionCode, socket);
                }
        }

        this.sendData.timeStamp = Calendar.getInstance();
    }

    private void ReadCoils(Socket socket) {
        this.sendData = new ModbusProtocol();
        this.sendData.response = true;
        this.sendData.transactionIdentifier = this.receiveData.transactionIdentifier;
        this.sendData.protocolIdentifier = this.receiveData.protocolIdentifier;
        this.sendData.unitIdentifier = this.receiveData.unitIdentifier;
        this.sendData.functionCode = this.receiveData.functionCode;
        if (this.receiveData.quantity < 1 | this.receiveData.quantity > 2000) {
            this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
            this.sendData.exceptionCode = 3;
        }

        if (this.receiveData.startingAddress + 1 + this.receiveData.quantity > 65535) {
            this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
            this.sendData.exceptionCode = 2;
        }

        if (this.receiveData.quantity % 8 == 0) {
            this.sendData.byteCount = (byte)(this.receiveData.quantity / 8);
        } else {
            this.sendData.byteCount = (byte)(this.receiveData.quantity / 8 + 1);
        }

        this.sendData.sendCoilValues = new boolean[this.receiveData.quantity];
        System.arraycopy(this.coils, this.receiveData.startingAddress + 1, this.sendData.sendCoilValues, 0, this.sendData.sendCoilValues.length);
        byte[] data;
        if (this.sendData.exceptionCode > 0) {
            data = new byte[9];
        } else {
            data = new byte[9 + this.sendData.byteCount];
        }

        byte[] byteData = new byte[2];
        this.sendData.length = (byte)(data.length - 6);
        data[0] = (byte)((this.sendData.transactionIdentifier & '\uff00') >> 8);
        data[1] = (byte)(this.sendData.transactionIdentifier & 255);
        data[2] = (byte)((this.sendData.protocolIdentifier & '\uff00') >> 8);
        data[3] = (byte)(this.sendData.protocolIdentifier & 255);
        data[4] = (byte)((this.sendData.length & '\uff00') >> 8);
        data[5] = (byte)(this.sendData.length & 255);
        data[6] = this.sendData.unitIdentifier;
        data[7] = this.sendData.functionCode;
        data[8] = (byte)(this.sendData.byteCount & 255);
        if (this.sendData.exceptionCode > 0) {
            data[7] = this.sendData.errorCode;
            data[8] = this.sendData.exceptionCode;
            this.sendData.sendCoilValues = null;
        }

        if (this.sendData.sendCoilValues != null) {
            for(int i = 0; i < this.sendData.byteCount; ++i) {
                byteData = new byte[2];

                for(int j = 0; j < 8; ++j) {
                    byte boolValue;
                    if (this.sendData.sendCoilValues[i * 8 + j]) {
                        boolValue = 1;
                    } else {
                        boolValue = 0;
                    }

                    byteData[1] = (byte)(byteData[1] | boolValue << j);
                    if (i * 8 + j + 1 >= this.sendData.sendCoilValues.length) {
                        break;
                    }
                }

                data[9 + i] = byteData[1];
            }
        }

        if (socket.isConnected() & !socket.isClosed()) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(data);
            } catch (IOException var7) {
                var7.printStackTrace();
            }
        }

    }

    private void ReadDiscreteInputs(Socket socket) {
        this.sendData = new ModbusProtocol();
        this.sendData.response = true;
        this.sendData.transactionIdentifier = this.receiveData.transactionIdentifier;
        this.sendData.protocolIdentifier = this.receiveData.protocolIdentifier;
        this.sendData.unitIdentifier = this.receiveData.unitIdentifier;
        this.sendData.functionCode = this.receiveData.functionCode;
        if (this.receiveData.quantity < 1 | this.receiveData.quantity > 2000) {
            this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
            this.sendData.exceptionCode = 3;
        }

        if (this.receiveData.startingAddress + 1 + this.receiveData.quantity > 65535) {
            this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
            this.sendData.exceptionCode = 2;
        }

        if (this.receiveData.quantity % 8 == 0) {
            this.sendData.byteCount = (byte)(this.receiveData.quantity / 8);
        } else {
            this.sendData.byteCount = (byte)(this.receiveData.quantity / 8 + 1);
        }

        this.sendData.sendCoilValues = new boolean[this.receiveData.quantity];
        System.arraycopy(this.discreteInputs, this.receiveData.startingAddress + 1, this.sendData.sendCoilValues, 0, this.receiveData.quantity);
        byte[] data;
        if (this.sendData.exceptionCode > 0) {
            data = new byte[9];
        } else {
            data = new byte[9 + this.sendData.byteCount];
        }

        byte[] byteData = new byte[2];
        this.sendData.length = (byte)(data.length - 6);
        data[0] = (byte)((this.sendData.transactionIdentifier & '\uff00') >> 8);
        data[1] = (byte)(this.sendData.transactionIdentifier & 255);
        data[2] = (byte)((this.sendData.protocolIdentifier & '\uff00') >> 8);
        data[3] = (byte)(this.sendData.protocolIdentifier & 255);
        data[4] = (byte)((this.sendData.length & '\uff00') >> 8);
        data[5] = (byte)(this.sendData.length & 255);
        data[6] = this.sendData.unitIdentifier;
        data[7] = this.sendData.functionCode;
        data[8] = (byte)(this.sendData.byteCount & 255);
        if (this.sendData.exceptionCode > 0) {
            data[7] = this.sendData.errorCode;
            data[8] = this.sendData.exceptionCode;
            this.sendData.sendCoilValues = null;
        }

        if (this.sendData.sendCoilValues != null) {
            for(int i = 0; i < this.sendData.byteCount; ++i) {
                byteData = new byte[2];

                for(int j = 0; j < 8; ++j) {
                    byte boolValue;
                    if (this.sendData.sendCoilValues[i * 8 + j]) {
                        boolValue = 1;
                    } else {
                        boolValue = 0;
                    }

                    byteData[1] = (byte)(byteData[1] | boolValue << j);
                    if (i * 8 + j + 1 >= this.sendData.sendCoilValues.length) {
                        break;
                    }
                }

                data[9 + i] = byteData[1];
            }
        }

        if (socket.isConnected() & !socket.isClosed()) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(data);
            } catch (IOException var7) {
                var7.printStackTrace();
            }
        }

    }

    private void ReadHoldingRegisters(Socket socket) {
        this.sendData = new ModbusProtocol();
        this.sendData.response = true;
        this.sendData.transactionIdentifier = this.receiveData.transactionIdentifier;
        this.sendData.protocolIdentifier = this.receiveData.protocolIdentifier;
        this.sendData.unitIdentifier = this.receiveData.unitIdentifier;
        this.sendData.functionCode = this.receiveData.functionCode;
        if (this.receiveData.quantity < 1 | this.receiveData.quantity > 125) {
            this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
            this.sendData.exceptionCode = 3;
        }

        if (this.receiveData.startingAddress + 1 + this.receiveData.quantity > 65535) {
            this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
            this.sendData.exceptionCode = 2;
        }

        this.sendData.byteCount = (short)(2 * this.receiveData.quantity);
        this.sendData.sendRegisterValues = new int[this.receiveData.quantity];
        System.arraycopy(this.holdingRegisters, this.receiveData.startingAddress + 1, this.sendData.sendRegisterValues, 0, this.receiveData.quantity);
        if (this.sendData.exceptionCode > 0) {
            this.sendData.length = 3;
        } else {
            this.sendData.length = (short)(3 + this.sendData.byteCount);
        }

        byte[] data;
        if (this.sendData.exceptionCode > 0) {
            data = new byte[9];
        } else {
            data = new byte[9 + this.sendData.byteCount];
        }

        this.sendData.length = (byte)(data.length - 6);
        data[0] = (byte)((this.sendData.transactionIdentifier & '\uff00') >> 8);
        data[1] = (byte)(this.sendData.transactionIdentifier & 255);
        data[2] = (byte)((this.sendData.protocolIdentifier & '\uff00') >> 8);
        data[3] = (byte)(this.sendData.protocolIdentifier & 255);
        data[4] = (byte)((this.sendData.length & '\uff00') >> 8);
        data[5] = (byte)(this.sendData.length & 255);
        data[6] = this.sendData.unitIdentifier;
        data[7] = this.sendData.functionCode;
        data[8] = (byte)(this.sendData.byteCount & 255);
        if (this.sendData.exceptionCode > 0) {
            data[7] = this.sendData.errorCode;
            data[8] = this.sendData.exceptionCode;
            this.sendData.sendRegisterValues = null;
        }

        if (this.sendData.sendRegisterValues != null) {
            for(int i = 0; i < this.sendData.byteCount / 2; ++i) {
                data[9 + i * 2] = (byte)((this.sendData.sendRegisterValues[i] & '\uff00') >> 8);
                data[10 + i * 2] = (byte)(this.sendData.sendRegisterValues[i] & 255);
            }
        }

        if (socket.isConnected() & !socket.isClosed()) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(data);
            } catch (IOException var5) {
                var5.printStackTrace();
            }
        }

    }

    private void ReadInputRegisters(Socket socket) {
        this.sendData = new ModbusProtocol();
        this.sendData.response = true;
        this.sendData.transactionIdentifier = this.receiveData.transactionIdentifier;
        this.sendData.protocolIdentifier = this.receiveData.protocolIdentifier;
        this.sendData.unitIdentifier = this.receiveData.unitIdentifier;
        this.sendData.functionCode = this.receiveData.functionCode;
        if (this.receiveData.quantity < 1 | this.receiveData.quantity > 125) {
            this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
            this.sendData.exceptionCode = 3;
        }

        if (this.receiveData.startingAddress + 1 + this.receiveData.quantity > 65535) {
            this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
            this.sendData.exceptionCode = 2;
        }

        this.sendData.byteCount = (short)(2 * this.receiveData.quantity);
        this.sendData.sendRegisterValues = new int[this.receiveData.quantity];
        System.arraycopy(this.inputRegisters, this.receiveData.startingAddress + 1, this.sendData.sendRegisterValues, 0, this.receiveData.quantity);
        if (this.sendData.exceptionCode > 0) {
            this.sendData.length = 3;
        } else {
            this.sendData.length = (short)(3 + this.sendData.byteCount);
        }

        byte[] data;
        if (this.sendData.exceptionCode > 0) {
            data = new byte[9];
        } else {
            data = new byte[9 + this.sendData.byteCount];
        }

        this.sendData.length = (byte)(data.length - 6);
        data[0] = (byte)((this.sendData.transactionIdentifier & '\uff00') >> 8);
        data[1] = (byte)(this.sendData.transactionIdentifier & 255);
        data[2] = (byte)((this.sendData.protocolIdentifier & '\uff00') >> 8);
        data[3] = (byte)(this.sendData.protocolIdentifier & 255);
        data[4] = (byte)((this.sendData.length & '\uff00') >> 8);
        data[5] = (byte)(this.sendData.length & 255);
        data[6] = this.sendData.unitIdentifier;
        data[7] = this.sendData.functionCode;
        data[8] = (byte)(this.sendData.byteCount & 255);
        if (this.sendData.exceptionCode > 0) {
            data[7] = this.sendData.errorCode;
            data[8] = this.sendData.exceptionCode;
            this.sendData.sendRegisterValues = null;
        }

        if (this.sendData.sendRegisterValues != null) {
            for(int i = 0; i < this.sendData.byteCount / 2; ++i) {
                data[9 + i * 2] = (byte)((this.sendData.sendRegisterValues[i] & '\uff00') >> 8);
                data[10 + i * 2] = (byte)(this.sendData.sendRegisterValues[i] & 255);
            }
        }

        if (socket.isConnected() & !socket.isClosed()) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(data);
            } catch (IOException var5) {
                var5.printStackTrace();
            }
        }

    }

    private void WriteSingleCoil(Socket socket) {
        this.sendData = new ModbusProtocol();
        this.sendData.response = true;
        this.sendData.transactionIdentifier = this.receiveData.transactionIdentifier;
        this.sendData.protocolIdentifier = this.receiveData.protocolIdentifier;
        this.sendData.unitIdentifier = this.receiveData.unitIdentifier;
        this.sendData.functionCode = this.receiveData.functionCode;
        this.sendData.startingAddress = this.receiveData.startingAddress;
        this.sendData.receiveCoilValues = this.receiveData.receiveCoilValues;
        if (this.receiveData.receiveCoilValues[0] != 0 & this.receiveData.receiveCoilValues[0] != 255) {
            this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
            this.sendData.exceptionCode = 3;
        }

        if (this.receiveData.startingAddress + 1 > 65535) {
            this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
            this.sendData.exceptionCode = 2;
        }

        if (this.receiveData.receiveCoilValues[0] > 0) {
            this.coils[this.receiveData.startingAddress + 1] = true;
        }

        if (this.receiveData.receiveCoilValues[0] == 0) {
            this.coils[this.receiveData.startingAddress + 1] = false;
        }

        if (this.sendData.exceptionCode > 0) {
            this.sendData.length = 3;
        } else {
            this.sendData.length = 6;
        }

        byte[] data;
        if (this.sendData.exceptionCode > 0) {
            data = new byte[9];
        } else {
            data = new byte[12];
        }

        data[0] = (byte)((this.sendData.transactionIdentifier & '\uff00') >> 8);
        data[1] = (byte)(this.sendData.transactionIdentifier & 255);
        data[2] = (byte)((this.sendData.protocolIdentifier & '\uff00') >> 8);
        data[3] = (byte)(this.sendData.protocolIdentifier & 255);
        data[4] = (byte)(0);
        data[5] = (byte)(this.sendData.length & 255);
        data[6] = this.sendData.unitIdentifier;
        data[7] = this.sendData.functionCode;
        if (this.sendData.exceptionCode > 0) {
            data[7] = this.sendData.errorCode;
            data[8] = this.sendData.exceptionCode;
            this.sendData.sendRegisterValues = null;
        } else {
            data[8] = (byte)((this.receiveData.startingAddress & '\uff00') >> 8);
            data[9] = (byte)(this.receiveData.startingAddress & 255);
            data[10] = (byte)this.receiveData.receiveCoilValues[0];
            data[11] = 0;
        }

        if (socket.isConnected() & !socket.isClosed()) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(data);
            } catch (IOException var5) {
                var5.printStackTrace();
            }
        }

        if (this.notifyCoilsChanged != null) {
            this.notifyCoilsChanged.coilsChangedEvent();
        }

    }

    private void WriteSingleRegister(Socket socket) {
        this.sendData = new ModbusProtocol();
        this.sendData.response = true;
        this.sendData.transactionIdentifier = this.receiveData.transactionIdentifier;
        this.sendData.protocolIdentifier = this.receiveData.protocolIdentifier;
        this.sendData.unitIdentifier = this.receiveData.unitIdentifier;
        this.sendData.functionCode = this.receiveData.functionCode;
        this.sendData.startingAddress = this.receiveData.startingAddress;
        this.sendData.receiveRegisterValues = this.receiveData.receiveRegisterValues;
        if (this.receiveData.receiveRegisterValues[0] < 0 | this.receiveData.receiveRegisterValues[0] > 65535) {
            this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
            this.sendData.exceptionCode = 3;
        }

        if (this.receiveData.startingAddress + 1 > 65535) {
            this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
            this.sendData.exceptionCode = 2;
        }

        this.holdingRegisters[this.receiveData.startingAddress + 1] = this.receiveData.receiveRegisterValues[0];
        if (this.sendData.exceptionCode > 0) {
            this.sendData.length = 3;
        } else {
            this.sendData.length = 6;
        }

        byte[] data;
        if (this.sendData.exceptionCode > 0) {
            data = new byte[9];
        } else {
            data = new byte[12];
        }

        data[0] = (byte)((this.sendData.transactionIdentifier & '\uff00') >> 8);
        data[1] = (byte)(this.sendData.transactionIdentifier & 255);
        data[2] = (byte)((this.sendData.protocolIdentifier & '\uff00') >> 8);
        data[3] = (byte)(this.sendData.protocolIdentifier & 255);
        data[4] = (byte)(0);
        data[5] = (byte)(this.sendData.length & 255);
        data[6] = this.sendData.unitIdentifier;
        data[7] = this.sendData.functionCode;
        if (this.sendData.exceptionCode > 0) {
            data[7] = this.sendData.errorCode;
            data[8] = this.sendData.exceptionCode;
            this.sendData.sendRegisterValues = null;
        } else {
            data[8] = (byte)((this.receiveData.startingAddress & '\uff00') >> 8);
            data[9] = (byte)(this.receiveData.startingAddress & 255);
            data[10] = (byte)((this.receiveData.receiveRegisterValues[0] & '\uff00') >> 8);
            data[11] = (byte)(this.receiveData.receiveRegisterValues[0] & 255);
        }

        if (socket.isConnected() & !socket.isClosed()) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(data);
            } catch (IOException var5) {
                var5.printStackTrace();
            }
        }

        if (this.notifyHoldingRegistersChanged != null) {
            this.notifyHoldingRegistersChanged.holdingRegistersChangedEvent();
        }

    }

    private void WriteMultipleCoils(Socket socket) {
        this.sendData = new ModbusProtocol();
        this.sendData.response = true;
        this.sendData.transactionIdentifier = this.receiveData.transactionIdentifier;
        this.sendData.protocolIdentifier = this.receiveData.protocolIdentifier;
        this.sendData.unitIdentifier = this.receiveData.unitIdentifier;
        this.sendData.functionCode = this.receiveData.functionCode;
        this.sendData.startingAddress = this.receiveData.startingAddress;
        this.sendData.quantity = this.receiveData.quantity;
        if (this.receiveData.quantity == 0 | this.receiveData.quantity > 1968) {
            this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
            this.sendData.exceptionCode = 3;
        }

        if (this.receiveData.startingAddress + 1 + this.receiveData.quantity > 65535) {
            this.sendData.errorCode = (byte)(this.receiveData.functionCode + 128);
            this.sendData.exceptionCode = 2;
        }

        for(int i = 0; i < this.receiveData.quantity; ++i) {
            int shift = i % 16;
            int mask = 1;
            mask = mask << shift;
            this.coils[this.receiveData.startingAddress + i + 1] = (this.receiveData.receiveCoilValues[i / 16] & mask) != 0;
        }

        if (this.sendData.exceptionCode > 0) {
            this.sendData.length = 3;
        } else {
            this.sendData.length = 6;
        }

        byte[] data;
        if (this.sendData.exceptionCode > 0) {
            data = new byte[9];
        } else {
            data = new byte[12];
        }

        data[0] = (byte)((this.sendData.transactionIdentifier & '\uff00') >> 8);
        data[1] = (byte)(this.sendData.transactionIdentifier & 255);
        data[2] = (byte)((this.sendData.protocolIdentifier & '\uff00') >> 8);
        data[3] = (byte)(this.sendData.protocolIdentifier & 255);
        data[4] = (byte)(0);
        data[5] = (byte)(this.sendData.length & 255);
        data[6] = this.sendData.unitIdentifier;
        data[7] = this.sendData.functionCode;
        if (this.sendData.exceptionCode > 0) {
            data[7] = this.sendData.errorCode;
            data[8] = this.sendData.exceptionCode;
            this.sendData.sendRegisterValues = null;
        } else {
            data[8] = (byte)((this.receiveData.startingAddress & '\uff00') >> 8);
            data[9] = (byte)(this.receiveData.startingAddress & 255);
            data[10] = (byte)((this.receiveData.quantity & '\uff00') >> 8);
            data[11] = (byte)(this.receiveData.quantity & 255);
        }

        if (socket.isConnected() & !socket.isClosed()) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(data);
            } catch (Exception var5) {
                var5.printStackTrace();
            }
        }

        if (this.notifyCoilsChanged != null) {
            this.notifyCoilsChanged.coilsChangedEvent();
        }

    }

    private void WriteMultipleRegisters(Socket socket) {
        this.sendData = new ModbusProtocol();
        this.sendData.response = true;
        this.sendData.transactionIdentifier = this.receiveData.transactionIdentifier;
        this.sendData.protocolIdentifier = this.receiveData.protocolIdentifier;
        this.sendData.unitIdentifier = this.receiveData.unitIdentifier;
        this.sendData.functionCode = this.receiveData.functionCode;
        this.sendData.startingAddress = this.receiveData.startingAddress;
        this.sendData.quantity = this.receiveData.quantity;
        if (this.receiveData.quantity == 0 | this.receiveData.quantity > 1968) {
            this.sendData.errorCode = (byte)(this.receiveData.functionCode + 144);
            this.sendData.exceptionCode = 3;
        }

        if (this.receiveData.startingAddress + 1 + this.receiveData.quantity > 65535) {
            this.sendData.errorCode = (byte)(this.receiveData.functionCode + 144);
            this.sendData.exceptionCode = 2;
        }

        for(int i = 0; i < this.receiveData.quantity; ++i) {
            this.holdingRegisters[this.receiveData.startingAddress + i + 1] = this.receiveData.receiveRegisterValues[i];
        }

        if (this.sendData.exceptionCode > 0) {
            this.sendData.length = 3;
        } else {
            this.sendData.length = 6;
        }

        byte[] data;
        if (this.sendData.exceptionCode > 0) {
            data = new byte[9];
        } else {
            data = new byte[12];
        }

        data[0] = (byte)((this.sendData.transactionIdentifier & '\uff00') >> 8);
        data[1] = (byte)(this.sendData.transactionIdentifier & 255);
        data[2] = (byte)((this.sendData.protocolIdentifier & '\uff00') >> 8);
        data[3] = (byte)(this.sendData.protocolIdentifier & 255);
        data[4] = (byte)(0);
        data[5] = (byte)(this.sendData.length & 255);
        data[6] = this.sendData.unitIdentifier;
        data[7] = this.sendData.functionCode;
        if (this.sendData.exceptionCode > 0) {
            data[7] = this.sendData.errorCode;
            data[8] = this.sendData.exceptionCode;
            this.sendData.sendRegisterValues = null;
        } else {
            data[8] = (byte)((this.receiveData.startingAddress & '\uff00') >> 8);
            data[9] = (byte)(this.receiveData.startingAddress & 255);
            data[10] = (byte)((this.receiveData.quantity & '\uff00') >> 8);
            data[11] = (byte)(this.receiveData.quantity & 255);
        }

        if (socket.isConnected() & !socket.isClosed()) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(data);
            } catch (IOException var5) {
                var5.printStackTrace();
            }
        }

        if (this.notifyHoldingRegistersChanged != null) {
            this.notifyHoldingRegistersChanged.holdingRegistersChangedEvent();
        }

    }

    private void sendException(int errorCode, int exceptionCode, Socket socket) {
        this.sendData = new ModbusProtocol();
        this.sendData.response = true;
        this.sendData.transactionIdentifier = this.receiveData.transactionIdentifier;
        this.sendData.protocolIdentifier = this.receiveData.protocolIdentifier;
        this.sendData.unitIdentifier = this.receiveData.unitIdentifier;
        this.sendData.errorCode = (byte)errorCode;
        this.sendData.exceptionCode = (byte)exceptionCode;
        if (this.sendData.exceptionCode > 0) {
            this.sendData.length = 3;
        } else {
            this.sendData.length = (short)(3 + this.sendData.byteCount);
        }

        byte[] data;
        if (this.sendData.exceptionCode > 0) {
            data = new byte[9];
        } else {
            data = new byte[9 + this.sendData.byteCount];
        }

        this.sendData.length = (byte)(data.length - 6);
        data[0] = (byte)((this.sendData.transactionIdentifier & '\uff00') >> 8);
        data[1] = (byte)(this.sendData.transactionIdentifier & 255);
        data[2] = (byte)((this.sendData.protocolIdentifier & '\uff00') >> 8);
        data[3] = (byte)(this.sendData.protocolIdentifier & 255);
        data[4] = (byte)((this.sendData.length & '\uff00') >> 8);
        data[5] = (byte)(this.sendData.length & 255);
        data[6] = this.sendData.unitIdentifier;
        data[7] = this.sendData.errorCode;
        data[8] = this.sendData.exceptionCode;
        if (socket.isConnected() & !socket.isClosed()) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(data);
            } catch (IOException var7) {
                var7.printStackTrace();
            }
        }

    }

    protected void CreateLogData() {
        for(int i = 0; i < 98; ++i) {
            this.modbusLogData[99 - i] = this.modbusLogData[99 - i - 2];
        }

        this.modbusLogData[0] = this.receiveData;
        this.modbusLogData[1] = this.sendData;
        if (this.notifyLogDataChanged != null) {
            this.notifyLogDataChanged.logDataChangedEvent();
        }

    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setFunctionCode1Disabled(boolean functionCode1Disabled) {
        this.functionCode1Disabled = functionCode1Disabled;
    }

    public void setFunctionCode2Disabled(boolean functionCode2Disabled) {
        this.functionCode2Disabled = functionCode2Disabled;
    }

    public void setFunctionCode3Disabled(boolean functionCode3Disabled) {
        this.functionCode3Disabled = functionCode3Disabled;
    }

    public void setFunctionCode4Disabled(boolean functionCode4Disabled) {
        this.functionCode4Disabled = functionCode4Disabled;
    }

    public void setFunctionCode5Disabled(boolean functionCode5Disabled) {
        this.functionCode5Disabled = functionCode5Disabled;
    }

    public void setFunctionCode6Disabled(boolean functionCode6Disabled) {
        this.functionCode6Disabled = functionCode6Disabled;
    }

    public void setFunctionCode15Disabled(boolean functionCode15Disabled) {
        this.functionCode15Disabled = functionCode15Disabled;
    }

    public void setFunctionCode16Disabled(boolean functionCode16Disabled) {
        this.functionCode16Disabled = functionCode16Disabled;
    }

    public void setNumberOfConnectedClients(int value) {
        this.numberOfConnections = value;
        if (this.notifyNumberOfConnectedClientsChanged != null) {
            this.notifyNumberOfConnectedClientsChanged.NumberOfConnectedClientsChanged();
        }

    }

    public int getPort() {
        return this.port;
    }

    public boolean getFunctionCode1Disabled() {
        return this.functionCode1Disabled;
    }

    public boolean getFunctionCode2Disabled() {
        return this.functionCode2Disabled;
    }

    public boolean getFunctionCode3Disabled() {
        return this.functionCode3Disabled;
    }

    public boolean getFunctionCode4Disabled() {
        return this.functionCode4Disabled;
    }

    public boolean getFunctionCode5Disabled() {
        return this.functionCode5Disabled;
    }

    public boolean getFunctionCode6Disabled() {
        return this.functionCode6Disabled;
    }

    public boolean getFunctionCode15Disabled() {
        return this.functionCode15Disabled;
    }

    public boolean getFunctionCode16Disabled() {
        return this.functionCode16Disabled;
    }

    public int getNumberOfConnectedClients() {
        return this.numberOfConnections;
    }

    public boolean getServerRunning() {
        return this.serverRunning;
    }

    public ModbusProtocol[] getLogData() {
        return this.modbusLogData;
    }

    public void setNotifyCoilsChanged(ICoilsChangedDelegator value) {
        this.notifyCoilsChanged = value;
    }

    public void setNotifyHoldingRegistersChanged(IHoldingRegistersChangedDelegator value) {
        this.notifyHoldingRegistersChanged = value;
    }

    public void setNotifyNumberOfConnectedClientsChanged(INumberOfConnectedClientsChangedDelegator value) {
        this.notifyNumberOfConnectedClientsChanged = value;
    }

    public void setNotifyLogDataChanged(ILogDataChangedDelegator value) {
        this.notifyLogDataChanged = value;
    }

    public int getClientConnectionTimeout() {
        return this.clientConnectionTimeout;
    }

    public void setClientConnectionTimeout(int value) {
        this.clientConnectionTimeout = value;
    }
}

