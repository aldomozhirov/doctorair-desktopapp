package app.console;
import gnu.io.CommPortIdentifier;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Enumeration;
import java.util.Scanner;

/**
 * Created by Alexey on 16.07.2016.
 */
public class Main {

    static JSONParser parser = new JSONParser();

    public static void main(String[] args) {

        SerialComm comm = new SerialComm();
        Scanner sc = new Scanner(System.in);

        System.out.println("Available COM ports: ");

        Enumeration ports = CommPortIdentifier.getPortIdentifiers();
        while (ports.hasMoreElements()) {
            CommPortIdentifier p = (CommPortIdentifier)ports.nextElement();
            System.out.println(" > " + p.getName());
        }

        try {

            System.out.print("Select device COM port: ");
            String com = sc.next();

            comm.connect(com);

            while (true) {

                System.out.print("Input command: ");
                switch (sc.next()) {
                    case "state":
                        comm.send("state");
                        comm.waitData();
                        printState(comm.receive());
                        break;
                    case "sensors":
                        comm.send("sensors");
                        comm.waitData();
                        printSensors(comm.receive());
                        break;
                    case "update":
                        comm.disconnect();
                        if (uploadBin("m328", com, "9600", "update.ino.standard.hex") == 0)
                            System.out.println("    Successful update");
                        else
                            System.out.println("    Update error!");
                        comm.connect(com);
                        break;
                    case "exit":
                        System.exit(0);
                        break;
                    default:
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static void printState(String json) {

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

    static void printSensors(String json) {

        try {
            JSONObject root = (JSONObject) parser.parse(json);
            for (Object sensor : root.entrySet()) {
                System.out.println("    " + sensor);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    static int uploadBin(String type, String port, String baud, String hex_path) {
        Runtime r = Runtime.getRuntime();
        Process p = null;

        try {
            String command = "avrdude.exe -C avrdude.conf -p " + type +
                    " -c wiring -P " +  port + " -b " + baud + " -D -U flash:w:\"" + hex_path + "\":i";
            System.out.println(command);
            p = r.exec(command);
            p.waitFor();
        }
        catch (Exception e) {
            System.out.println(e);
        }

        return p.exitValue();

    }

}
