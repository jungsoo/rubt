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
    public static void main(String[] args) {
        String metaStr = "";
        File metaFile = new File(args[0]);
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(args[0]));
            String line;

            while ((line = reader.readLine()) != null) {
                metaStr += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {

            }
        }

        try {
            Bencoder2.decode(metaStr.getBytes(Charset.forName("UTF-8")));
            // TODO: Sigh...
        } catch (Exception e) {
            e.printStackTrace();
        }
        // System.out.println(decoded);
    }
}
