package app.console;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

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
    String port = null;

    private byte[] buffer = new byte[1024];

    /**
     * Create connection with device by specified port name
     * @param portName
     * @throws Exception
     */
    public void connect ( String portName ) throws Exception
    {
        //Get communication port id by specified port name

        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

        //Check if specified port is busy

        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            //Open port communication

            commPort = portIdentifier.open(this.getClass().getName(),2000);

            //Store connected port name in String format

            port = portName;

            if ( commPort instanceof SerialPort)
            {
                //Convert CommPort to SerialPort

                SerialPort serialPort = (SerialPort) commPort;

                //Set serial port parameters

                serialPort.setSerialPortParams(57600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);

                //Get port communication input/output streams

                in = serialPort.getInputStream();
                out = serialPort.getOutputStream();

            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }

    /**
     * Close current device connection
     */
    public void disconnect() {
        try {
            in.close();
            out.close();
            commPort.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send specified message to the connected device
     * @param message
     */
    public void send(String message) {
        try {
            out.write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Receive input data stream and return it in String format
     * @return
     */
    public String receive() {
        int data;

        try
        {
            int len = 0;
            while ( ( data = in.read()) > -1 )
            {
                /*if ( data == '\n' ) {
                    break;
                }*/

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

    /**
     * Wait for data input stream
     */
    public void waitData() {
        try {
            while ( in.available() == 0 );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get connected port name in String format
     * @return
     */
    public String getPortName() {
        return port;
    }

}
