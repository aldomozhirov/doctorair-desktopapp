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

        comm.send("state");
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

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @CommandDef(desc = "Prints the current on board sensor values", params = {""})
    public static void sensors() {

        comm.send("sensors");
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
            String command = "avrdude.exe -C avrdude.conf -p m328p -c arduino -P "+ comm.getPortName() + " -b 115200 -D -U flash:w:\"update.ino.standard.hex\":i";
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

}
