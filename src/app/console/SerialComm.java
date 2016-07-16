package app.console;

import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Alexey on 16.07.2016.
 */
public class SerialComm {

    CommPort commPort = null;
    InputStream in = null;
    OutputStream out = null;

    private byte[] buffer = new byte[1024];

    void connect ( String portName ) throws Exception
    {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            commPort = portIdentifier.open(this.getClass().getName(),2000);

            if ( commPort instanceof SerialPort)
            {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);

                in = serialPort.getInputStream();
                out = serialPort.getOutputStream();

            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }

    void disconnect() {
        try {
            in.close();
            out.close();
            commPort.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void send(String s) {
        try {
            out.write(s.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String receive() {
        int data;

        try
        {
            int len = 0;
            while ( ( data = in.read()) > -1 )
            {
                if ( data == '\n' ) {
                    break;
                }
                buffer[len++] = (byte) data;
            }

            return new String(buffer,0,len);

        }
        catch ( IOException e )
        {
            e.printStackTrace();
            System.exit(-1);
        }

        return null;

    }

    void waitData() {
        try {
            while ( in.available() == 0 );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
