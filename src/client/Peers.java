/*
 *  @author Jungsoo Park
 *  @author Jamie Liao
 *
 *  CS352 BitTorrent Project - Peers.java
 *
 *  This class contacts the tracker for the list of peers. It also calculates the peer with the lowest average RTT and connects to it.
 *
 *
 */


package client;

import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;

import GivenTools.*;

public class Peers{
	private static final ByteBuffer KEY_PEERS = ByteBuffer.wrap(new byte[] 
            { 'p', 'e', 'e', 'r', 's' });
	private static final ByteBuffer KEY_PEER_ID = ByteBuffer.wrap(new byte[] 
            { 'p', 'e', 'e', 'r', ' ', 'i', 'd' });
	private static final ByteBuffer KEY_IP = ByteBuffer.wrap(new byte[] 
            { 'i', 'p' });
	private static final ByteBuffer INTERVAL = ByteBuffer.wrap(new byte[] 
            { 'i', 'n', 't', 'e', 'r', 'v', 'a', 'l' });
	
	private List<Map<String, Object>> allPeers;
	private List<Map<String, Object>> RUPeers;
  private Map<String, Object> peer;
	
	public Peers(List<Map<String, Object>> allPeers){
    this.allPeers = allPeers;
		//makeConnection(host, qs);
    try{
      peer = findLowestRTT();
    }catch(Exception e){
      System.err.println("Failure!\nERROR: Could not find lowest RTT peer.");
      e.printStackTrace();
    }
	}

  /*
   * Finds the peer with the lowest RTT and stores it into peer
   *
   * @return the peer with the lowest RTT
   */
  public Map<String,Object> getPeer(){
    return peer;
  }
	
  /*
	private void makeConnection(String host, String qs){
	  byte[] responseBytes = null;
    System.out.print("Connecting to TRACKER... ");

    try { // Connecting
      HttpURLConnection con = (HttpURLConnection) new URL(host + qs).openConnection();
      InputStream in = con.getInputStream();
      responseBytes = new byte[in.available()];
      in.read(responseBytes);

    } catch (IOException e) {
      System.err.println("Failure!\nERROR: Failed to connect to tracker.");
      e.printStackTrace();
    }

    Map<ByteBuffer, Object> response = null;
    try {
      response = (Map<ByteBuffer, Object>) Bencoder2.decode(responseBytes);
    } catch (BencodingException e) {
      System.err.println("Failure!\nERROR: Could not decode tracker response.");
      e.printStackTrace();
    }
    this.allPeers = (List<Map<String, Object>>) response.get(KEY_PEERS);
    System.out.println("response.get interval: " + response.get(INTERVAL));

	}
  */

  /* Calculates the RU peer with the lowest RTT
   *
   * @return the peer with the lowest RTT by calculating the RTT to each peer in RUPeers everytime
  */

  public Map<String, Object> findLowestRTT() throws Exception{
    findPeers("-RU1103");
    String prefix = "172.16.97";
    long minAvgPing = Long.MAX_VALUE;
    Map<String, Object> returnPeer = null;

	  for (Map<String, Object> p : RUPeers) {
      String peerIP = RUBTClient.getStringFrom((ByteBuffer) p.get(KEY_IP));
      if(peerIP.startsWith(prefix) && (peerIP.contains(".11") || peerIP.contains(".12") || peerIP.contains(".13"))){
        //find avg ping per ip
        long avgPing = findAvgPing(peerIP);
        if(avgPing < minAvgPing){
          minAvgPing = avgPing;
          returnPeer = p;
        }
        System.out.println("avgPing for " + peerIP + " is " + avgPing);
      }

    //ToolKit.print(returnPeer);
    }

    return returnPeer;
  }

  private long findAvgPing(String peerIP) throws Exception{
    long avgPing = 0;
    for(int i = 0; i < 10; i ++){
      InetAddress inet = InetAddress.getByName(peerIP);
      //System.out.println("\n" + i + ": Sending ping to " + peerIP);
      long finish = 0;
      long start = new GregorianCalendar().getTimeInMillis();
      if(inet.isReachable(5000)){
        finish = new GregorianCalendar().getTimeInMillis();
        avgPing += finish-start;
        //System.out.println( i + " Ping is: " + (finish-start) + "ms");
      }else{
        System.err.println("Failure!\nERROR: Could not reach IP!.");
      }
    }
    return avgPing;
    
  }
	
	private void findPeers(String prefix){
	  RUPeers = new ArrayList<Map<String, Object>>();
	  for (Map<String, Object> p : allPeers) {
      String peerId = RUBTClient.getStringFrom((ByteBuffer) p.get(KEY_PEER_ID));
      if (peerId.startsWith(prefix)) 
        RUPeers.add(p);
      
    }
  }

  public List<Map<String, Object>> getPeers(){
    return RUPeers;
  }

}
