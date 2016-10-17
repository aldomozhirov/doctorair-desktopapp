package app.console.reflection;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Alexey on 13.09.2016.
 */
public class Server {

    static JSONParser parser = new JSONParser();

    /**
     * Downloads file from specified URL to specified location path on computer
     * @param strURL
     * @param strPath
     * @param buffSize
     * @throws Exception
     */
    public static void downloadFile(String strURL, String strPath, int buffSize) throws Exception {

        //Open new URL connection by HTTP GET request

        URL connection = new URL(strURL);
        HttpURLConnection urlconn;
        urlconn = (HttpURLConnection) connection.openConnection();
        urlconn.setRequestMethod("GET");
        urlconn.connect();

        //Read response data in buffer

        InputStream in = null;
        in = urlconn.getInputStream();
        OutputStream writer = new FileOutputStream(strPath);
        byte buffer[] = new byte[buffSize];
        int c = in.read(buffer);
        while (c > 0) {
           writer.write(buffer, 0, c);
           c = in.read(buffer);
        }

        //Close connection

        writer.flush();
        writer.close();
        in.close();

    }

    /**
     * Queries information about all existing firmware versions and returns it in JSON format
     * @return JSONObject
     * @throws Exception
     */
    public static JSONObject getFirmwareInfo() throws Exception {

        JSONObject root = null;

        //HTTP GET request to the server with /firmware_versions command

        URL connection = new URL("https://doctorair.tk/firmware_versions");
        HttpURLConnection urlconn;
        urlconn = (HttpURLConnection) connection.openConnection();
        urlconn.setRequestMethod("GET");
        urlconn.connect();

        //Reading and parsing JSON string from server response, which contains versions info

        BufferedReader br = new BufferedReader(new InputStreamReader(urlconn.getInputStream()));
        String json = br.readLine();
        br.close();
        root = (JSONObject) parser.parse(json);

        return root;
    }

    /**
     * Add new control device to recirculator by specified id
     * @param id
     * @param token
     * @throws Exception
     */
    public static void addDevice(String id, String token)  throws Exception {

        //HTTP GET request to the server to register new connection between recirculator specified by id and control device specified by token

        URL connection = new URL("https://doctorair.tk/finishnewconnection/" + token + "_" + id);
        HttpURLConnection urlconn;
        urlconn = (HttpURLConnection) connection.openConnection();
        urlconn.setRequestMethod("GET");
        urlconn.connect();
    }

}
