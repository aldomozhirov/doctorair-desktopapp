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

        comm.send("SST");
        comm.waitData();
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

        comm.send("SEN");
        comm.waitData();
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
        comm.send("VER");
        comm.waitData();
        return comm.receive();
    }

    /**
     * Requests last device system logs and returns it
     * @return String
     */
    public String getLogs() {
        comm.send("LOG");
        comm.waitData();
        return comm.receive();
    }

    /**
     * Resets lamp counter of connected device. Returns result of operation
     * @return boolean
     */
    public boolean resetLampCounter() {
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
        comm.send("WAP " + ssid + password);
        comm.waitData();
        return comm.receive().compareTo("SUCCESS") == 0;
    }

    /**
     * Requests current device internet connection and returns it
     * @return boolean
     */
    public boolean checkConnection() {
        comm.send("CON");
        comm.waitData();
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

        Runtime r = Runtime.getRuntime();
        Process p = null;

        String command = "avrdude.exe -C avrdude.conf -p m2560 -c wiring -P "+ comm.getPortName() + " -b 115200 -D -U flash:w:\"" + path +"\":i";
        comm.disconnect();
        p = r.exec(command);
        p.waitFor();
        comm.connect(comm.getPortName());

        return p.exitValue() == 0;

    }

}
