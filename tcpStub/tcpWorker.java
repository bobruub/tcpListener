import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class tcpWorker implements Runnable {

    private final Socket clientSocket;
    private final ArrayList<String> result;
    private final ArrayList<String> dataVariables;
    private final int contentFirstPos;
    private final int contentLastPos;

    public tcpWorker(Socket clientSocket,
                     int contentFirstPos, int contentLastPos,
                     ArrayList<String> result, ArrayList<String> dataVariables) {

        this.clientSocket = clientSocket;
        this.contentLastPos = contentLastPos;
        this.contentFirstPos = contentFirstPos;
        this.result = result;
        this.dataVariables = dataVariables;

    }

    @Override
    public void run() {
        System.out.println("---------------");
        System.out.println("Processing Client Request...");
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
                inputLine = in.readLine();
                cntr++;
                System.out.println(cntr + ":" + inputLine);
                // at this stage need to check the request response array for correct responses to send
                // extract indentifier from input message
                contentCheck = inputLine.substring(contentFirstPos, contentLastPos);
                //System.out.println("contentCheck: " + contentCheck);
                // loop throught he request response array
                for (int i = 0; i < result.size(); i++) {
                    // read and split the current line
                    requestResponseLine = result.get(i);
                    //System.out.println("requestResponseLine: " + requestResponseLine);
                    String[] requestResponseArray = requestResponseLine.split(";");
                    // if the contentCheck exists in the line then send it out
                    if (requestResponseLine.contains(contentCheck)) {
                        // extract the response data from the array
                        responseData = requestResponseArray[1];
                        // check if response line has any variables to be processed
                        if (responseData.contains("%")) {
                            responseData = processVariable(responseData, inputLine);
                        }
                        // if the contentCheck responseData matches the end of a sequence then close the loop
                        if (responseData.equals("endOfStream")) {
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
        // extract the variablename from the inptut streem %varname%
        while (true) {
            int firstPos = responseData.indexOf("%");
            int lastPos = responseData.indexOf("%", firstPos + 1);
            //System.out.println("--------------------------");
            //System.out.println("responseData: " + responseData);
            //System.out.println("firstPos: " + firstPos);
            //System.out.println("lastPos: " + lastPos);
            if (firstPos < 0) {
                break;
            }
            String variableName = responseData.substring(firstPos + 1, lastPos);
            //System.out.println("variableName: "  +variableName);
            // now we've got the variable name, lets loop though varibale array and find out what type it is
            for (int dvi = 0; dvi < dataVariables.size(); dvi++) {
                dataVariableDetails = dataVariables.get(dvi);
                // if the variable name exists in the current line then process
                if (dataVariableDetails.contains(variableName)) {
                    // split it into name, type and rules e.g sequenceNumber;substring;7,10
                    String[] dataVariableDetailsArray = dataVariableDetails.split(";");
                    String variableType = dataVariableDetailsArray[1];
                    String variableRules = dataVariableDetailsArray[2];
                    //System.out.println("variableType: "  + variableType);
                    //System.out.println("variableRules: "  + variableRules);

                    if (variableType.equals("substring")) {
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
