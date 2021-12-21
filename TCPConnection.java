/************************************************************************
 * 
 * This class acts as the actual Server-Client connection
 * 
 * Message Format
 * |SRC PORT|DEST PORT|User Name|SEQ NUM|ACK NUM|MSG|
 *     0         1         2        3       4     5
 * 
 *************************************************************************/
import java.io.*;
import java.net.*;
import java.util.PriorityQueue;
import java.util.Queue;

class TCPConnection extends Thread {
    Socket connection;
    boolean exited = false;

    private long startTime;

    public TCPConnection(Socket conn) {
        connection = conn;
    }

    //This method runs everytime a thread is started. 
    @Override
    public void run() {
      startTime = System.currentTimeMillis();
      //Loops forever reading client messages and sending messages back to client. 
      while(!exited) {
          try {
            sendMessage(readMessage());    
          } catch (Exception e) {
            e.printStackTrace();
          }
      }
      exitThread();
    }

    public String[] readMessage() throws IOException {
        DataInputStream inFromClient = new DataInputStream(connection.getInputStream());
        String clientSentence = inFromClient.readUTF();

        //Stores client messages in priority queue.
        Queue<String> messageQueue = new PriorityQueue<>();
        messageQueue.add(clientSentence);

        //Get the first message from the queue, trim whitespace from the message and split it into an array
        String[] message = messageQueue.poll().trim().split(",");
        TCPServer.logger.info("Equation from client: "+message[2]+", eq: "+message[5]);
        
        //If the message is exit, write it to log file and exit this thread.
        if(message[5].equalsIgnoreCase("exit")) {
          double elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
          TCPServer.logger.info("Client "+message[2]+" has closed connection after " + Double.toString(elapsedSeconds) + " seconds.");
          exited = true;
        } else {  //If the message is not exit, compute the equation from the message and format it to 2 decimals. 
          message[5] = String.format("Result: %.2f",compute(message[5]));
        }
        
        //return the entire message array
        return message;
      }
    
      public void sendMessage(String[] clientSentence) throws IOException {
        //|SRC PORT|DEST PORT|User Name|SEQ NUM|ACK NUM|MSG|
        //    0         1         2        3       4     5
        DataOutputStream outToClient = new DataOutputStream(connection.getOutputStream());
        String output = "";
        
        //Combine each array element into a single String separated by commas
        for(int i=0; i<clientSentence.length; i++) {
          output=output.concat(clientSentence[i]);
          if(i != clientSentence.length-1) {
            output=output.concat(",");
          }
        }

        TCPServer.logger.info("Message to client: "+clientSentence[2]+", "+clientSentence[5]+"\n");

        //Write the message to the output stream
        outToClient.writeUTF(output);
      }
    
      public double compute(String str){
        //Splits the string into two doubles and an operand
        String[] arr = str.split("\\+|-|\\*|/");
        double one = Double.parseDouble(arr[0]);
        double two = Double.parseDouble(arr[1]);
        char operand = str.charAt(arr[0].length());
        
        double result;
    
        switch(operand) {
          case '+': result = one+two;
                  break;
          case '-': result = one-two;
                  break;
          case '/': result = one/two;
                  break;
          case '*': result = one*two;
                  break;
          default: result = -1;
        }

        return result;
      }

      //Attempt to close this thread. 
      public void exitThread() {
        try {
          Thread obj = Thread.currentThread();
          obj.join();
        } catch (Exception e) {
          e.printStackTrace();
        }
        
      }
}