package tcpStub;

import javax.json.JsonObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import javax.json.*;

public class tcpWorker implements Runnable {

    private final Socket clientSocket;
    private final ArrayList<String> result;
    private final ArrayList<String> dataVariables;
    private final JsonObject configObject;
    private final int contentFirstPos;
    private final int contentLastPos;

    public tcpWorker(Socket clientSocket,
                     int contentFirstPos, int contentLastPos,
                     ArrayList<String> result, ArrayList<String> dataVariables,
                     JsonObject configObject) {

        this.clientSocket = clientSocket;
        this.contentLastPos = contentLastPos;
        this.contentFirstPos = contentFirstPos;
        this.result = result;
        this.dataVariables = dataVariables;
        this.configObject = configObject;

    }
    @Override
    public void run() {
        PrintWriter out = null;
        BufferedReader in = null;
        // create an input and output socket
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
            System.out.println("tcpWorker: error opening socket: " + e);
            System.exit(1);
        }
        boolean lineStatus = true;
        int cntr = 0;
        String contentCheck = null;
        String requestResponseLine = null;
        String responseData = null;
        try {
            boolean connectionOpen = true;
            while (connectionOpen) {
                String inputLine;
                inputLine = in.readLine();                                              // read input line from socket
                cntr++;
                System.out.println(cntr + ":" + inputLine);
                contentCheck = inputLine.substring(contentFirstPos, contentLastPos);    // extract contentCheck indentifier from input message
                // at this stage need to check the request response array for correct responses to send based on the contentCheck
                for (int i = 0; i < result.size(); i++) {
                    // read and split the current line, into a check value [0] and a response message [1]
                    requestResponseLine = result.get(i);
                    String[] requestResponseArray = requestResponseLine.split(";");
                    String checkValue = requestResponseArray[0];
                    if (contentCheck.equals(checkValue)){                               // if the contentCheck exists in the line then send it out
//                    if (requestResponseLine.contains(contentCheck)) {
                        responseData = requestResponseArray[1];                         // extract the response data from the array
                        if (responseData.contains("%")) {                               // check if response line has any variables to be processed
                            responseData = processVariable(responseData, inputLine);
                        }
                        // if the contentCheck response message matches the end of a sequence then close the loop
                        // its a hack but only way I can think of to know a sequence of data is complete.
                        if (responseData.equals("endOfStream")) {
                            connectionOpen = false;
                        } else {
                            out.println(responseData);                                  // write the data to open connectionOpen
                            cntr++;
                            System.out.println("\t" + cntr + ":" + responseData);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("tcpWorker: error reading input stream: " + e);
        }
        // close all resources
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (Exception e) {
            System.out.println("tcpWorker: error closing resources: " + e);
        }
    }
    String processVariable(String responseData, String inputLine) {
        String dataVariableDetails = null;
        String subStringValue = null;
        // extract the variablename from the inptut stream %varname%
        while (true) {
            int firstPos = responseData.indexOf("%");
            int lastPos = responseData.indexOf("%", firstPos + 1);
            // if indexof finds no more messages then stop processing.
            if (firstPos < 0) {
                break;
            }
            String variableName = responseData.substring(firstPos + 1, lastPos);
            // now we've got the variable name, lets loop though variable array and find out what type it is
            for (int dvi = 0; dvi < dataVariables.size(); dvi++) {
                dataVariableDetails = dataVariables.get(dvi);
                // if the variable name exists in the current line then process
                if (dataVariableDetails.contains(variableName)) {
                    // split it into name, type and rules e.g sequenceNumber;substring;7,10
                    String[] dataVariableDetailsArray = dataVariableDetails.split(";");
                    String variableType = dataVariableDetailsArray[1];
                    String variableRules = dataVariableDetailsArray[2];
                    if (variableType.equals("substring")) {
                        // need to get rules for a substring, first and last pos
                        String[] variableRulesArray = variableRules.split(",");
                        int substringStartPos = Integer.parseInt(variableRulesArray[0]);
                        int substringLastPos = Integer.parseInt(variableRulesArray[1]);
                        // now use the variables to extract data from input input stream
                        subStringValue = inputLine.substring(substringStartPos, substringLastPos);
                    }
                    if (variableType.equals("randomNumber")) {
                        // for randomNumber varibale rules are, 1. min, 2. max and 3. printf format
                        String[] variableRulesArray = variableRules.split(",");
                        int min = Integer.parseInt(variableRulesArray[0]);
                        int max = Integer.parseInt(variableRulesArray[1]);
                        String format = variableRulesArray[2];
                        int random_int = (int)Math.floor(Math.random()*(max-min+1)+min);
                        subStringValue = String.format("%04d", random_int);  // 0009
                    }
                    // now replace the tag in the response message with the generated value
                    variableName = "%" + variableName + "%";
                    responseData = responseData.replaceAll(variableName, subStringValue);
                }
            }
        }
        return responseData;
    }
}
