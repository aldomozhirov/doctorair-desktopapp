package app.console.reflection;

import app.console.SerialComm;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Created by Alexey on 13.09.2016.
 */
public class Controller {

    static JSONParser parser = new JSONParser();
    static SerialComm comm = null;

    /**
     * Creates new USB device serial connection
     * @param comm
     */
    public Controller (SerialComm comm) {
        this.comm = comm;
    }

    /**
     * Requests current device state and returns it in JSON format
     * @return JSONObject
     */
    public JSONObject getState() {

        JSONObject root = null;

        //Sending SST command to connected device (see recirculator communication protocol specification) and waiting for data

        comm.send("SST");
        comm.waitData();

        //Parsing received device state information in JSON format

        String json = comm.receive();

        try {
            root = (JSONObject) parser.parse(json);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return root;
    }

    /**
     * Requests current device sensors values and returns it in JSON format
     * @return JSONObject
     */
    public JSONObject getSensorsValues() {

        JSONObject root = null;

        //Sending SEN command to connected device (see recirculator communication protocol specification) and waiting for data

        comm.send("SEN");
        comm.waitData();

        //Parsing received sensors values in JSON format

        String json = comm.receive();

        try {
            root = (JSONObject) parser.parse(json);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return root;
    }

    /**
     * Requests current device firmware version and returns it
     * @return String
     */
    public String getVersion() {

        //Sending VER command to connected device (see recirculator communication protocol specification) and waiting for data

        comm.send("VER");
        comm.waitData();

        //Return received device firmware version

        return comm.receive();
    }

    /**
     * Requests last device system logs and returns it
     * @return String
     */
    public String getLogs() {

        //Sending LOG command to connected device (see recirculator communication protocol specification) and waiting for data

        comm.send("LOG");
        comm.waitData();

        //Return received device firmware version

        return comm.receive();
    }

    /**
     * Resets lamp counter of connected device. Returns result of operation
     * @return boolean
     */
    public boolean resetLampCounter() {

        //Sending RLC command to connected device (see recirculator communication protocol specification)

        comm.send("RLC");
        return true;
    }

    /**
     * Connects device to specified wifi AP. Returns result of connection
     * @param ssid
     * @param password
     * @return boolean
     */
    public boolean connectWiFiAP(String ssid, String password) {

        //Sending WAP "ssid" "password" command to connected device (see recirculator communication protocol specification) and waiting for data

        comm.send("WAP " + ssid + password);
        comm.waitData();

        //Receive result of connection and return it in boolean format

        return comm.receive().compareTo("SUCCESS") == 0;
    }

    /**
     * Requests current device internet connection and returns it
     * @return boolean
     */
    public boolean checkConnection() {

        //Sending CON command to connected device (see recirculator communication protocol specification) and waiting for data

        comm.send("CON");
        comm.waitData();

        //Receive request result and return boolean true if result is "ONLINE" and false in other cases

        return comm.receive() == "ONLINE";
    }

    /**
     * Disconnects connected device
     */
    public void close() {
        comm.disconnect();
    }

    /**
     * Upload firmware in .hex format from specified location path to the connected device. Returns result of operation
     * @param path
     * @return boolean
     * @throws Exception
     */
    public boolean uploadFirmware(String path) throws Exception {

        //Creating new process to launch avrdude

        Runtime r = Runtime.getRuntime();
        Process p = null;

        String command = "avrdude.exe -C avrdude.conf -p m2560 -c wiring -P "+ comm.getPortName() + " -b 115200 -D -U flash:w:\"" + path +"\":i";

        //Disconnect connected device to make available for avrdude

        comm.disconnect();

        //Executing avrdude command to upload firmware to the connected device

        p = r.exec(command);
        p.waitFor();

        //Connect device to program back

        comm.connect(comm.getPortName());

        //Return result of avrdude process execution

        return p.exitValue() == 0;

    }

}
