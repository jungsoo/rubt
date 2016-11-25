/*
 * Jamie Liao and Jungsoo Park
 *
 * This class maintains the connections between the multiple connected peers via threads. It handles a download or upload request from the peers.
 *
 *
 */

package client;

import java.io.*;
import java.util.*;
import java.net.*;

public class ThreadConnection{
  //static array to be shared between the threads that contains the index of the received and downloaded pieces. Each piece has a unique index that can be mapped to each piece.
  private Thread thread;
  private static Piece[] pieceRec;
  private boolean end;
  private Torrent torr;
  private Peers peers;

  public ThreadConnection(int numPieces, Peers peers, Torrent torr){
    pieceRec = new Piece[numPieces];
    this.peers = peers;
    this.torr = torr;
    end = false;
  }

  public void run(){
    int i = 0;
      for(Map<String, Object> p : peers.getPeers()){
          PeerThread pt = new PeerThread("Thread-" + i, torr, p);
          pt.start();
        //allow only 1 thread to access boolean array of pieces received
        //sychronized(pieceRec){
        //  readPiece();
        //}
        System.out.println("hello");
        i++;
      }
  }



  private void printPieces(){
    for(int i = 0; i < pieceRec.length; i++){
      if(pieceRec[i] != null)
        System.out.print("1");
      else
        System.out.print("0");

    }
  }

}

