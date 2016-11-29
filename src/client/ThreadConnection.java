/*
 * Jamie Liao and Jungsoo Park
 *
 * This class maintains the connections between the multiple connected peers via threads. It handles all thread calling for downloading, uploading, tracker updates, and listening. 
 *
 *
 */

package client;

import java.io.*;
import java.util.*;
import java.net.*;

public class ThreadConnection{
  private boolean end;
  private static Torrent torr;
  private Peers peers;
  private String outFileName;
  private static ArrayList<String> uploadList;

  public ThreadConnection(int numPieces, Peers peers, Torrent torr, String outFileName){
    this.peers = peers;
    this.torr = torr;
    this.outFileName = outFileName;
    uploadList = new ArrayList<String>();
  }

  public void run(){
    //check to see if the download is resuming or completely new
    String directory = System.getProperty("user.dir");
    if(new File(directory, outFileName).exists()){
      System.out.println("Resuming download of " + torr.getFileName() + "...");
      updateSave();
    }


    int i = 0;
    for(Map<String, Object> p : peers.getRUPeers()){
        PeerThread pt = new PeerThread("Thread-" + i, torr, p);
        pt.start();
      //allow only 1 thread to access boolean array of pieces received
      //sychronized(pieceRec){
      //  readPiece();
      //}
      i++;
    }

    //Uploading listening starts
    System.out.println("Piece length: " + torr.getPieceLength());
    ListenConnect lc = new ListenConnect(torr);
    lc.start();

  }

  private void updateSave(){
    
    Save s = null;
    try {
        FileInputStream fileIn = new FileInputStream(torr.getFileName() + ".ser");
        ObjectInputStream in = new ObjectInputStream(fileIn);
        s = (Save) in.readObject();
        in.close();
        fileIn.close();
    }catch(IOException e) {
        System.err.println("ERROR: Could not find save file to read from.");
        e.printStackTrace();
    }catch(ClassNotFoundException e) {
        System.err.println("ERROR: Could not write to file.");
        e.printStackTrace();
    }

    if(!Arrays.equals(s.getInfoHash(), torr.getInfoHash())){
      System.err.println("ERROR: Incorrect info hashes with save file.");
      System.exit(0);
    }
    torr.setDownloaded(s.getDownloaded());
    torr.setUploaded(s.getUploaded());
    torr.setLeft(s.getLeft());
    torr.setPieceRec(s.getPieceRec());
    System.out.println("Total downloaded: " + torr.getDownloaded());
    System.out.println("Successfully retrieved save file");
  }


}
