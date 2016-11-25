package client;

import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;

public class PeerThread implements Runnable{
    private DataInputStream in;
    private DataOutputStream out;
    private Thread thread;
    private String threadName;
    private String peerIp;
    private Torrent torr;
    private Socket peerSock;
    private static boolean end;
    private static int numThreads;
    private Map<String, Object> peer;

    public PeerThread(String threadName, Torrent torr, Map<String, Object> peer){
      this.threadName = threadName;
      this.torr = torr;
      this.peer = peer;
      incThreadCount();
    }

    public void start(){
      if(thread == null)
        thread = new Thread(this, this.threadName);
      thread.start();
    }

    public void run(){
      //send handshake
      System.out.println("Thread " + threadName + " is running!");
      checkHandshake();
      System.out.println(threadName + "'s handshake approved");

      try{
        getBitField();
        gaugeInterest();

        downloadFile();

        System.out.println("   The file is " + torr.getPieceLength() + "B long and has " + torr.getPieceCount() + " pieces.");
      }catch(IOException e){
        System.err.println("ERROR: Sumting went wrong in " + threadName + ".");
        e.printStackTrace();

      }

      
    }

    private void gaugeInterest() throws IOException{
      System.out.println("   Expressing interest to peer " + peerIp + "...");
      out.writeInt(1);                // Message Length
      out.writeByte(2);               // Message ID

      int len = in.readInt();
      int msgId = in.readByte();
      System.out.println("      len: " + len + "\n      msgId: " + msgId);

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
      System.out.println("   " + peerIp + " is interested.");
    }

    private void getBitField() throws IOException{
      System.out.println("   Getting bitfield...");
      int len = in.readInt();
      byte msgId = in.readByte();
      if(msgId == 5){
        System.out.println("   Verified bitfield");
        //byte[] bitfield = new byte[1+(torr.getPieceHashes().length/8)];
        byte[] bitfield = new byte[len -1];
        in.readFully(bitfield);
      }else{
        System.err.println("ERROR: Could not get bitfield of thread " + threadName + ".");
      }
    }

    private void closeConnects(){
      try {
        peerSock.close();
        in.close();
        out.close();}
      catch (IOException e) {
        System.err.println("ERROR: Could not close connections.");
        e.printStackTrace();
      }
    }

    //get the peerSocket to open a socket to send handshake
    private Socket getPeerSock() throws UnknownHostException, IOException{
      peerIp = getStringFrom((ByteBuffer) peer.get(torr.getKEY_IP()));
      int peerPort = (int)peer.get(torr.getKEY_PORT());
      peerSock = new Socket(peerIp, peerPort);
      return peerSock;
    }

    //check the handshake of the user.
    private synchronized void checkHandshake(){
      System.out.println("   Attempting to handshake with peer...");
      try{
        Socket peerSock = getPeerSock();
        in = new DataInputStream(peerSock.getInputStream());
        out = new DataOutputStream(peerSock.getOutputStream());
        Handshake hs = new Handshake(out, in, torr, peer);
        hs.sendHandshake();
      }catch(UnknownHostException e){
        System.err.println("ERROR: Could not create socket to handshake.");
        e.printStackTrace();
      }catch(IOException e){
        System.err.println("ERROR: Could not create socket to handshake.");
        e.printStackTrace();
      }catch(Exception e){
        System.err.println("ERROR: Could not create socket to handshake.");
        e.printStackTrace();
      }
    }

    private static String getStringFrom(ByteBuffer byteString) {
            byte[] bytes = byteString.array();
            String res = "";
            for (int i = 0; i < bytes.length; i++) {
                res += (char) bytes[i];
            }

            return res;
    }


    private synchronized void incThreadCount(){
      numThreads++;
    }

}
