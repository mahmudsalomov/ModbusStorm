package uz.maniac4j.ModbusRTU;

import uz.maniac4j.modbus.client.ModbusClient;

import java.io.IOException;

public class ModbusRTU {
    public ModbusRTU() {
    }

    public static void main(String[] args) throws IOException {
        boolean success = false;
        ModbusClient modbusClient = new ModbusClient();

        while(!success) {
            try {
                modbusClient.Connect("127.0.0.1", 502);
                boolean[] response = modbusClient.ReadCoils(2, 20);
                int[] responseint = modbusClient.ReadHoldingRegisters(0, 20);
                modbusClient.WriteSingleCoil(0, true);
                modbusClient.WriteSingleRegister(200, 456);
                modbusClient.WriteMultipleCoils(200, new boolean[]{true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true});
                modbusClient.WriteMultipleRegisters(300, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});

                for(int i = 0; i < response.length; ++i) {
                    System.out.println(response[i]);
                    System.out.println(responseint[i]);
                }

                success = true;
                Thread.sleep(1000L);
            } catch (Exception var9) {
                var9.printStackTrace();
            } finally {
                modbusClient.Disconnect();
            }
        }

    }
}
