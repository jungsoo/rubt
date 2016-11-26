/*
 *
 * Jamie Liao and Jungsoo Park
 *
 * This class contains all the information about a piece that is downloaded. We need to store this information because it will be used to upload various pieces to a peer that requests it.
 *
 */

package client;

import java.io.*;
import java.util.*;
import java.net.*;

public class Piece{
  private int len;
  private byte msgId;
  private int index;
  private int begin;
  private int pieceLength;
  private byte[] pieceData;

  public Piece(int len, byte msgId, int pieceLength){
    this.len = len;
    this.msgId = msgId;
    this.pieceLength = pieceLength;
  }

  public int getLen(){return len;}
  public byte getMsgId(){return msgId;}
  public int getIndex(){return index;}
  public int getBegin(){return begin;}
  public int getPieceLength(){return pieceLength;}
  public byte[] getPieceData(){return pieceData;}

  public void setIndex(int index){
    this.index = index;
  }
  
  public void setBegin(int begin){
    this.begin = begin;
  }

  public void setPieceData(byte[] pieceData){
    this.pieceData = pieceData;
  }


}
