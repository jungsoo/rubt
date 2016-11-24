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

public class Connection implements Runnable{
  //static array to be shared between the threads that contains the index of the received and downloaded pieces. Each piece has a unique index that can be mapped to each piece.
  private Thread thread;
  private static Piece[] pieceRec;
  private boolean end;
  private List<Map<String, Object>> RUPeers;

  public Connection(int numPieces){
    pieceRec = new Piece[numPieces];
    end = false;
  }

  //method to run the multithreading process to check if the connection is valid
  @Override
  public void run(){
    while(!end){
      for(Map<String, Object> p : RUPeers){
        
        //allow only 1 thread to access boolean array of pieces received
        //sychronized(pieceRec){
        //  readPiece();
        //}
        System.out.println("hello");
        break;
      }
    }
  }

  private void readPiece(){
    
  }


  private void printPieces(){
    for(int i = 0; i < pieceRec.length; i++){
      if(pieceRec[i] != null)
        System.out.print("1");
      else
        System.out.print("0");

    }
  }

  public void setPeers(List<Map<String, Object>> RUPeers){
    this.RUPeers = RUPeers;
  }

}

 class ConnectionTest{
  public static void main(String[] args){
    Connection cnt = new Connection(10);
    cnt.run();
  }
}
