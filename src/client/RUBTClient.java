/**
 * @author Jungsoo Park
 * @author Jamie Liao
 *
 * CS352 Project - RUBTClient.java
 */

package client;

import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;

import GivenTools.*;

public class RUBTClient {

    // Unique, arbitrary peer id
    private static final byte[] PEER_ID =
        { 'j', 'u', 'n', 'g', 's', 'o', 'o', 'p', 'a', 'r', 
          'k', 'j', 'a', 'm', 'i', 'e', 'l', 'i', 'a', 'o' };

    private static final ByteBuffer KEY_PEERS = ByteBuffer.wrap(new byte[] 
            { 'p', 'e', 'e', 'r', 's' });

    private static final ByteBuffer KEY_PEER_ID = ByteBuffer.wrap(new byte[] 
            { 'p', 'e', 'e', 'r', ' ', 'i', 'd' });

    private static final ByteBuffer KEY_IP = ByteBuffer.wrap(new byte[] 
            { 'i', 'p' });

    private static final ByteBuffer KEY_PORT = ByteBuffer.wrap(new byte[] 
            { 'p', 'o', 'r', 't' });

    private static TorrentInfo getTorrentInfo(String fileName) throws IOException {

        byte[] torrentFileBytes = null;
        try {
            torrentFileBytes = readFile(fileName);
        } catch (FileNotFoundException e) {
            System.err.println("ERROR: Torrent file not found.");
            e.printStackTrace();
        }

        TorrentInfo torrentInfo = null;
        try {
            torrentInfo = new TorrentInfo(torrentFileBytes);
        } catch (BencodingException e) {
            System.err.println("ERROR: Can't get torrent info.");
            e.printStackTrace();
        }

        return torrentInfo;
    }

    private static byte[] readFile(String fileName) throws IOException {
        RandomAccessFile f = new RandomAccessFile(new File(fileName), "r");
        byte[] data = new byte[(int) f.length()];
        f.readFully(data);
        f.close();
        return data;
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

    private static String getQueryString(TorrentInfo info) {

        String infoHash = toHexString(info.info_hash.array());
        String peerId = toHexString(PEER_ID);
        String ip = info.announce_url.getHost().toString();
        String port = String.valueOf(info.announce_url.getPort());
        String event = "started";
        String uploaded = "" + 0;
        String downloaded = "" + 0;
        String left = String.valueOf(info.file_length);
        // String noPeerId = "1";
        // String compact = "0";

        String qs = "?info_hash=" + infoHash
                    + "&peer_id=" + peerId
                    + "&downloaded=" + downloaded
                    + "&uploaded=" + uploaded
                    + "&left=" + left
                    + "&event=" + event;

        return qs;
    }

    private static String getStringFrom(ByteBuffer byteString) {
        byte[] bytes = byteString.array();
        String res = "";
        for (int i = 0; i < bytes.length; i++) {
            res += (char) bytes[i];
        }

        return res;
    }

    private static void sendHandShake(Socket peerSock, TorrentInfo metaInfo) throws IOException{
            DataOutputStream peerOut = new DataOutputStream(peerSock.getOutputStream());
            peerOut.writeByte(19);
            peerOut.write("BitTorrent protocol".getBytes());
            peerOut.write(new byte[8]);
            peerOut.write(metaInfo.info_hash.array());
            peerOut.write(PEER_ID);
    }

    private static boolean receivedHandShake(DataInputStream in, TorrentInfo metaInfo, Map<String, Object> peer) throws Exception{
        try{
            int pstrlen = in.readByte();
            if(pstrlen != 19)
              throw new Exception("Wrong protocol length of " + pstrlen);

            byte[] pstr = new byte[pstrlen];
            in.read(pstr);
            if(!Arrays.equals(pstr, "BitTorrent protocol".getBytes()))
              throw new Exception("Protocols do not match!");

            byte[] reserved = new byte[8];
            in.read(reserved);
            if(!Arrays.equals(reserved, new byte[8]))
              throw new Exception("Reserved bytes not matched!");

            byte[] infoHash = new byte[20];
            in.read(infoHash);
            if(!Arrays.equals(infoHash, metaInfo.info_hash.array()))
              throw new Exception("Info hash not the same! ");

            byte[] recPeerID = new byte[20];
            in.read(recPeerID);
            String peerId = getStringFrom((ByteBuffer) peer.get(KEY_PEER_ID));
            if(Arrays.equals(recPeerID, peerId.getBytes()))
              throw new Exception("Peer IDs are the same!");

            System.out.println("apples");
        }catch(IOException e){
          System.out.println("Problem parsing header!");
          throw e;
        }
        return false;
      
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("ERROR: Enter the location to a .torrent file.");
            System.exit(1);
        }

        TorrentInfo metaInfo = null;
        try {
            metaInfo = getTorrentInfo(args[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* Build query string */
        String host = metaInfo.announce_url.toString();
        String qs = getQueryString(metaInfo);

        byte[] responseBytes = null;

        try { // Connecting
            HttpURLConnection con = (HttpURLConnection) new URL(host + qs).openConnection();
            System.out.println(con.getResponseCode() + " " + con.getResponseMessage() + '\n');

            InputStream in = con.getInputStream();
            responseBytes = new byte[in.available()];
            in.read(responseBytes);

        } catch (IOException e) {
            System.err.println("ERROR: Failed to connect to tracker.");
            e.printStackTrace();
        }

        Map<ByteBuffer, Object> response = null;
        try {
            response = (Map<ByteBuffer, Object>) Bencoder2.decode(responseBytes);
        } catch (BencodingException e) {
            System.err.println("ERROR: Could not decode tracker response.");
            e.printStackTrace();
        }

        List<Map<String, Object>> peers = (List<Map<String, Object>>) response.get(KEY_PEERS);
        Map<String, Object> peer = null;

        // Get the peer prefixed with "-RU"
        for (Map<String, Object> p : peers) {
            String peerId = getStringFrom((ByteBuffer) p.get(KEY_PEER_ID));
            if (peerId.startsWith("-RU")) {
                peer = p;
                break;
            }
        }

        ToolKit.print(peer);
        String peerIp = getStringFrom((ByteBuffer) peer.get(KEY_IP));
        int peerPort = (int) peer.get(KEY_PORT);


        try {
            Socket peerSock = new Socket(peerIp, peerPort);
            DataOutputStream peerOut = new DataOutputStream(peerSock.getOutputStream());
            BufferedReader peerIn = new BufferedReader(
                    new InputStreamReader(
                    new DataInputStream(peerSock.getInputStream())));
            DataInputStream in = new DataInputStream(peerSock.getInputStream());
            //building handshake message
            sendHandShake(peerSock, metaInfo);

            //check if returned handshake is correct
            boolean protocolSuccess = receivedHandShake(in, metaInfo, peer);

            /*
            int pstrlen = 19;
            byte init = (byte)pstrlen;
            byte[] pstr = new String("BitTorrent protocol").getBytes();
            byte[] reserved = new byte[8];
            Arrays.fill(reserved, (byte)0);

            byte[] initMsg = { 19, 'B', 'i', 't', 'T', 'o', 'r', 'r', 'e', 
                'n', 't', ' ', 'p', 'r', 'o', 't', 'o', 'c', 'o', 'l', '0',
                '0', '0', '0', '0', '0', '0', '0' };
            initMsg = (byte[]) ArrayUtils.addAll(initMsg, metaInfo.info_hash.array());
            initMsg = (byte[]) ArrayUtils.addAll(initMsg, PEER_ID);
            */

            /*
            String initMsg = (new Integer(19)).byteValue() + "BitTorrent protocol00000000" + 
                getStringFrom(metaInfo.info_hash) + 
                getStringFrom(ByteBuffer.wrap(PEER_ID));
                */

            //System.out.println(initMsg);
            //peerOut.writeBytes(initMsg);
            
            String line;

            while ((line = peerIn.readLine()) != null) {
                System.out.println(line);
            }

        } catch (UnknownHostException uhe) {
            System.err.println("ERROR: Unknown host.");
            uhe.printStackTrace();
        } catch (IOException e) {
            System.err.println("ERROR: Failed to connect to peer.");
            e.printStackTrace();
        } catch (Exception e){
            System.out.println("ERROR: Incorrect protocol header.");
            e.printStackTrace();
        }

    }
}
