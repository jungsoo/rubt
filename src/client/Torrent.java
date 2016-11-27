/*
 *
 * Jamie Liao and Jungsoo Park
 *
 *
 * This class contains all needed information related to the torrent itself. Makes it easier to transfer information between the classes and methods. Mainly consists of getters and setters. 
 *
 */

package client;

import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;
import GivenTools.*;

public class Torrent{
  // Unique, arbitrary peer id
    private static final byte[] PEER_ID =
            { 'J', 'u', 'n', 'g', 's', 'o', 'o', 'P', 'a', 'r', 
            'k', 'J', 'a', 'm', 'i', 'e', 'L', 'i', 'a', 'o' };

    private static final ByteBuffer KEY_PEERS = ByteBuffer.wrap(new byte[] 
            { 'p', 'e', 'e', 'r', 's' });

    private static final ByteBuffer KEY_PEER_ID = ByteBuffer.wrap(new byte[] 
            { 'p', 'e', 'e', 'r', ' ', 'i', 'd' });

    private static final ByteBuffer KEY_IP = ByteBuffer.wrap(new byte[] 
            { 'i', 'p' });

    private static final ByteBuffer KEY_PORT = ByteBuffer.wrap(new byte[] 
            { 'p', 'o', 'r', 't' });
    private static final ByteBuffer INTERVAL = ByteBuffer.wrap(new byte[] 
            { 'i', 'n', 't', 'e', 'r', 'v', 'a', 'l'} );

    private TorrentInfo info;
    private int pieceCount;
    private int pieceLength;
    private int file_length;
    private ByteBuffer[] piece_hashes;
    private List<Map<String, Object>> RUPeers;
    private byte[] info_hash;
    private int uploaded;
    private int downloaded;
    private int left;
    private static Piece[] pieceRec;
    private String outFileName;

    public Torrent(TorrentInfo info, List<Map<String, Object>> RUPeers, String outFileName){
      this.info = info;
      this.RUPeers = RUPeers;
      pieceCount = info.piece_hashes.length;
      pieceLength = info.piece_length;
      file_length = info.file_length;
      piece_hashes = info.piece_hashes;
      info_hash = info.info_hash.array();
      uploaded = 0;
      downloaded = 0;
      left = file_length;
      pieceRec = new Piece[pieceCount];
      this.outFileName = outFileName;
    }

    public void incDownload(int numBytes){
      downloaded += numBytes;
      left -= numBytes;
    }
    public void incUpdate(int numBytes){
      uploaded += numBytes;
    }
    //setters
    public void setDownloaded(int downloaded){
      this.downloaded = downloaded;
    }

    public void setUploaded(int uploaded){
      this.uploaded = uploaded;
    }

    public void setLeft(int left){
      this.left = left;
    }

    public void setPieceRec(Piece[] pieceRec){
      this.pieceRec = pieceRec;
    }



    //getters
    public byte[] getPEER_ID(){ return PEER_ID;}
    public ByteBuffer getKEY_PEERS(){ return KEY_PEERS;}
    public ByteBuffer getKEY_PEER_ID(){ return KEY_PEER_ID;}
    public ByteBuffer getKEY_IP(){ return KEY_IP;}
    public ByteBuffer getKEY_PORT(){ return KEY_PORT;}
    public ByteBuffer getINTERVAL(){ return INTERVAL;}
    public TorrentInfo getTorrent(){ return info;}
    public int getPieceCount(){ return pieceCount;}
    public int getPieceLength(){ return pieceLength;}
    public int getFileLength(){ return file_length;}
    public ByteBuffer[] getPieceHashes(){ return piece_hashes;}
    public byte[] getInfoHash(){ return info_hash;}
    public int getDownloaded(){return downloaded;}
    public int getUploaded(){return uploaded;}
    public int getLeft(){return left;}
    public TorrentInfo getTorrInfo(){return info;}
    public Piece[] getPieceRec(){return pieceRec;}
    public String getFileName(){return outFileName;}


}
