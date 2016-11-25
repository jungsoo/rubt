/*
 *  @author Jungsoo Park
 *  @author Jamie Liao
 *
 *  CS352 BitTorrent Project - Handshake.java
 *
 *  This class contains all of the details needed to conduct the handshake
 *  with the peer.
 *
 */

package client;

import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;

import GivenTools.*;

public class Handshake{
	private int pstrlen;
	private byte[] pstr;
	private byte[] reserved;
	private byte[] infoHash;
	private byte[] recPeerID;
  private byte[] PEER_ID;
  private DataOutputStream out;
  private DataInputStream in;
  private Torrent torr;
	
	public Handshake(DataOutputStream out, DataInputStream in, Torrent torr, Map<String, Object> peer) throws Exception{
    this.out = out;
    this.in = in;
    this.torr = torr;
		setPstrlen(in);
		setPstr(in);
		setReserved(in);
		setInfoHash(in, torr);
		setRecPeerID(in, peer, getPeerID(peer));
	}

  private String getPeerID(Map<String, Object> peer){
    return getStringFrom((ByteBuffer)peer.get(torr.getKEY_PEER_ID()));
  }

  private static String getStringFrom(ByteBuffer byteString) {
        byte[] bytes = byteString.array();
        String res = "";
        for (int i = 0; i < bytes.length; i++) {
            res += (char) bytes[i];
        }

        return res;
  }

  public void sendHandshake() throws IOException{
    out.writeByte(19);
    out.write("BitTorrent protocol".getBytes());
    out.write(new byte[8]);
    out.write(infoHash);
    out.write(torr.getPEER_ID());
  }

  public boolean verifyHandshake(){
    return false;
  }
	
	private void setPstrlen(DataInputStream in) throws Exception{
		int pstrlen = in.readByte();
		if(pstrlen != 19){
            throw new Exception("Wrong protocol length of " + pstrlen);
		}
		this.pstrlen = pstrlen;
	}
	
	private void setPstr(DataInputStream in) throws Exception{
		byte[] pstr = new byte[pstrlen];
        in.read(pstr);
		if(!Arrays.equals(pstr, "BitTorrent protocol".getBytes())){
            throw new Exception("Protocols do not match!");
		}
		this.pstr = pstr;
	}
	
	private void setReserved(DataInputStream in) throws Exception{
		byte[] reserved = new byte[8];
        in.read(reserved);
		if(!Arrays.equals(reserved, new byte[8])){
            throw new Exception("Protocols do not match!");
		}
		this.reserved = reserved;
	}
	
	private void setInfoHash(DataInputStream in, Torrent torr) throws Exception{
		byte[] infoHash = new byte[20];
        in.read(infoHash);
		if(!Arrays.equals(infoHash, torr.getInfoHash())){
            throw new Exception("Info hash not the same!");
		}
		this.infoHash = infoHash;
	}
	
	private void setRecPeerID(DataInputStream in, Map<String, Object> peer, String peerID) throws Exception{
		byte[] recPeerID = new byte[20];
        in.read(recPeerID);
		
		if(Arrays.equals(recPeerID, peerID.getBytes())){
            throw new Exception("Peer IDs are the same!");
		}
		this.infoHash = infoHash;
	}

}
