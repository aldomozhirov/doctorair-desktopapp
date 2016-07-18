package app.console.reflection;

import app.console.SerialComm;
import com.sun.org.apache.xpath.internal.SourceTree;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;

import java.util.Enumeration;
import java.util.Scanner;

/**
 * Created by Alexey on 18.07.2016.
 */
public class Main {

    public static void main(String[] args) {

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
            String com = sc.next();

            serialcomm.connect(com);
            Commander commander = new Commander(serialcomm);

            while (true) {
                System.out.print("Input command: ");
                try {
                    Commander.class.getMethod(sc.next()).invoke(commander, new Object[]{});
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
