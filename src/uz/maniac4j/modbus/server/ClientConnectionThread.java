package uz.maniac4j.modbus.server;

import java.io.InputStream;
import java.net.Socket;

class ClientConnectionThread extends Thread {
    private final Socket socket;
    private final byte[] inBuffer = new byte[1024];
    ModbusServer modbusTCPServer;

    public ClientConnectionThread(Socket socket, ModbusServer modbusTCPServer) {
        this.modbusTCPServer = modbusTCPServer;
        this.socket = socket;
    }

    public void run() {
        this.modbusTCPServer.setNumberOfConnectedClients(this.modbusTCPServer.getNumberOfConnectedClients() + 1);

        try {
            this.socket.setSoTimeout(this.modbusTCPServer.getClientConnectionTimeout());

            for(InputStream inputStream = this.socket.getInputStream(); this.socket.isConnected() & !this.socket.isClosed() & this.modbusTCPServer.getServerRunning(); Thread.sleep(5L)) {
                int numberOfBytes = inputStream.read(this.inBuffer);
                if (numberOfBytes > 4) {
                    (new ProcessReceivedDataThread(this.inBuffer, this.modbusTCPServer, this.socket)).start();
                }
            }

            this.modbusTCPServer.setNumberOfConnectedClients(this.modbusTCPServer.getNumberOfConnectedClients() - 1);
            this.socket.close();
        } catch (Exception var4) {
            this.modbusTCPServer.setNumberOfConnectedClients(this.modbusTCPServer.getNumberOfConnectedClients() - 1);

            try {
                this.socket.close();
            } catch (Exception ignored) {
            }

            var4.printStackTrace();
        }

    }
}

