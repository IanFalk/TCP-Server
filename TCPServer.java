/************************************************************************
 * 
 * This class acts as the listening server, 
 * and creates a new connection everytime a client connects.
 * 
 * Message Format
 * |SRC PORT|DEST PORT|User Name|SEQ NUM|ACK NUM|MSG|
 *     0         1         2        3       4     5
 * 
 *************************************************************************/

import java.io.*; 
import java.net.*;
import java.util.logging.*;

class TCPServer { 

  public static Logger logger;
  private static FileHandler fh;
  public static ServerSocket welcomeSocket;
  public static Socket connectionSocket;

  public static void main(String[] args){
    //Ensures that a port was specified
    if(args.length != 1){
        System.out.println("Error starting server!");
        System.out.println("Format should be TCPServer <welcomePort>");
        return;
    }
    System.out.println("Starting server");
    try {
        start(Integer.parseInt(args[0]));
    } catch (Exception i){
        System.out.println("Closing server");
    }
}
  
  public static void start(int welcomePort) throws IOException {
    logger = Logger.getLogger("MyLog");
      
    //Sets the log format to [hour:minute:second AM/PM] <message>
    System.setProperty("java.util.logging.SimpleFormatter.format","[%1$tr] %5$s %n");
    SimpleFormatter formatter = new SimpleFormatter();

    //Sets the server log file, creates it if it doesn't exist.
    fh = new FileHandler("MyLogFile.log");
    fh.setFormatter(formatter);
    logger.addHandler(fh);

    logger.info("Server Startup");

    welcomeSocket = new ServerSocket(welcomePort);
    try {
      while(true){
        //accept() waits until a connection from a client has been made, the rest of the program stalls until this connection has been made. 
        connectionSocket = welcomeSocket.accept();

        DataInputStream inFromClient = new DataInputStream(connectionSocket.getInputStream());
        String clientSentence = inFromClient.readUTF(); //Reads line from input stream
      
        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        outToClient.writeUTF(clientSentence.toUpperCase()); //Writes line to output stream, but converts it to uppercase

        logger.info("New connection established with user: "+clientSentence+"\n");

        //Creates a new thread of the TCPConnection class. Each thread is the actual "connection" between Server and Client
        TCPConnection client = new TCPConnection(connectionSocket);
        //Starts the thread
        client.start();
      }
    } catch (IOException e){
      logger.info("Server shutting down");
      e.printStackTrace();
      logger.removeHandler(fh);
      fh.close();
    }
  } 
} 
 

           
