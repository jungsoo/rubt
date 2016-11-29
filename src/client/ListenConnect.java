/*
 *
 * Jamie Liao and Jungsoo Park
 *
 * Thread that listens for incoming requests for uploads.
 *
 */

package client;

import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;
import GivenTools.*;

public class ListenConnect implements Runnable{
  private static ArrayList<String> uploadList;
  private ServerSocket serverSock;
  private static Torrent torr;
  private Thread thread;
  private Socket sock;
  private int port;

  public ListenConnect(Torrent torr){
    this.torr = torr;
    uploadList = new ArrayList<String>();
    port = 6881;
    while(true){
      System.out.println("port: " + port);
      try{
          serverSock = new ServerSocket(port);
          port = serverSock.getLocalPort();
          break;
      }catch(Exception e){
        port++;
      }
    }
  }

  public void start(){
    if(thread == null)
      thread = new Thread(this);
    thread.start();
  }

  public void run(){
    while(true){
      String connectIP = acceptConnections();
      Map<String, Object> connectPeer = null;
      for(Map<String, Object> p: torr.getAllPeers()){
        String tmp = RUBTClient.getStringFrom((ByteBuffer) p.get(torr.getKEY_IP()));
        if(connectIP.equals(tmp))
          connectPeer = p;
      }
      checkHandshake(connectPeer);
      UploadThread up = new UploadThread(connectIP, torr, sock);
    }
  }

  private synchronized void checkHandshake(Map<String, Object> peer){
    //System.out.println("   Attempting to handshake with peer...");
    try{
      DataInputStream in = new DataInputStream(sock.getInputStream());
      DataOutputStream out = new DataOutputStream(sock.getOutputStream());
      Handshake hs = new Handshake(out, in, torr, peer);
      hs.sendHandshake();
    }catch(UnknownHostException e){
      System.err.println("ERROR: Could not create socket to handshake.");
      //e.printStackTrace();
    }catch(IOException e){
      System.err.println("ERROR: Could not create socket to handshake.");
      //e.printStackTrace();
    }catch(Exception e){
      System.err.println("ERROR: Could not create socket to handshake.");
      //e.printStackTrace();
    }
  }

  private String acceptConnections(){
    System.out.println("Looking for connections...");
    try{
      sock = serverSock.accept();
    }catch(IOException e){
      System.err.println("ERROR: Could not create Server Socket.");
      //e.printStackTrace();
    }
    String clientIP = sock.getInetAddress().toString();
    System.out.println("Connected to " + clientIP);
    if(!uploadList.contains(clientIP)){
      uploadList.add(sock.getInetAddress().toString());
      return clientIP;
    }
    return null;
  }


}
