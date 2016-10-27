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
	
	List<Map<String, Object>> allPeers;
	List<Map<String, Object>> RUPeers;
	
	public Peers(String host, String qs){
		makeConnection(host, qs);
	
	}
	
	public List<Map<String, Object>> getAllPeers(){
		return allPeers;
	}
	
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
        ToolKit.print(allPeers);
        
        findPeers("-RU");
        
	}
	
	public Map<String, Object> findLowestRTT(){
		
		
	}
	
	private void findPeers(String prefix){
		RUPeers = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> p : allPeers) {
            String peerId = RUBTClient.getStringFrom((ByteBuffer) p.get(KEY_PEER_ID));
            if (peerId.startsWith(prefix)) {
                RUPeers.add(p);
                
            }
        }
		
		ToolKit.print(RUPeers);
	}
	
}