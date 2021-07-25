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
      static ArrayList<String> result = new ArrayList<>();
      static ArrayList<String> dataVariables = new ArrayList<>();
      static String[] contentCheckRules = null;
      static int contentFirstPos = 0;
      static int contentLastPos = 0;
      static int socketTimeout = 0;
      static int clientTimeout = 0;
      static int threadCount = 0;
      static String ListenerVersion = null;
      static JsonObject configObject = null;
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
                    contentFirstPos,
                    contentLastPos,
                    result,
                    dataVariables,
                    configObject);
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

        // load request and response data into an array
        String fileName = "./data/requestresponse.txt";
        System.out.println("tcpListener: opening file: " + fileName);
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            while (br.ready()) {
                result.add(br.readLine());
            }
            br.close();
        } catch (Exception e) {
            System.out.println("tcpListener: error opening file: " + fileName + "..." + e);
            System.exit(1);
        }

        // load data variables into an array
        fileName = "./data/datavariables.txt";
        System.out.println("tcpListener: opening file: " + fileName);
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            while (br.ready()) {
                dataVariables.add(br.readLine());
            }
            br.close();
        } catch (Exception e) {
            System.out.println("tcpListener: error opening file: " + fileName + "..." + e);
            System.exit(1);
        }

// load the content check descriptor into memory
        fileName = "./data/contentcheck.txt";
        System.out.println("tcpListener: opening file: " + fileName);
        String contentCheckType = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String[] contentCheckArray = br.readLine().split(";");
            contentCheckType = contentCheckArray[0];
            contentCheckRules = contentCheckArray[1].split(",");
            contentFirstPos = Integer.parseInt(contentCheckRules[0]);
            contentLastPos = Integer.parseInt(contentCheckRules[1]);
            System.out.println("tcpListener: contentCheckType: " + contentCheckType +
                    " - Start Position: " + contentFirstPos +
                    " - End Position: " + contentLastPos);
            br.close();
        } catch (Exception e) {
            System.out.println("tcpListener: error opening file: " + fileName + "..." + e);
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
            System.out.println("tcpListener: socketTimeout: " + socketTimeout);
            System.out.println("tcpListener: clientTimeout: " + clientTimeout);
            System.out.println("tcpListener: threadCount: " + threadCount);
        } catch (Exception e) {
            System.out.println("tcpListener: error opening file: " + fileName + "..." + e);
            System.exit(1);
        }
        System.out.println("tcpListener v"+ListenerVersion+": opening port on 20001.");
        tcpListener server = new tcpListener();
        server.start(20001);
    }

}

