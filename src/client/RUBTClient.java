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

import java.nio.charset.StandardCharsets;

import GivenTools.*;

public class RUBTClient {

    // Unique, arbitrary peer id
    private static final byte[] PEER_ID =
        { 'j', 'u', 'n', 'g', 's', 'o', 'o', 'p', 'a', 'r', 
          'k', 'j', 'a', 'm', 'i', 'e', 'l', 'i', 'a', 'o' };

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
        String noPeerId = "1";
        String compact = "0";

        String qs = "?info_hash=" + infoHash
                    + "&peer_id=" + peerId
                    + "&downloaded=" + downloaded
                    + "&uploaded=" + uploaded
                    + "&left=" + left
                    + "&event=" + event;

        return qs;
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

        try {
            HttpURLConnection con = (HttpURLConnection) new URL(host + qs).openConnection();
            con.setRequestMethod("GET");
            con.connect();

            System.out.println(con.getResponseCode() + " " + con.getResponseMessage() + '\n');

            InputStream in = con.getInputStream();

            byte[] responseBytes = new byte[in.available()];
            in.read(responseBytes);

            try {
                ToolKit.print(Bencoder2.decode(responseBytes));
            } catch (BencodingException e) {
                System.err.println("ERROR: Could not decode tracker response.");
                e.printStackTrace();
            }

        } catch (IOException e) {
            System.err.println("ERROR: Connection failed.");
            e.printStackTrace();
        }

    }
}
