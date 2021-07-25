import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
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

    public void start(int port) {
        // open the port
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(10000);
        } catch (Exception e) {
            System.out.println("tcpListener: error opening port: " + e);
            System.exit(1);
        }
        ExecutorService executor = Executors.newFixedThreadPool(100);
        while (true) {
            // establish a client connection
            try {
                clientSocket = serverSocket.accept();
                // set a timeout so threads get cleared automatically
                clientSocket.setSoTimeout(5 * 1000);
            } catch (Exception e) {
                System.out.println("tcpListener: error opening socket: " + e);
                System.exit(1);
            }
            // for each input connection start a thread to deal with it
            Runnable tcpWorker;
            tcpWorker = new tcpWorker(clientSocket,
                    contentFirstPos,
                    contentLastPos,
                    result,
                    dataVariables);
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

        System.out.println("tcpListener: opening port on 20001.");
        tcpListener server = new tcpListener();
        server.start(20001);
    }

}
