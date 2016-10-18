package app.console.reflection;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.lang.reflect.Method;

/**
 * Created by Alexey on 18.07.2016.
 */
public class Commander {

    static Controller controller = null;

    public Commander(Controller controller) {
        this.controller = controller;
    }

    @CommandDef(desc = "Prints the current state of lamp and cooler", params = {})
    public static void state() {

        try {

            //Request device state from controller in JSON format

            JSONObject root = controller.getState();
            JSONObject lamp = (JSONObject) root.get("lamp"), cooler = (JSONObject) root.get("cooler");

            //Print to console lamp state in human-readable format

            long lamp_state = (Long) lamp.get("state");
            if (lamp_state == 1) {
                System.out.println("    Lamp is ON");
            } else if (lamp_state == 0) {
                System.out.println("    Lamp is OFF");
            } else
                throw new ParseException(-1);

            //Print to console cooler state in human readable format

            long cooler_state = (Long) cooler.get("state"), cooler_intensivity = (Long) cooler.get("intensivity");
            if (lamp_state == 1) {
                System.out.println("    Cooler is ON with " + cooler_intensivity + "% intensivity");
            } else if (lamp_state == 0) {
                System.out.println("    Cooler is OFF");
            } else
                throw new ParseException(-1);

            //Print to console current lamp count and mode in human readable format

            System.out.println("    Lamp count: " + lamp.get("count"));
            System.out.println("    Mode: " + root.get("mode"));

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @CommandDef(desc = "Prints the current on board sensor values", params = {})
    public static void sensors() {

        //Request sensors values from controller in JSON format

        JSONObject root = controller.getSensorsValues();

        //Print sensors values to console in human readable format

        for (Object sensor : root.entrySet()) {
            System.out.println("    " + sensor);
        }

    }

    @CommandDef(desc = "Lists all available user commands", params = {})
    public static void list() {

        //Go throw all declared methods in this class using reflexion

        for (Method m : Commander.class.getDeclaredMethods()) {
            CommandDef serviceDef = m.getAnnotation(CommandDef.class);
            String descOfMethod = null;
            String[] params = null;

            //In case if method annotation exists store it

            if (serviceDef != null) {
                descOfMethod = serviceDef.desc();
                params = serviceDef.params();
            }

            //Print method name

            System.out.print("    " + m.getName());

            //Print method parameters

            for (String p : params) {
                System.out.print(" " + p);
            }

            //Print description of method

            System.out.println(" | " + descOfMethod);
        }
    }

    @CommandDef(desc = "Finish work of the application", params = {})
    public static void exit() {

        //Close device connection with software serial controller

        controller.close();

        //Close the application

        System.exit(0);
    }

    @CommandDef(desc = "Prints firmware version of device", params = {})
    public static void version() {

        //Request device firmware version from controller and print it

        System.out.println("    Device firmware version: " + controller.getVersion());

    }

    @CommandDef(desc = "Prints all device logs starting from launching the system", params = {})
    public static void log() {

        //Request device system logs from controller and print it

        System.out.println(controller.getLogs());
    }

    @CommandDef(desc = "Resets the counter of lamp", params = {})
    public static void resetlamp() {

        //Call controller method to request lamp counter reset and print result of the operation to the console

        if (controller.resetLampCounter()) {
            System.out.println("    Lamp counter successfully reset");
        }
        else {
            System.out.println("    Lamp counter reset fail!");
        }
    }

    @CommandDef(desc = "Connect to inputed wifi access point", params = {"\"ssid\"", "\"password\""})
    public static void wifiap(String ssid, String password)
    {
        //Validate ssid and password parameters
        if (ssid.startsWith("\"") && ssid.endsWith("\"") && password.startsWith("\"") && password.endsWith("\"")) {
            System.out.println("    Connection attempt to " + ssid + "...");
            //Call controller method to connect it to specified AP and print result of the operation
            if (controller.connectWiFiAP(ssid, password)) {
                System.out.println("    Successful connection");
            }
            else {
                System.out.println("    Connection failed");
            }
        }
        else {
            System.out.println("    Input error!");
        }
    }

    @CommandDef(desc = "Prints state of connection device with internet", params = {})
    public static void connection() {
        //Request connection state in boolean format and print it in human readable format
        if (controller.checkConnection()) {
            System.out.println("    Device is online");
        }
        else {
            System.out.println("    Device is offline");
        }
    }

    @CommandDef(desc = "Prints list of all avaliable firmware versions", params = {})
    public static void listfwversions() {
        try {

            //Request firmware info from server in JSON format

            JSONObject fwinfo = Server.getFirmwareInfo();

            //Print list of all versions to the console

            for (Object v : fwinfo.keySet().toArray()) {
                System.out.println("    " + v);
            }

        } catch (Exception e) {
            System.out.println("    Cannot get a list of versions from server!");
        }
    }

    @CommandDef(desc = "Upload inputed version of firmware to device", params = {"version"})
    public static void uploadfw(String version) {
        try {
            //Request firmware info from server in JSON format
            JSONObject fwinfo = Server.getFirmwareInfo();
            //Check if specified in parameters version exists
            if (fwinfo.containsKey(version)) {
                //Generate url string to download firmware
                String url = "https://" + fwinfo.get(version);
                try {
                    //Download .hex firmware file from server to computer in the application root directory

                    Server.downloadFile(url, version + ".hex", 1000);
                    System.out.println("    Downloading firmware from server...");
                    try {
                        //Upload downloaded firmware to connected device
                        controller.uploadFirmware(version + ".hex");
                        System.out.println("    Uploading firmware on device...");
                        System.out.println("    Firmware version " + version + " successfully uploaded!");
                    } catch (Exception e) {
                        System.out.println("    Error while uploading firmware on device!");
                    }
                } catch (Exception e) {
                    System.out.println("    Error while downloading firmware from server!");
                }
            } else {
                System.out.println("    Incorrect version!");
            }
        } catch (Exception e) {
            System.out.println("    Cannot check firmware version!");
        }
    }

    @CommandDef(desc = "Update the system's firmware to the latest version", params = {})
    public static void updatefw() {

        try {
            //Request firmware info from server in JSON format and get array of versions from it (versions key set sorted on server)

            Object[] versions = Server.getFirmwareInfo().keySet().toArray();

            //Find the highest version (highest version - last element according to array sorted)

            String last_ver = (String)versions[versions.length - 1];

            //Request current device firmware version

            String cur_ver = controller.getVersion();

            //Compare current device firmware version with the highest and upload new version to device if it is lower

            if(last_ver.compareTo(cur_ver) != 0) {
                System.out.println("    Current device version is " + cur_ver + ". The latest version " + last_ver + " to be uploaded...");
                uploadfw(last_ver);
            }
            else {
                System.out.println("    Current version " + cur_ver + " of your device is up to date!");
            }
        }
        catch (Exception e) {
            System.out.println("    Cannot check available firmware versions!");
            System.out.println(e);
        }

    }

    @CommandDef(desc = "Connect new cloud control device", params = {"token"})
    public static void device(String token) {
        try {

            //Request new control device connection by specified token and id of connected device
            Server.addDevice("1", token);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
