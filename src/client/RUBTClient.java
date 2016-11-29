/*
 * @author Jungsoo Park
 * @author Jamie Liao
 *
 * CS352 Project - RUBTClient.java
 */

package client;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
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

    /*
     * @param Bytebuffer to convert
     * @return the bytestring in string form
     *
     */

    public static String getStringFrom(ByteBuffer byteString) {
        byte[] bytes = byteString.array();
        String res = "";
        for (int i = 0; i < bytes.length; i++) {
            res += (char) bytes[i];
        }

        return res;
    }
    
    
    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("ERROR: Not enough inputs.");
            System.exit(1);
        }

        long start = System.nanoTime();

        String outFileName = args[1];
        TorrentInfo metaInfo = null;
        try {
            metaInfo = getTorrentInfo(args[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        //start user input thread to check for 'quit' to end the program and save their progress
        InputThread it = new InputThread();
        it.start();


        //Create tracker thread that continuously updates the tracker
        TrackerThread tt = new TrackerThread(metaInfo, PEER_ID);

        //Find lowest RTT of peers... Peers class is kinda useless right now
        Peers peers = new Peers(tt.getAllPeers());

        //Store all the information of the torrent into this object for easy access
        Torrent torr = new Torrent(metaInfo, peers, outFileName);
        it.setTorr(torr);
        tt.setTorrent(torr);
        tt.start();

        //Connects all acceptable peers and downloads from them via multithreading
        ThreadConnection conn = new ThreadConnection(torr.getPieceCount(), peers, torr, outFileName);
        conn.run();

        while(!torr.getFinished()){
          try{
            TimeUnit.SECONDS.sleep(5);
          }catch(InterruptedException e){
            System.out.println("Could not wait.. :(");
          }
        }

        System.out.println("Finished downloading!\nUploading tracker...");
        tt.sendFinished();

        long end = System.nanoTime();
        double totalTime = (double)(end-start)/1000000000.0;
        System.out.println("Total time to download file is " + (int)totalTime/60 + " minutes and " + totalTime%60 + "s");
        System.exit(0);
    }
}
