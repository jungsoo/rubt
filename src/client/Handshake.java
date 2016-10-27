package client;

import java.io.*;
import java.util.*;
import java.net.*;

import GivenTools.*;

public class Handshake{
	public int pstrlen;
	public byte[] pstr;
	public byte[] reserved;
	public byte[] infoHash;
	public byte[] recPeerID;
	
	public Handshake(DataInputStream in, TorrentInfo info, Map<String, Object> peer, String peerID) throws Exception{
		setPstrlen(in);
		setPstr(in);
		setReserved(in);
		setInfoHash(in, info);
		setRecPeerID(in, peer, peerID);
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
	
	private void setInfoHash(DataInputStream in, TorrentInfo info) throws Exception{
		byte[] infoHash = new byte[20];
        in.read(infoHash);
		if(!Arrays.equals(infoHash, info.info_hash.array())){
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
