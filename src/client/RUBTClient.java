/**
 * @author Jungsoo Park
 * @author Jamie Liao
 *
 * CS352 Project - RUBTClient.java
 */

package client;

import java.io.*;
import java.util.*;
import java.nio.charset.Charset;

import GivenTools.*;

public class RUBTClient {

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

    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("ERROR: Enter the location to a .torrent file.");
            System.exit(-1);
        }

        try {
            TorrentInfo torrentInfo = getTorrentInfo(args[0]);
            System.out.println(torrentInfo);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
