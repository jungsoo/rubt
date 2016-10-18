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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import GivenTools.*;

public class RUBTClient {

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

    private static String getQueryString(TorrentInfo info, String event) {
        // Make these parameters as our client gets more interesting
        String infoHash = toHexString(info.info_hash.array());
        String peerId = toHexString(PEER_ID);
        String ip = info.announce_url.getHost().toString();
        String port = String.valueOf(info.announce_url.getPort());
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

    private static void sendHandshake(DataOutputStream out, TorrentInfo info) throws IOException {
            out.writeByte(19);
            out.write("BitTorrent protocol".getBytes());
            out.write(new byte[8]);
            out.write(info.info_hash.array());
            out.write(PEER_ID);
    }

    // Verifies the handshake response from the peer
    private static void receiveHandshake(DataInputStream in, TorrentInfo info, Map<String, Object> peer) throws Exception {
        try {
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
            if(!Arrays.equals(infoHash, info.info_hash.array()))
              throw new Exception("Info hash not the same!");

            byte[] recPeerID = new byte[20];
            in.read(recPeerID);
            String peerId = getStringFrom((ByteBuffer) peer.get(KEY_PEER_ID));
            if(Arrays.equals(recPeerID, peerId.getBytes()))
              throw new Exception("Peer IDs are the same!");
        } catch (Exception e) {
            System.err.println("Failure!\nERROR: Bad peer response");
            e.printStackTrace();
        }

        System.out.println("Success!");
    }

    private static boolean messageIsUnchoked(int lengthPrefix, byte message) {
        return lengthPrefix == 1 && message == 1;
    }

    private static boolean messageIsChoked(int lengthPrefix, byte message) {
        return lengthPrefix == 1 && message == 0;
    }

    private static boolean messageIsPiece(byte message) {
        return message == 7;
    }

    private static ByteBuffer getSHA1Checksum(byte[] piece) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(piece);
            return ByteBuffer.wrap(digest.digest());
        } catch (NoSuchAlgorithmException nsae) {
            System.err.println(nsae.getLocalizedMessage());
        }
        return null;
    }

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("ERROR: Not enough inputs.");
            System.exit(1);
        }

        String outFileName = args[1];
        TorrentInfo metaInfo = null;
        try {
            metaInfo = getTorrentInfo(args[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* Build query string */
        String host = metaInfo.announce_url.toString();
        String qs = getQueryString(metaInfo, "started");

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

        List<Map<String, Object>> peers = (List<Map<String, Object>>) response.get(KEY_PEERS);
        Map<String, Object> peer = null;

        // Find the peer prefixed with "-RU"
        for (Map<String, Object> p : peers) {
            String peerId = getStringFrom((ByteBuffer) p.get(KEY_PEER_ID));
            if (peerId.startsWith("-RU")) {
                peer = p;
                break;
            }
        }

        System.out.println("Success!");

        String peerIp = getStringFrom((ByteBuffer) peer.get(KEY_IP));
        int peerPort = (int) peer.get(KEY_PORT);

        try { // Fun with the peer!
            Socket peerSock = new Socket(peerIp, peerPort);
            DataOutputStream out = new DataOutputStream(peerSock.getOutputStream());
            DataInputStream in = new DataInputStream(peerSock.getInputStream());

            System.out.print("Attempting to handshake with peer... ");
            sendHandshake(out, metaInfo);
            receiveHandshake(in, metaInfo, peer); // Verify peer response

            // Read bitfield, what is this for?
            int len = in.readInt();
            byte msgId = in.readByte();
            byte[] bitfield = new byte[len - 1];
            in.readFully(bitfield);

            System.out.print("Expressing interest to peer... ");
            out.writeInt(1);                // Message Length
            out.writeByte(2);               // Message ID

            len = in.readInt();
            msgId = in.readByte();
            
            if (messageIsUnchoked(len, msgId)) {   // Unchoked

                System.out.println("Unchoked!");
                final int pieceCount = metaInfo.piece_hashes.length;
                int pieceLength = metaInfo.piece_length;

                byte[] pieces = new byte[(pieceCount - 1) * pieceLength + 
                    metaInfo.file_length % pieceLength];
                System.out.println("The file is " + pieces.length + "B long.");
                System.out.println("Downloading...");

                for (int i = 0; i < pieceCount; i++) {

                    pieceLength = metaInfo.piece_length;
                    if (i == pieceCount - 1) {
                        pieceLength = metaInfo.file_length % pieceLength;
                    }

                    // Sending "request"
                    out.writeInt(13);               // Message Length
                    out.writeByte(6);               // Message ID
                    out.writeInt(i);                // Index
                    out.writeInt(0);                // Begin
                    out.writeInt(pieceLength);      // Length

                    // Read message header
                    len = in.readInt();
                    msgId = in.readByte();

                    if (messageIsPiece(msgId)) {
                        int index = in.readInt();
                        int begin = in.readInt();

                        if (i == pieceCount - 1) { // Last piece
                            pieceLength = in.available();
                        } else { // Wait until there is enough available bytes
                            while (in.available() < pieceLength) { }
                        }

                        in.readFully(pieces, i * metaInfo.piece_length, pieceLength);

                        byte[] currPiece = Arrays.copyOfRange(pieces,
                                i * metaInfo.piece_length,
                                i * metaInfo.piece_length + pieceLength);

                        ByteBuffer correctChecksum = metaInfo.piece_hashes[i];
                        ByteBuffer pieceChecksum = getSHA1Checksum(currPiece);

                        /* 
                        for (Byte b : correctChecksum.array())
                            System.out.print(b);
                        System.out.println();
                        for (Byte b : pieceChecksum.array())
                            System.out.print(b);
                        System.out.println();
                        */

                        if (correctChecksum.equals(pieceChecksum)) {
                            System.out.println(i + "\t" + currPiece);
                        } else {
                            System.err.println("ERROR: PIECE #" + i + " FAILED SHA-1 CHECK, TRYING AGAIN");
                            i--;
                        }
                    } else {
                        System.err.println("ERROR: PIECE #" + i + " NOT RECEIVED!");
                    }
                }

                FileOutputStream fout = new FileOutputStream(outFileName);
                fout.write(pieces);
                fout.close();
                System.out.println("File written to disk.");

                out.close();
                in.close();
                peerSock.close();
            }

        } catch (UnknownHostException uhe) {
            System.err.println("ERROR: Unknown host.");
            uhe.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            System.err.println("ERROR: Something went wrong lul.");
            e.printStackTrace();
        }

        try { // Telling the tracker that the download has completed
            qs = getQueryString(metaInfo, "completed");
            HttpURLConnection con = (HttpURLConnection) new URL(host + qs).openConnection();

            InputStream in = con.getInputStream();
            responseBytes = new byte[in.available()];
            in.read(responseBytes);

        } catch (IOException e) {
            System.err.println("Failure!\nERROR: Failed to connect to tracker.");
            e.printStackTrace();
        }
    }
}
