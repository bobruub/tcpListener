  import java.net.*;
  import java.io.*;
  import java.util.ArrayList;

  public class tcpListener {
      private ServerSocket serverSocket;
      private Socket clientSocket;
      private PrintWriter out;
      private BufferedReader in;
      String greeting;
      String inputLine;
      String outLine;
      static ArrayList<String> result = new ArrayList<>();
      public void start(int port) {
        try {
          serverSocket = new ServerSocket(port);
          clientSocket = serverSocket.accept();
        } catch (Exception e) {
          System.out.println("tcpListener: error opening socket: " + e);
          //Be.printStackTrace();
        }

        try {
          out = new PrintWriter(clientSocket.getOutputStream(), true);
          in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
          System.out.println("tcpListener: error with input stream: " + e);
          e.printStackTrace();
        }
        int cntr = 0;
        String contentCheck = null;
        String requestResponseLine = null;
        try {

          boolean connectionOpen = true;
          while (connectionOpen){
              // Read the line from the client
              inputLine = in.readLine();
              cntr++;
              System.out.println(cntr + ":" + inputLine);
              // at this stage need to check the request response array for correct responses to send
              // setp 1 - extract indentifier from input message
              contentCheck = inputLine.substring(0,7);
              System.out.println("contentCheck:" + contentCheck);
              // loop throught he request response array
                for (int i = 0; i < result.size(); i++) {
                  // read and split the current line
                  requestResponseLine = result.get(i);
                  String[] requestResponseArray = requestResponseLine.split(";");
                  System.out.println("requestResponseLine:" + requestResponseLine);
                  // if the contentCheck exists int he line then send it out
                  if (requestResponseLine.contains(contentCheck)){
                      String responseData = requestResponseArray[1];
                      System.out.println("responseData:" + responseData);
                      // if the contentCheck responseData matches the end of a sequence then close the loop
                      // else write the data to open connectionOpen
                      if (responseData.equals("endOfStream")){
                          connectionOpen = false;
                      } else {
                          out.println(responseData);
                          System.out.println(cntr + ":" + responseData);
                      }
                  }
                }
              }

          //outLine = "ALTBA05C1F.....h015641001402707ITSP05.2501072";
          //out.println(outLine);
          //cntr++;
          //System.out.println(cntr + ":" + outLine);

          //outLine = "PCTBA08C1F..... 015641000002603";
          //out.println(outLine);
          //cntr++;
          //System.out.println(cntr + ":" + outLine);

          // line 4 - read line
          /* inputLine = in.readLine();
          cntr++;
          System.out.println(cntr + ":" + inputLine);
          // line 5 - write
          outLine = "DCTBA08C1F..... 015641035702606HS01102014212000000000050601020304050612WINaPAANNNNNPa1Pda1QTeDSHWaPAANNNNNPa1Pda1QTReDQU aPAANNNNNPPPPPa1Pda1QWeDEX aPAANNNNNPPPPPa1Pda1PXReDTRIaPAANNNNNPPPPPa1Pda1bDSPRaSAANNNNNPPPPPa1Pda1RReDDD aPAANNNNNPPPPPa1Pda1bDDD1aPAANNNNNPPPPPa1Pda1bDOMNaPAANNNNNPPPPPa1Pda1QTeDEQDaCAANNNNNPPPPPa1Pda1QTReDP04aCAANNNNNPPPPPa1Pda1bDP06aCAANNNNNPPPPPa1Pda1ReD3563302";
          cntr++;
          System.out.println(cntr + ":" + outLine);
          out.println(outLine);

          // line 6 - read line
          inputLine = in.readLine();
          cntr++;
          System.out.println(cntr + ":" + inputLine);
          // line 7 - write
          outLine = "RCTBA14C1F..... 015641000002602";
          cntr++;
          System.out.println(cntr + ":" + outLine);
          out.println(outLine);

          // line 8 - read line
          inputLine = in.readLine();
          cntr++;
          System.out.println(cntr + ":" + inputLine);
          // line 9 - write
          outLine = "ACTBA14C1F..... 015641000002561";
          cntr++;
          System.out.println(cntr + ":" + outLine);
          out.println(outLine);

          // line 10 - read line
          inputLine = in.readLine();
          cntr++;
          System.out.println(cntr + ":" + inputLine);
          // line 11 - write
          outLine = "TBQ20C1F01... 015239000002626";
          cntr++;
                  System.out.println(cntr + ":" + outLine);
          out.println(outLine);

          // line 12 - read line
          inputLine = in.readLine();
          cntr++;
          System.out.println(cntr + ":" + inputLine);
          // line 13 - write
          outLine = "TBQ26C1F01... 015239011302644C......P....10WINCe..SHWCe..EX Ce..QU Ce..OMNCe..TRICe..SPRCe..DD C02e..EQDC020304e..P06C0203040506e....000014754";
          cntr++;
          System.out.println(cntr + ":" + outLine);
          out.println(outLine);

          // line 14 - read line
          inputLine = in.readLine();
          cntr++;
          System.out.println(cntr + ":" + inputLine);
          // line 15 - write
          outLine = "DLTBA92C1F.....e015643001402717ITSP05.2501072";
          cntr++;
          System.out.println(cntr + ":" + outLine);
          out.println(outLine);

          // line 16 - read line
          inputLine = in.readLine();
          cntr++;
          System.out.println(cntr + ":" + inputLine);*/

        } catch (Exception e) {
          System.out.println("tcpListener: error reading input stream: " + e);
          e.printStackTrace();
        }
        System.out.println("tcpListener: Input message: " + inputLine);
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

        String fileName = "./data/requestresponse.txt";
        System.out.println("tcpListener: opening file: " + fileName);

        try{

          BufferedReader br = new BufferedReader(new FileReader(fileName));
          while (br.ready()) {
            result.add(br.readLine());
          }

         } catch (Exception e) {
          System.out.println("tcpListener: error opening file: " + fileName + "..." + e);
          e.printStackTrace();
        }

         for (int i = 0; i < result.size(); i++) {

              // accessing each element of array
              System.out.println(result.get(i));
          }
        System.out.println("tcp Listener: opening port on 20001.");
          tcpListener server=new tcpListener();
          server.start(20001);
      }
  }
