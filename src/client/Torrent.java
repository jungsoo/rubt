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

    private static  TorrentInfo info;
    private static  int pieceCount;
    private static  int pieceLength;
    private static  int file_length;
    private static  ByteBuffer[] piece_hashes;
    private static List<Map<String, Object>> RUPeers;
    private static byte[] info_hash;

    public Torrent(TorrentInfo info, List<Map<String, Object>> RUPeers){
      this.info = info;
      this.RUPeers = RUPeers;
      pieceCount = info.piece_hashes.length;
      pieceLength = info.piece_length;
      file_length = info.file_length;
      piece_hashes = info.piece_hashes;
      info_hash = info.info_hash.array();
    }

    //getters
    public byte[] getPEER_ID(){ return PEER_ID;}
    public ByteBuffer getKEY_PEERS(){ return KEY_PEERS;}
    public ByteBuffer getKEY_PEER_ID(){ return KEY_PEER_ID;}
    public ByteBuffer getKEY_IP(){ return KEY_IP;}
    public ByteBuffer getKEY_PORT(){ return KEY_PORT;}
    public TorrentInfo getTorrent(){ return info;}
    public int getPieceCount(){ return pieceCount;}
    public int getPieceLength(){ return pieceLength;}
    public int getFileLength(){ return file_length;}
    public ByteBuffer[] getPieceHashes(){ return piece_hashes;}
    public byte[] getInfoHash(){ return info_hash;}

}
