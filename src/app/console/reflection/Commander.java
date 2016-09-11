package app.console.reflection;

import app.console.SerialComm;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.lang.reflect.Method;

/**
 * Created by Alexey on 18.07.2016.
 */
public class Commander {

    static JSONParser parser = new JSONParser();
    static SerialComm comm = null;

    public Commander(SerialComm comm) {
        this.comm = comm;
    }

    @CommandDef(desc = "Prints the current state of lamp and cooler", params = {""})
    public static void state() {

        comm.send("SST");
        comm.waitData();
        String json = comm.receive();

        try {
            JSONObject root = (JSONObject) parser.parse(json);
            JSONObject lamp = (JSONObject) root.get("lamp"), cooler = (JSONObject) root.get("cooler");

            long lamp_state = (Long) lamp.get("state");
            if (lamp_state == 1) {
                System.out.println("    Lamp is ON");
            }
            else if (lamp_state == 0) {
                System.out.println("    Lamp is OFF");
            }
            else
                throw new ParseException(-1);

            long cooler_state = (Long) cooler.get("state"), cooler_intensivity = (Long) cooler.get("intensivity");
            if (lamp_state == 1) {
                System.out.println("    Cooler is ON with " + cooler_intensivity + "% intensivity");
            }
            else if (lamp_state == 0) {
                System.out.println("    Cooler is OFF");
            }
            else
                throw new ParseException(-1);

            System.out.println("    Lamp count: " + lamp.get("count"));
            System.out.println("    Mode: " + root.get("mode"));

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @CommandDef(desc = "Prints the current on board sensor values", params = {""})
    public static void sensors() {

        comm.send("SEN");
        comm.waitData();
        String json = comm.receive();

        try {
            JSONObject root = (JSONObject) parser.parse(json);
            for (Object sensor : root.entrySet()) {
                System.out.println("    " + sensor);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @CommandDef(desc = "Update the system's firmware to the latest version", params = {""})
    public static void update() {
        Runtime r = Runtime.getRuntime();
        Process p = null;

        try {
            String command = "avrdude.exe -C avrdude.conf -p m2560 -c wiring -P "+ comm.getPortName() + " -b 115200 -D -U flash:w:\"doctorair-firmware-ver-1.1.hex\":i";
            comm.disconnect();
            p = r.exec(command);
            p.waitFor();
            comm.connect(comm.getPortName());
        }
        catch (Exception e) {
            System.out.println(e);
        }

        if (p.exitValue() == 0)
            System.out.println("    Successful update");
        else
            System.out.println("    Update error!");
    }

    @CommandDef(desc = "Lists all available user commands", params = {""})
    public static void list() {
        for (Method m : Commander.class.getDeclaredMethods()) {
            CommandDef serviceDef = m.getAnnotation(CommandDef.class);
            String descOfMethod = null;
            if (serviceDef != null) {
                descOfMethod = serviceDef.desc();
            }
            System.out.println("    " + m.getName() + " | " + descOfMethod);
        }
    }

    @CommandDef(desc = "Finish work of the application", params = {""})
    public static void exit() {
        comm.disconnect();
        System.exit(0);
    }

    @CommandDef(desc = "Prints firmware version of device", params = {""})
    public static void version() {
        comm.send("VER");
        comm.waitData();
        System.out.println("    Device firmware version: " + comm.receive());
    }

    @CommandDef(desc = "Prints all device logs starting from launching the system", params = {""})
    public static void log() {
        comm.send("LOG");
        comm.waitData();
        System.out.println(comm.receive());
    }

    @CommandDef(desc = "Resets the counter of lamp", params = {""})
    public static void resetlamp() {
        comm.send("RLC");
    }

    @CommandDef(desc = "Connect to inputed wifi access point", params = {"ssid", "password"})
    public static void wifiap(String ssid, String password)
    {
        comm.send("WAP " + "\"" + ssid + "\" \"" + password + "\"");
        comm.waitData();
        System.out.println(comm.receive());
    }

}
