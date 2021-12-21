/************************************************************************
 * 
 * This class acts as a client to the server, and accepts user input. 
 * 
 * Message Format
 * |SRC PORT|DEST PORT|User Name|SEQ NUM|ACK NUM|MSG|
 *     0         1         2        3       4     5
 * 
 *************************************************************************/
import java.io.*; 
import java.net.*; 

class ManualTCPClient extends Thread{ 

  private Socket clientSocket;
  private String userName;
  private int srcPort;
  private int destPort;
  private volatile boolean exited = false;

  public ManualTCPClient(int srcPort, int destPort, String user) {
    this.userName = user;
    this.srcPort = srcPort;
    this.destPort = destPort;
  }

  public static void main(String[] args){
    try {
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        String username;
        int srcPort;
        int destPort;

        System.out.println("What is your username?");
        username = inFromUser.readLine(); 

        System.out.println("What port do you want to connect to?");
        destPort = Integer.parseInt(inFromUser.readLine());

        System.out.println("What port are you connecting from?");
        srcPort = Integer.parseInt(inFromUser.readLine());

        ManualTCPClient client = new ManualTCPClient(srcPort, destPort, username);
        client.start();
    } catch (Exception e) {
        e.printStackTrace();
    }
    
}

  //This method runs everytime a thread is started. 
  @Override
  public void run() {
      try {
        //Attempt to establish a connection between server and client. 
        establishConnection(userName);
        int ackNum = 0;
        int seqNum = 0;
        do {
          ackNum++;
          seqNum++;
          BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

          //Reads an equation from user input
          System.out.println("Enter an equation for the server to solve: ");
          String sentence = inFromUser.readLine();

          //Sends the equation to the server. 
          System.out.println(String.format("Sending equation: %s",sentence)); 
          sendMessage(srcPort+","+destPort+","+userName+","+seqNum+","+ackNum+","+sentence);
          Thread.sleep(1000);
        } while(!exited && readMessage() == ackNum);
        
      } catch(Exception e) {
        e.printStackTrace();
      }
  }

  //This method writes messages to the dataoutputstream for the server to read from. 
  public void sendMessage(String sentence) throws IOException {
    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
    outToServer.writeUTF(sentence + '\n');

  }
 
  //This method closes the client socket, breaks the loop in run(), and ends this thread. 
  public void exitClient() throws IOException {
    System.out.println("EXITING");
    clientSocket.close();
    System.out.println("Closed socket");
    exited = true;
    System.exit(0);
  }

  //This method reads a message from the server in the datainputstream. 
  public int readMessage() throws IOException {
    DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());
    String modifiedSentence = inFromServer.readUTF();
    String[] message = modifiedSentence.split(",");

    //If the message is 'exit' then call the exitClient() method, if not print the result for the client.
    if(message[5].equalsIgnoreCase("exit")) {
      exitClient();
    } else {
      System.out.println(message[5]+"\n");
    }
    //Returns the ackNum of the message. 
    return Integer.parseInt(message[4]);
  }

  //This method establishes a connection between client and server. 
  public void establishConnection(String user) throws IOException {
    String modifiedSentence;
    clientSocket = new Socket("localhost",destPort); 

    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
    DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());

    outToServer.writeUTF(user);

    modifiedSentence = inFromServer.readUTF();

    //If the server returns the client username capitalized, then the connection is successfully established. 
    if(modifiedSentence.equalsIgnoreCase(user)) {
      System.out.println("Connection for user: "+user+" established sucessfully.\n");
    } else {
      exitClient();
    }
  }   
}

        
