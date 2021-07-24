package tcpListener;

import tcpListener.*;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class tcpWorker implements Runnable {

  private Socket clientSocket;

  public tcpWorker(Socket clientSocket){
      this.clientSocket = clientSocket;
      System.out.println(this.clientSocket);
  }

  @Override
  public void run() {

  }

}
