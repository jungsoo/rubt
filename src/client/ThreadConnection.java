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
  private Torrent torr;
  private Peers peers;
  private String outFileName;

  public ThreadConnection(int numPieces, Peers peers, Torrent torr, String outFileName){
    this.peers = peers;
    this.torr = torr;
    this.outFileName = outFileName;
  }

  public void run(){
    //run tracker thread
    


    int i = 0;
      for(Map<String, Object> p : peers.getPeers()){
          PeerThread pt = new PeerThread("Thread-" + i, torr, p, outFileName);
          pt.start();
        //allow only 1 thread to access boolean array of pieces received
        //sychronized(pieceRec){
        //  readPiece();
        //}
        System.out.println("hello");
        i++;
      }
  }




}

