/************************************************************************
 * 
 * This class acts as a client to the server, and sends automatically generated equations. 
 * 
 * Message Format
 * |SRC PORT|DEST PORT|User Name|SEQ NUM|ACK NUM|MSG|
 *     0         1         2        3       4     5
 * 
 *************************************************************************/
import java.io.*; 
import java.net.*; 

class AutoTCPClient extends Thread{ 

  private Socket clientSocket;
  private String userName;
  private int srcPort;
  private int destPort;
  private volatile boolean exited = false;

  public AutoTCPClient(int srcPort, int destPort, String user) {
    this.userName = user;
    this.srcPort = srcPort;
    this.destPort = destPort;
  }

  public static void main(String[] args){
    if(args.length != 3){
        System.out.println("Error running client!");
        System.out.println("Client must be run as: TCPClient <srcPort> <destPort> <Name>");
        return;
    }
    int srcPort = Integer.parseInt(args[0]);
    int destPort = Integer.parseInt(args[1]);
    String name = args[2];
    System.out.println("Starting Client");
    AutoTCPClient client = new AutoTCPClient(srcPort, destPort, name);
    client.start();
    System.out.println("Finished creating client!");
}

  //This method runs everytime a thread is started. 
  @Override
  public void run() {
      try {
        //Attempt to establish a connection between server and client. 
        establishConnection(userName);
        int ackNum = 0;
        int seqNum = 0;
        int serverAck=0;
        do {
          ackNum++;
          seqNum++;
          //Assigns a random double between 1 and 1000
          double one = (Math.random()*1000)+1;
          double two = (Math.random()*1000)+1;
          //Creates a sleep time betweeen 1 and 2000 ms
          int sleep = (int)(Math.random()*2000)+1;
          //Chooses a random operand
          int operand = (int)(Math.random()*4)+1;
          String op;
          switch(operand) {
            case 1: op = "+";
                    break;
            case 2: op = "-";
                    break;
            case 3: op = "*";
                    break;
            case 4: op = "/";
                    break;
            default: op = "";
          }

          //This is an artificial limit preventing the autoClient from sending more than 5 messages.
          if(ackNum > 5) {
            sendMessage(srcPort+","+destPort+","+userName+","+seqNum+","+ackNum+",exit");
          } else {
            System.out.println(String.format("Sending equation: %.2f%s%.2f",one,op,two));
            sendMessage(srcPort+","+destPort+","+userName+","+seqNum+","+ackNum+","+String.format("%.2f%s%.2f",one,op,two));
          }

          Thread.sleep(sleep);

          // This assignment allows the client to read the message from the server before the while loop,
          // which gives time for the exited variable to update before its checked.
          serverAck = readMessage();

          // This check makes sure that the client hasn't exited,
          // and that the ackNum received from the server matches the ackNum of the message sent.
        } while(!exited && serverAck == ackNum);
        
      } catch(Exception e) {
        e.printStackTrace();
      }
  }

  public void sendMessage(String sentence) throws IOException {
    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
    outToServer.writeUTF(sentence + '\n');

  }
 
  public void exitClient() throws IOException {
    exited = true;
    System.out.println("EXITING");
    clientSocket.close();
    System.out.println("Closed socket");
  }

  public int readMessage() throws IOException {
    DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());
    //|SRC PORT|DEST PORT|User Name|SEQ NUM|ACK NUM|MSG|
    //    0         1         2        3       4     5
    String modifiedSentence = inFromServer.readUTF();
    String[] message = modifiedSentence.split(",");

    if(message[5].equalsIgnoreCase("exit")) {
      exitClient();
    } else {
      System.out.println(message[5]+"\n");
    }
    //Returns the ackNum of the message. 
    return Integer.parseInt(message[4]);
  }

  public void establishConnection(String user) throws IOException {
    String modifiedSentence;
    clientSocket = new Socket("localhost",destPort); 

    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
    DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());

    //|User|DEST PORT|SEQ NUM|ACK NUM|MSG|
    //  0       1        2       3     4 
    outToServer.writeUTF(user);

    modifiedSentence = inFromServer.readUTF();

    if(modifiedSentence.equalsIgnoreCase(user)) {
      System.out.println("Connection for user: "+user+" established sucessfully.\n");
    } else {
      exitClient();
    }
  }   
}

        
