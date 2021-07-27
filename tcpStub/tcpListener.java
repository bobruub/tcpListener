package tcpStub;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.json.*;

  public class tcpListener {
      private ServerSocket serverSocket;
      private Socket clientSocket;
      private PrintWriter out;
      private BufferedReader in;
      String greeting;
      String inputLine;
      String outLine;
      static ArrayList<String> incrementNumberArray = new ArrayList<>();
      static ArrayList<String> dataVariables = new ArrayList<>();
      static String[] contentCheckRules = null;
      static int contentFirstPos = 0;
      static int contentLastPos = 0;
      static int socketTimeout = 0;
      static int clientTimeout = 0;
      static int threadCount = 0;
      static int port = 0;
      static String ListenerVersion = null;
      static JsonObject configObject = null;
      static JsonObject requestResponseObject = null;
      static JsonObject dataVariableObject = null;  
    
      public void start(int port) {
        // open the port
        try {
            serverSocket = new ServerSocket(port);
            if (socketTimeout > 0) {                                                // set a timeout for socket connections from config.json a socketTimeout of 0 is forever.
                serverSocket.setSoTimeout(socketTimeout);
            }
        } catch (Exception e) {
            System.out.println("tcpListener: error opening port: " + e);
            System.exit(1);
        }
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);        // setup a thread pool for sockets
        while (true) {
            try {
                // establish a client connection
                clientSocket = serverSocket.accept();
                if (clientTimeout > 0) {                                            // set a timeout so threads get cleared automatically from config.json a clientTimeout of 0 is forever.
                    clientSocket.setSoTimeout(clientTimeout);
                }
            } catch (Exception e) {
                System.out.println("tcpListener: error opening socket: " + e);
                System.exit(1);
            }
            // for each input connection start a thread to deal with it
            Runnable tcpWorker = new tcpWorker(clientSocket,
                                               configObject,
                                               dataVariableObject,
                                               requestResponseObject,
                                               incrementNumberArray);
            executor.execute(tcpWorker);
        }

    }

    public void stop() {
        try {
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
        } catch (Exception e) {
            System.out.println("tcpListener: error closing input stream: " + e);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {

// load request and responses in json file.
        String fileName = "./data/requestresponse.json";
        System.out.println("tcpListener: opening file: " + fileName);
        try {
            InputStream fis = new FileInputStream(fileName);
            JsonReader reader = Json.createReader(fis);
            requestResponseObject = reader.readObject();
            JsonArray  responseArray = (JsonArray) requestResponseObject.get("response");
            reader.close();
            fis.close();
        } catch (Exception e) {
            System.out.println("tcpListener: error processing file: " + fileName + "..." + e);
            System.exit(1);
        }
        // load data variables json file
        fileName = "./data/datavariables.json";
        System.out.println("tcpListener: opening file: " + fileName);
        try {
            InputStream fis = new FileInputStream(fileName);
            JsonReader reader = Json.createReader(fis);
            dataVariableObject = reader.readObject();
            // load default incremented number values in an array.
            JsonArray variableArray = (JsonArray) dataVariableObject.get("variable");
            for (int variableCntr = 0; variableCntr < variableArray.size(); variableCntr++) {
                String arrayVariableName = variableArray.getJsonObject(variableCntr).getString("name");
                String arrayVariableType = variableArray.getJsonObject(variableCntr).getString("type");
                if (arrayVariableType.equals("IncrementNumber")){
                    JsonArray formatArray = (JsonArray) variableArray.getJsonObject(variableCntr).get("format");
                    String incrementNumberStartValue = formatArray.getJsonObject(0).getString("default");
                    incrementNumberArray.add(arrayVariableName + ":" + incrementNumberStartValue);
                }
            }
            reader.close();
            fis.close();
        } catch (Exception e) {
            System.out.println("tcpListener: error processing file: " + fileName + "... " + e);
            System.exit(1);
        }
        // load cofig json file
        fileName = "./data/config.json";
        System.out.println("tcpListener: opening file: " + fileName);
        try {
            InputStream fis = new FileInputStream(fileName);
            JsonReader reader = Json.createReader(fis);
            configObject = reader.readObject();
            reader.close();
            fis.close();
            ListenerVersion = configObject.getString("ListenerVersion");
            socketTimeout = configObject.getInt("socketTimeout");
            clientTimeout = configObject.getInt("clientTimeout");
            threadCount = configObject.getInt("threadCount");
            port = configObject.getInt("port");
            // get the config check type and format
            JsonArray configArray = (JsonArray) configObject.get("configCheck");
            String contentCheckType = configArray.getJsonObject(0).getString("type");
            contentFirstPos = configArray.getJsonObject(0).getInt("startPos");
            contentLastPos = configArray.getJsonObject(0).getInt("endPos");
           
            System.out.println("tcpListener: contentCheckType: " + contentCheckType +
                    " - Start Position: " + contentFirstPos +
                    " - End Position: " + contentLastPos);
            System.out.println("tcpListener: socketTimeout: " + socketTimeout);
            System.out.println("tcpListener: clientTimeout: " + clientTimeout);
            System.out.println("tcpListener: threadCount: " + threadCount);
            
        } catch (Exception e) {
            System.out.println("tcpListener: error processing file: " + fileName + "..." + e);
            System.exit(1);
        }
        

        System.out.println("tcpListener: v"+ListenerVersion+": opening port on " + port + ".");
        tcpListener server = new tcpListener();
        server.start(port);
    }

}

