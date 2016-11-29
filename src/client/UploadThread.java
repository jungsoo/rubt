/*
 *
 * Jamie Liao and Jungsoo Park
 *
 * This thread uploads pieces to the peer.
 *
 */

package client;

import java.io.*;
import java.util.Arrays;
import java.net.*;

public class UploadThread implements Runnable{
  private static Torrent torr;
  private String threadName;
  private Thread thread;
  private Socket sock;
  private DataInputStream in;
  private DataOutputStream out;

  public UploadThread(String threadName,  Torrent torr, Socket sock){
    this.threadName = threadName;
    this.torr = torr;
    this.sock = sock;
    try{
      in = new DataInputStream(sock.getInputStream());
      out = new DataOutputStream(sock.getOutputStream());
    }catch(IOException e){
      System.err.println("ERROR: Could not create data streams.");
      e.printStackTrace();
    }
  }

  public void run(){
    try{
      synchronized(torr.getPieceRec()){
        sendResources();

        int len = in.readInt();
        int msgId = in.readByte();
        if(msgId != 2){
          System.out.println(threadName + " is not interested.");
        }
        unchoke();

        len = in.readInt();
        msgId = in.readByte();

        //if request
        if(len == 13 && msgId == 6){
          int index = in.readInt();
          int begin = in.readInt();
          len = in.readInt();

          if(len < 16384){
            System.out.println("Requested piece is too large!");
            close();
          }

          uploadPiece(index, begin, len);

        }
      }
    }catch(IOException e){
      System.out.println("error!");
    }
  }

  private void close() throws IOException{
    in.close();
    out.close();
    sock.close();
    return;
  }

  private void uploadPiece(int index, int begin, int len) throws IOException{
    byte[] piece = torr.getPieceRec()[index].getPieceData();
    byte[] pieceToSend = Arrays.copyOfRange(piece, begin, len+begin);

    out.writeInt(9+len);                      // Message Length
    out.writeByte(7);                         // Message ID
    out.writeInt(index);                      // Index
    out.writeInt(begin);                      // Begin
    out.write(pieceToSend);                   // Piece

    torr.setUploaded(len);
  }

  private void unchoke() throws IOException{
      //System.out.println("   Expressing interest to peer " + peerIp + "...");
      out.writeInt(1);                // Message Length
      out.writeByte(2);               // Message ID

      int len = in.readInt();
      int msgId = in.readByte();

      //if msg is unchoked
      if(!(len == 1 && msgId == 1)){
          System.out.println("   Peer not interested... trying again");
          //keep trying to find interested Peer
          while(true){
            out.writeByte(2);
            msgId = in.readByte();
            System.out.println("msgId: " + msgId);
            if(msgId == 1)
              break;
          }
      }
      //System.out.println("   " + peerIp + " is interested.");
  }

  public void start(){
    if(thread == null)
      thread = new Thread(this, threadName);
    thread.start();
  }

  private void sendResources() throws IOException{
    for(int i = 0; i < torr.getPieceRec().length;i ++){
      out.writeInt(5);                         // Message Length
      out.writeByte(4);                         // Message ID
      out.writeInt(i);                          // Index
      out.writeInt(0);                          // Begin
      out.writeInt(torr.getPieceLength());      // Length

    }
  }
}
