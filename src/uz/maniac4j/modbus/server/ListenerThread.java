package uz.maniac4j.modbus.server;



import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class ListenerThread extends Thread {
    ModbusServer modbusTCPServer;

    public ListenerThread(ModbusServer modbusTCPServer) {
        this.modbusTCPServer = modbusTCPServer;
    }

    public void run() {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(this.modbusTCPServer.getPort());

            while(this.modbusTCPServer.getServerRunning() & !this.isInterrupted()) {
                Socket socket = serverSocket.accept();
                (new ClientConnectionThread(socket, this.modbusTCPServer)).start();
            }
        } catch (IOException var4) {
            System.out.println(var4.getMessage());
            var4.printStackTrace();
        }

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException var3) {
                var3.printStackTrace();
            }
        }

    }
}

