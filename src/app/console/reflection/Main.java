package app.console.reflection;

import app.console.SerialComm;
import com.sun.org.apache.xpath.internal.SourceTree;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;

import java.util.*;

/**
 * Created by Alexey on 18.07.2016.
 */
public class Main {

    public static void main(String[] args) {

        System.setProperty("console.encoding","Cp866");
        SerialComm serialcomm = new SerialComm();
        Scanner sc = new Scanner(System.in);

        System.out.println("--- Doctor Air console PC application v.1.1 ---");

        System.out.println("Available COM ports: ");

        Enumeration ports = CommPortIdentifier.getPortIdentifiers();
        while (ports.hasMoreElements()) {
            CommPortIdentifier p = (CommPortIdentifier)ports.nextElement();
            System.out.println(" > " + p.getName());
        }

        try {
            System.out.print("Select device COM port: ");
            String com = sc.nextLine();

            serialcomm.connect(com);
            Commander commander = new Commander(new Controller(serialcomm));

            while (true) {
                System.out.print("Input command: ");
                try {
                    while (!sc.hasNextLine());
                    String[] command = sc.nextLine().split(" ");
                    Class[] paramTypes = new Class[command.length - 1];
                    String[] paramValues = new String[command.length - 1];
                    for (int i = 0; i < command.length - 1; i++) {
                        paramTypes[i] = String.class;
                        paramValues[i] = command[i + 1];
                    }
                    Commander.class.getMethod(command[0], paramTypes).invoke(commander, paramValues);
                } catch (Exception e) {
                    if (e instanceof NoSuchMethodException) {
                        System.out.println("Invalid command!");
                    }
                }
            }

        } catch (Exception e) {
            if (e instanceof NoSuchPortException) {
                System.out.println("Invalid port!");
            }
        }

    }

}
