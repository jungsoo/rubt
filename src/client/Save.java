/*
 *
 * Jamie Liao and Jungsoo Park
 *
 * This object saves all information that has happened so far in the download and writes it to a file on the local machine called ____.ser. Upon restarting the program, it will search for that file and see if it is resuming a download or starting a new one completely.
 *
 */

package client;

import java.io.*;

public class Save implements java.io.Serializable{
  private boolean finished;
  private Piece[] pieceRec;
  private int uploaded;
  private int downloaded;
  private int left;
  private byte[] info_hash;
  private static final long serialVersionUID = 3373572515086712978L;


  public Save(Torrent torr){
    finished = (torr.getLeft() == 0) ? true : false;
    pieceRec = torr.getPieceRec();
    uploaded = torr.getUploaded();
    downloaded = torr.getDownloaded();
    left = torr.getLeft();
    info_hash = torr.getInfoHash();
  }

  //getters
  public boolean getFinished(){return finished;}
  public Piece[] getPieceRec(){return pieceRec;}
  public int getUploaded(){return uploaded;}
  public int getDownloaded(){return downloaded;}
  public int getLeft(){return left;}
  public byte[] getInfoHash(){return info_hash;}

}
