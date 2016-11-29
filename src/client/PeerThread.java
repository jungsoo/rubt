/*
 *
 * Jamie Liao and Jungsoo Park
 *
 * This class is the peer thread that runs for each peer on the list of acceptable peers. Initiates handshake to peer and verifies interest. Then begins to download the file in order and writes to Piece type array to check which piece to download.
 *
 */

package client;

import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PeerThread implements Runnable{
    private DataInputStream in;
    private DataOutputStream out;
    private Thread thread;                      // Thread to run
    private String threadName;                  // Unique thread name
    private String peerIp;                      // This specific peer's ip
    private static Torrent torr;                // Torrent info
    private Socket peerSock;                    // This specific peer's socket to communicate
    private static boolean end;                 // End condition in case user wants to end
    private static int numThreads;              // Maintains number of running threads
    private Map<String, Object> peer;           // Current peer thread is talking to
    //private static Piece[] pieceRec;            // Piece array to keep track of pieces
    private static int index;                   // Pointer to the next available piece
    private static FileOutputStream os;         // Used to continuously write to file

    public PeerThread(String threadName, Torrent torr, Map<String, Object> peer){
      this.threadName = threadName;
      this.torr = torr;
      this.peer = peer;
      //torr.pieceRec = new Piece[torr.getPieceCount()];
      index = getNextIndex();
      //Check if file exists already
      try{
/*
        String directory = System.getProperty("user.dir");
        if(new File(directory, outFileName+".ser").exists()){        //if resuming download
          System.out.println("Resuming download of " + torr.getFileName());
          os = new FileOutputStream(torr.getFileName(), true);
          updateSave();
        }else
*/
          os = new FileOutputStream(torr.getFileName(), true);
      }catch(FileNotFoundException e){
        System.err.println("ERROR: Could not write to file.");
        e.printStackTrace();
      }
    }

    public void start(){
      if(thread == null)
        thread = new Thread(this, this.threadName);
      thread.start();
    }

    public void run(){
      //send handshake
      //System.out.println("Thread " + threadName + " is running!");
      checkHandshake();
      //System.out.println(threadName + "'s handshake approved");

      try{
        getBitField();
        unchoke();
        //System.out.println("   The file is " + torr.getPieceLength() + "B long and has " + torr.getPieceCount() + " pieces.");

        System.out.println("Downloading file from " + threadName + "...");
        while(index < 509 && !torr.getFinished()){
          downloadFile();
        }
        System.out.println("Finished!");
        os.close();
        in.close();
        out.close();
      

      }catch(IOException e){
        torr.setFinished(true);
        return;
        //e.printStackTrace();
      }
    }

/*
    private void updateSave(){
      Save s = null;
      try {
        FileInputStream fileIn = new FileInputStream(torr.getFileName() + ".ser");
        ObjectInputStream in = new ObjectInputStream(fileIn);
        s = (Save) in.readObject();
        in.close();
        fileIn.close();
      }catch(IOException e) {
        System.err.println("ERROR: Could not read saved file.");
        e.printStackTrace();
      }catch(ClassNotFoundException e) {
        System.err.println("ERROR: Could not read saved file.");
        e.printStackTrace();
      }
      
    }
*/

    //method to download the file
    private void downloadFile() throws IOException{
      byte msgId = -2;
      int i = -2, pieceLength = -1, begin = -1;
      Piece piece;
      byte[] currPiece =  new byte[torr.getPieceLength()];
      // Sending "request"
      synchronized(torr.getPieceRec()){
        out.writeInt(13);                         // Message Length
        out.writeByte(6);                         // Message ID
        out.writeInt(getNextIndex());             // Index
        out.writeInt(0);                          // Begin
        out.writeInt(torr.getPieceLength());      // Length

        // Read message header
        int len = in.readInt();
        msgId = in.readByte();


        //Interested
        if(msgId == 7){

          if(index == torr.getPieceCount() -1){
            while(in.available()>0) {
              i = in.readInt();
              begin = in.readInt();
            }
          }else{
            i = in.readInt();
            begin = in.readInt();
          }

          pieceLength = torr.getPieceLength();
          //System.out.println("   " + i + " is a piece!");
          if (index == torr.getPieceCount() - 1) { // Last piece
            pieceLength = in.available();
          } else { // Wait until there is enough available bytes
            while (in.available() < torr.getPieceLength()) { }
          }
          piece = new Piece(len, msgId, pieceLength);
          torr.getPieceRec()[i] = piece;

          in.readFully(currPiece);
          //build piece to store into array
          piece.setIndex(i);
          piece.setBegin(begin);
          piece.setPieceData(currPiece);

        }
      }//sync
      if(msgId == 7){

        ByteBuffer correctChecksum = torr.getPieceHashes()[i];
        ByteBuffer pieceChecksum = getSHA1Checksum(currPiece);

        if (correctChecksum.equals(pieceChecksum)) {
          System.out.println(i + "\t" + currPiece);
          try{
              os.write(currPiece);
              //System.out.println("   piecelength: " + pieceLength);
              torr.incDownload(pieceLength);
          }catch(FileNotFoundException e){
            System.err.println("ERROR: Could not write to file.");
            e.printStackTrace();
          }
        }
      }else{//if msgId == 7
        System.out.println("   Not a piece :(");
        return;
      }
    }

    //method to get the next piece's index
    private synchronized int getNextIndex(){
      for(; index < torr.getPieceCount(); index++){
        if(torr.getPieceRec()[index] == null){
          //System.out.println("      index: " + index);
          return index;
        }
      }
      return -1;
    }

    private static ByteBuffer getSHA1Checksum(byte[] piece) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(piece);
            return ByteBuffer.wrap(digest.digest());
        } catch (NoSuchAlgorithmException nsae) {
            System.err.println(nsae.getLocalizedMessage());
        }
        return null;
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

    private void getBitField() throws IOException{
      //System.out.println("   Getting bitfield...");
      int len = in.readInt();
      byte msgId = in.readByte();
      if(msgId == 5){
        //System.out.println("   Verified bitfield");
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
      //System.out.println("   Attempting to handshake with peer...");
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


}
