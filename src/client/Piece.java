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
