package tcpStub;

import javax.json.JsonObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.json.*;
import java.util.Date.*;

public class tcpWorker implements Runnable {
  
  private final Socket clientSocket;
  private final JsonObject configObject;
  private final JsonObject dataVariableObject;
  private final JsonObject requestResponseObject;
  private final ArrayList<String> incrementNumberArray;
  private final int contentFirstPos;
  private final int contentLastPos;
  private final String contentDataFormat;

  public tcpWorker(Socket clientSocket,
                   JsonObject configObject,
                   JsonObject dataVariableObject,
                   JsonObject requestResponseObject,
                   ArrayList incrementNumberArray) {
    
    this.clientSocket = clientSocket;
    JsonArray configArray = (JsonArray) configObject.get("configCheck");
    this.contentFirstPos = configArray.getJsonObject(0).getInt("startPos");
    this.contentLastPos = configArray.getJsonObject(0).getInt("endPos");
    this.contentDataFormat = configArray.getJsonObject(0).getString("dataFormat");
    this.configObject = configObject;
    this.dataVariableObject = dataVariableObject;
    this.requestResponseObject = requestResponseObject;
    this.incrementNumberArray = incrementNumberArray;
    
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
        if (contentDataFormat.toUpperCase().equals("HEX")){

        }
        cntr++;
        System.out.println(cntr + ":" + inputLine);
        contentCheck = inputLine.substring(contentFirstPos, contentLastPos);    // extract contentCheck indentifier from input message
        // at this stage need to check the request response array for correct responses to send based on the contentCheck
        JsonArray requestResponseArray = (JsonArray) requestResponseObject.get("response");
        for (int i = 0; i < requestResponseArray.size(); i++) {
          String checkValue = getVariableName(requestResponseArray, i);
          if (contentCheck.equals(checkValue)){                               // if the contentCheck exists in the line then send it out
            responseData = getResponseData(requestResponseArray, i);
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
      // now we've got the variable name, lets loop though variable array and see if we can find a match
      JsonArray variableArray = (JsonArray) dataVariableObject.get("variable");

      for (int variableCntr = 0; variableCntr < variableArray.size(); variableCntr++) {
        String arrayVariableName = getVariableName(variableArray, variableCntr);

        if (arrayVariableName.equals(variableName)){
          // we've found a match so now get the type of the variable
          String arrayVariableType = getVariableType(variableArray, variableCntr);
          // extract all the format detail for the variable
          JsonArray formatArray = getFormatArray(variableArray, variableCntr);

          // based on the variable type process it  here...
          if (arrayVariableType.equals("substring")){
            int formatStartPos = getStartPos(formatArray);
            int formatEndPos = getEndPos(formatArray);
            subStringValue = inputLine.substring(formatStartPos, formatEndPos);
          } else if (arrayVariableType.equals("randomNumber")) {
            int formatMin = getMin(formatArray);;
            int formatMax = getMax(formatArray);;
            String formatFormat = getFormat(formatArray);;
            int random_int = (int)Math.floor(Math.random()*(formatMax-formatMin+1)+formatMin);
            subStringValue = String.format(formatFormat, random_int);  // 0009
          } else if (arrayVariableType.equals("date")) {
            String dateFormat = getFormat(formatArray);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
            String currentTime = simpleDateFormat.format(new Date());
            subStringValue = currentTime;
          } else if (arrayVariableType.equals("guid")) {
            UUID randomUUID = UUID.randomUUID();
            subStringValue = randomUUID.toString();
          } else if (arrayVariableType.equals("IncrementNumber")) {
            String numberFormat = getFormat(formatArray);
            int incrementItemValue = 0;
            for (int i = 0; i < incrementNumberArray.size(); i++){
                String[] incrementItem = incrementNumberArray.get(i).split(":");      // split the array
                if (incrementItem[0].equals(variableName)){                                 // if current array name matches current variablename
                  incrementItemValue = Integer.parseInt(incrementItem[1]);                  // extract the number from array
                  incrementItemValue++;                                                     // increment the number
                  incrementNumberArray.set(i,variableName + ":" + incrementItemValue);      // update the number to array
                  break;
                }
            }
            subStringValue = String.format(numberFormat, incrementItemValue);
          }
        }
      }

      variableName = "%" + variableName + "%";
      responseData = responseData.replaceAll(variableName, subStringValue);
    }

    return responseData;
  }
  //
  
  String getVariableName(JsonArray  variableArray, int variableCntr ){
    return variableArray.getJsonObject(variableCntr).getString("name");
  }
  String getVariableType(JsonArray  variableArray, int variableCntr ){
    return variableArray.getJsonObject(variableCntr).getString("type");
  }
  
  JsonArray getFormatArray(JsonArray  variableArray, int variableCntr ){
    JsonArray returnFormatArray = (JsonArray) variableArray.getJsonObject(variableCntr).get("format");
    return returnFormatArray;
  }
  
  int getStartPos(JsonArray  formatArray){
    return formatArray.getJsonObject(0).getInt("startPos");
  }
  
  int getEndPos(JsonArray  formatArray){
    return formatArray.getJsonObject(0).getInt("endPos");
  }
  
  int getMin(JsonArray  formatArray){
    return formatArray.getJsonObject(0).getInt("min");
  }
  
  int getMax(JsonArray formatArray){
    return formatArray.getJsonObject(0).getInt("max");
  }
  
  String getFormat(JsonArray formatArray){
    return formatArray.getJsonObject(0).getString("format");
  }
  
  String getResponseData(JsonArray variableArray, int variableCntr ){
    return variableArray.getJsonObject(variableCntr).getString("response");
  }
  
}
