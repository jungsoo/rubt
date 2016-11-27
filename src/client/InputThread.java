/*
 *
 * Jamie Liao and Jungsoo Park
 *
 * Thread that catches the user's input in case of quitting.
 *
 */

package client;

import java.util.Scanner;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class InputThread implements Runnable{
  private boolean end;
  private Scanner sc;
  private Thread thread;
  private static Torrent torr;

  public InputThread(){
    System.out.println("Please enter 'quit' if you want to stop the download. You can resume the download at a later time.");
    sc = new Scanner(System.in);
  }

  public void start(){
    if(thread == null)
      thread = new Thread(this);
    thread.start();
  }

  public void run(){
    while(!end){
      String input = sc.next().toLowerCase();
      if(input.equals("quit")){
        end = true;
        saveProgress();
        System.out.println("Quitting...");
        System.exit(0);
      }
    }
  }

  public void setTorr(Torrent torr){
    this.torr = torr;
  }

  public void saveProgress(){
    System.out.println("Saving progress...");
    if(torr == null){
      return;
    }

    Save s = new Save(torr);
    try{
      FileOutputStream fileOut = new FileOutputStream(torr.getFileName() + ".ser");
      ObjectOutputStream out = new ObjectOutputStream(fileOut);
      out.writeObject(s);
      out.close();
      fileOut.close();
      System.out.println("Successfully saved!");
    }catch(IOException e){
      System.err.println("ERROR: Could not save information to file.");
      e.printStackTrace();
    }
  }
}
