package uz.maniac4j.modbus.server;


import java.net.Socket;
import java.util.Calendar;

class ProcessReceivedDataThread extends Thread {
    short[] inBuffer;
    final ModbusServer modbusTCPServer;
    Socket socket;

    public ProcessReceivedDataThread(byte[] inBuffer, ModbusServer modbusTCPServer, Socket socket) {
        this.socket = socket;
        this.inBuffer = new short[inBuffer.length];

        for(int i = 0; i < inBuffer.length; ++i) {
            this.inBuffer[i] = (short)((short)inBuffer[i] & 255);
        }

        this.modbusTCPServer = modbusTCPServer;
    }

    public void run() {
        synchronized(this.modbusTCPServer) {
            short[] wordData = new short[1];
            short[] byteData = new short[2];
            this.modbusTCPServer.receiveData = new ModbusProtocol();
            this.modbusTCPServer.receiveData.timeStamp = Calendar.getInstance();
            this.modbusTCPServer.receiveData.request = true;
            byteData[1] = this.inBuffer[0];
            byteData[0] = this.inBuffer[1];
            wordData[0] = (short)this.byteArrayToInt(byteData);
            this.modbusTCPServer.receiveData.transactionIdentifier = wordData[0];
            byteData[1] = this.inBuffer[2];
            byteData[0] = this.inBuffer[3];
            wordData[0] = (short)this.byteArrayToInt(byteData);
            this.modbusTCPServer.receiveData.protocolIdentifier = wordData[0];
            byteData[1] = this.inBuffer[4];
            byteData[0] = this.inBuffer[5];
            wordData[0] = (short)this.byteArrayToInt(byteData);
            this.modbusTCPServer.receiveData.length = wordData[0];
            this.modbusTCPServer.receiveData.unitIdentifier = (byte)this.inBuffer[6];
            this.modbusTCPServer.receiveData.functionCode = (byte)this.inBuffer[7];
            byteData[1] = this.inBuffer[8];
            byteData[0] = this.inBuffer[9];
            wordData[0] = (short)this.byteArrayToInt(byteData);
            this.modbusTCPServer.receiveData.startingAddress = wordData[0];
            if (this.modbusTCPServer.receiveData.functionCode <= 4) {
                byteData[1] = this.inBuffer[10];
                byteData[0] = this.inBuffer[11];
                wordData[0] = (short)this.byteArrayToInt(byteData);
                this.modbusTCPServer.receiveData.quantity = wordData[0];
            }

            if (this.modbusTCPServer.receiveData.functionCode == 5) {
                this.modbusTCPServer.receiveData.receiveCoilValues = new short[1];
                byteData[0] = this.inBuffer[10];
                byteData[1] = this.inBuffer[11];
                this.modbusTCPServer.receiveData.receiveCoilValues[0] = (short)this.byteArrayToInt(byteData);
            }

            if (this.modbusTCPServer.receiveData.functionCode == 6) {
                this.modbusTCPServer.receiveData.receiveRegisterValues = new int[1];
                byteData[1] = this.inBuffer[10];
                byteData[0] = this.inBuffer[11];
                this.modbusTCPServer.receiveData.receiveRegisterValues[0] = this.byteArrayToInt(byteData);
            }

            int i;
            if (this.modbusTCPServer.receiveData.functionCode == 15) {
                byteData[1] = this.inBuffer[10];
                byteData[0] = this.inBuffer[11];
                wordData[0] = (short)this.byteArrayToInt(byteData);
                this.modbusTCPServer.receiveData.quantity = wordData[0];
                this.modbusTCPServer.receiveData.byteCount = (byte)this.inBuffer[12];
                if (this.modbusTCPServer.receiveData.byteCount % 2 != 0) {
                    this.modbusTCPServer.receiveData.receiveCoilValues = new short[this.modbusTCPServer.receiveData.byteCount / 2 + 1];
                } else {
                    this.modbusTCPServer.receiveData.receiveCoilValues = new short[this.modbusTCPServer.receiveData.byteCount / 2];
                }

                for(i = 0; i < this.modbusTCPServer.receiveData.byteCount; ++i) {
                    if (i % 2 == 1) {
                        this.modbusTCPServer.receiveData.receiveCoilValues[i / 2] = (short)(this.modbusTCPServer.receiveData.receiveCoilValues[i / 2] + 256 * this.inBuffer[13 + i]);
                    } else {
                        this.modbusTCPServer.receiveData.receiveCoilValues[i / 2] = this.inBuffer[13 + i];
                    }
                }
            }

            if (this.modbusTCPServer.receiveData.functionCode == 16) {
                byteData[1] = this.inBuffer[10];
                byteData[0] = this.inBuffer[11];
                wordData[0] = (short)this.byteArrayToInt(byteData);
                this.modbusTCPServer.receiveData.quantity = wordData[0];
                this.modbusTCPServer.receiveData.byteCount = (byte)this.inBuffer[12];
                this.modbusTCPServer.receiveData.receiveRegisterValues = new int[this.modbusTCPServer.receiveData.quantity];

                for(i = 0; i < this.modbusTCPServer.receiveData.quantity; ++i) {
                    byteData[1] = this.inBuffer[13 + i * 2];
                    byteData[0] = this.inBuffer[14 + i * 2];
                    this.modbusTCPServer.receiveData.receiveRegisterValues[i] = byteData[0];
                    this.modbusTCPServer.receiveData.receiveRegisterValues[i] += byteData[1] << 8;
                }
            }

            this.modbusTCPServer.CreateAnswer(this.socket);
            this.modbusTCPServer.CreateLogData();
        }
    }

    public int byteArrayToInt(short[] byteArray) {
        int returnValue = byteArray[0];
        returnValue = returnValue + 256 * byteArray[1];
        return returnValue;
    }
}

