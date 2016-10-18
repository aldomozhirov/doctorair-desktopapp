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

        SerialComm serialcomm = new SerialComm();
        Scanner sc = new Scanner(System.in);

        System.out.println("--- Doctor Air console PC application v.1.1 ---");

        //Ask user to select COM port of connected device from the list

        System.out.println("Available COM ports: ");

        Enumeration ports = CommPortIdentifier.getPortIdentifiers();
        while (ports.hasMoreElements()) {
            CommPortIdentifier p = (CommPortIdentifier)ports.nextElement();
            System.out.println(" > " + p.getName());
        }

        try {
            System.out.print("Select device COM port: ");
            String com = sc.nextLine();

            //Connection attempt to specified com port

            serialcomm.connect(com);
            Commander commander = new Commander(new Controller(serialcomm));

            //Command shell loop (work until "exit" command input)

            while (true) {
                System.out.print("Input command: ");
                try {
                    //Wait for input
                    while (!sc.hasNextLine());
                    //Split command on it's name and parameters
                    String[] command = sc.nextLine().split(" ");
                    Class[] paramTypes = new Class[command.length - 1];
                    String[] paramValues = new String[command.length - 1];
                    //Store command parameters
                    for (int i = 0; i < command.length - 1; i++) {
                        paramTypes[i] = String.class;
                        paramValues[i] = command[i + 1];
                    }
                    //Specified command execution
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
