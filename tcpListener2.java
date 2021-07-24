import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

  public class tcpListener2 {
      private ServerSocket serverSocket;
      private Socket clientSocket;
      private PrintWriter out;
      private BufferedReader in;
      String greeting;
      String inputLine;
      String outLine;
      static ArrayList<String> result = new ArrayList<>();
      static ArrayList<String> dataVariables = new ArrayList<>();
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
        while(true){
          // establish a client connection
          try {
            clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
          } catch (Exception e) {
            System.out.println("tcpListener: error opening socket: " + e);
            System.exit(1);
          }
          // process the input line
          boolean lineStatus = processLine(in,out);
          // if the line is oK then close the client connection.
          if (lineStatus){
            try {
              in.close();
              out.close();
              clientSocket.close();
            } catch (Exception e) {
              System.out.println("tcpListener: error closing input stream: " + e);
              System.exit(1);
            }
          }

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

      public static void main(String[] args) {

        // load request and response data into an array
        String fileName = "./data/requestresponse.txt";
        System.out.println("tcpListener: opening file: " + fileName);
        try{
          BufferedReader br = new BufferedReader(new FileReader(fileName));
          while (br.ready()) {
            result.add(br.readLine());
          }
          br.close();
         } catch (Exception e) {
          System.out.println("tcpListener: error opening file: " + fileName + "..." + e);
          e.printStackTrace();
        }

        // load data variables into an array
        fileName = "./data/datavariables.txt";
        System.out.println("tcpListener: opening file: " + fileName);
        try{
          BufferedReader br = new BufferedReader(new FileReader(fileName));
          while (br.ready()) {
            dataVariables.add(br.readLine());
          }
          br.close();
         } catch (Exception e) {
          System.out.println("tcpListener: error opening file: " + fileName + "..." + e);
          e.printStackTrace();
        }

// load the content check descriptor into memory
        fileName = "./data/contentcheck.txt";
        System.out.println("tcpListener: opening file: " + fileName);
        String contentCheckType = null;
        try{
          BufferedReader br = new BufferedReader(new FileReader(fileName));
          String[] contentCheckArray = br.readLine().split(";");
          contentCheckType = contentCheckArray[0];
          String[] contentCheckRules = contentCheckArray[1].split(",");

            contentFirstPos = Integer.parseInt(contentCheckRules[0]);
            contentLastPos = Integer.parseInt(contentCheckRules[1]);
            System.out.println("contentCheckType: " + contentCheckType + " - " + contentFirstPos + " - " + contentLastPos);
            br.close();
         } catch (Exception e) {
          System.out.println("tcpListener: error opening file: " + fileName + "..." + e);
          e.printStackTrace();
        }

        System.out.println("tcp Listener: opening port on 20001.");
          tcpListener server=new tcpListener();
          server.start(20001);
      }

boolean processLine(BufferedReader in,PrintWriter out){

        boolean lineStatus = true;
        int cntr = 0;
        String contentCheck = null;
        String requestResponseLine = null;
        String responseData = null;
        try {
          boolean connectionOpen = true;
          while (connectionOpen){
              inputLine = in.readLine();
              cntr++;
              System.out.println(cntr + ":" + inputLine);
              // at this stage need to check the request response array for correct responses to send
              // extract indentifier from input message
              contentCheck = inputLine.substring(contentFirstPos,contentLastPos);
              //System.out.println("contentCheck: " + contentCheck);
              // loop throught he request response array
              for (int i = 0; i < result.size(); i++) {
                // read and split the current line
                requestResponseLine = result.get(i);
                //System.out.println("requestResponseLine: " + requestResponseLine);
                String[] requestResponseArray = requestResponseLine.split(";");
                // if the contentCheck exists in the line then send it out
                if (requestResponseLine.contains(contentCheck)){
                  // extract the response data from the array
                  responseData = requestResponseArray[1];
                  // check if response line has any variables to be processed
                  if (responseData.contains("%")){
                    responseData = processVariable(responseData, inputLine);
                  }
                  // if the contentCheck responseData matches the end of a sequence then close the loop
                  if (responseData.equals("endOfStream")){
                    connectionOpen = false;
                  } else {
                    // else write the data to open connectionOpen
                    out.println(responseData);
                    cntr++;
                    System.out.println("\t" + cntr + ":" + responseData);
                  }
                }
              }
            }

        } catch (Exception e) {
          System.out.println("tcpListener: error reading input stream: " + e);
        }
          //--
        return lineStatus;
}
String processVariable(String responseData, String inputLine){
  String dataVariableDetails = null;
  String subStringValue = null;
  // extract the variablename from the inptut streem %varname%
  while (true){
    int firstPos = responseData.indexOf("%");
    int lastPos = responseData.indexOf("%",firstPos+1);
    //System.out.println("--------------------------");
    //System.out.println("responseData: " + responseData);
    //System.out.println("firstPos: " + firstPos);
    //System.out.println("lastPos: " + lastPos);
    if (firstPos < 0){
      break;
    }
    String variableName = responseData.substring(firstPos+1,lastPos);
    //System.out.println("variableName: "  +variableName);
    // now we've got the variable name, lets loop though varibale array and find out what type it is
    for (int dvi = 0; dvi < dataVariables.size(); dvi++) {
      dataVariableDetails = dataVariables.get(dvi);
      // if the variable name exists in the current line then process
      if (dataVariableDetails.contains(variableName)){
        // split it into name, type and rules e.g sequenceNumber;substring;7,10
        String[] dataVariableDetailsArray = dataVariableDetails.split(";");
        String variableType = dataVariableDetailsArray[1];
        String variableRules = dataVariableDetailsArray[2];
        //System.out.println("variableType: "  + variableType);
        //System.out.println("variableRules: "  + variableRules);

          if (variableType.equals("substring")){
          // need to get rules for a substring, first and last pos
            String[] variableRulesArray = variableRules.split(",");
            int substringStartPos = Integer.parseInt(variableRulesArray[0]);
            int substringLastPos = Integer.parseInt(variableRulesArray[1]);
            // now use the variables to extrat data from input input stream
            subStringValue = inputLine.substring(substringStartPos, substringLastPos);
            //System.out.println("subStringValue: "  + subStringValue);
          }
          variableName = "%" + variableName + "%";
          responseData = responseData.replaceAll(variableName, subStringValue);
          //System.out.println("responseData: " + responseData);
          //System.out.println("--------------------------");
      }

  }
}
  return responseData;
}

  }
