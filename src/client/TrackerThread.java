/*
 * Jamie and Jungsoo Park
 *
 * Tracker thread that continuously updates the tracker
 *
 */

package client;

import java.io.*;
import java.util.*;
import java.net.*;
import GivenTools.*;
import java.nio.ByteBuffer;

public class TrackerThread implements Runnable{
  private String infoHash;
  private String peerId;
  private String uploaded;
  private String downloaded;
  private String left;
  private String event;
  private String host;

  private Thread thread;
  private static Torrent torr;
  private Map<ByteBuffer, Object> response;
  private List<Map<String, Object>> allPeers;
  private static final ByteBuffer KEY_PEERS = ByteBuffer.wrap(new byte[] 
                  { 'p', 'e', 'e', 'r', 's' });

  public TrackerThread(TorrentInfo info, byte[] PEER_ID){
    this.infoHash = toHexString(info.info_hash.array());
    this.peerId = toHexString(PEER_ID);
    uploaded = "" + 0;
    downloaded = "" + 0;
    left = "" + String.valueOf(info.file_length);
    makeConnection(info);
  }

  public void start(){
    if(thread == null)
      thread = new Thread(this);
    thread.start();

  }

  public void run(){
    while(true){
      try{
        //System.out.println("response interval: " + Integer.valueOf( response.get(torr.getINTERVAL())));
        thread.sleep((long)120*2000);
        System.out.println("Updating tracker...");
        HttpURLConnection con = (HttpURLConnection) new URL(host + buildQueryString()).openConnection();
        updateValues();
      }catch(IOException e){
        System.err.println("ERROR: Tracker could not connect.");
        e.printStackTrace();
      }catch(InterruptedException e){
        System.err.println("ERROR: Tracker could not connect- Interrupted");
        e.printStackTrace();

      }
    }

  }

  private void makeConnection(TorrentInfo info){
    event="started";
    host = info.announce_url.toString();
    String qs = buildQueryString();
    byte[] responseBytes = null;
    System.out.println("Connecting to TRACKER... ");


    try { // Connecting
      HttpURLConnection con = (HttpURLConnection) new URL(host + qs).openConnection();
      InputStream in = con.getInputStream();
      responseBytes = new byte[in.available()];
      in.read(responseBytes);

    } catch (IOException e) {
      System.err.println("Failure!\nERROR: Failed to connect to tracker.");
      e.printStackTrace();
    }

    response = null;
    try {
      response = (Map<ByteBuffer, Object>) Bencoder2.decode(responseBytes);
    } catch (BencodingException e) {
      System.err.println("Failure!\nERROR: Could not decode tracker response.");
      e.printStackTrace();
    }
    this.allPeers = (List<Map<String, Object>>) response.get(KEY_PEERS);
  
    
  }
  
  private void updateValues(){
    uploaded = "" + torr.getUploaded();
    downloaded = "" + torr.getDownloaded();
    left = "" + torr.getLeft();
  }

  private String buildQueryString(){
    String qs = "?info_hash=" + infoHash
                + "&peer_id=" + peerId
                + "&downloaded=" + downloaded
                + "&uploaded=" + uploaded
                + "&left=" + left
                + "&event=" + event;
    return qs;
  }

  private static String toHexString(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 3);
    char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    for (int i = 0; i < bytes.length; i++) {
      byte b = bytes[i];
      byte hi = (byte) ((b >> 4) & 0x0f);
      byte lo = (byte) (b & 0x0f);
      sb.append('%').append(hex[hi]).append(hex[lo]);
    }

    return sb.toString();
  }

  public List<Map<String, Object>> getAllPeers(){
    return allPeers;
  }

  public void setTorrent(Torrent torr){
    this.torr = torr;
  }
}
