package client;

public class PeerThread implements Runnable{
    private Thread thread;
    private String threadName;
    private static boolean end;
    private static int numThreads;

    public PeerThread(String threadName){
      this.threadName = threadName;
      incThreadCount();
    }

    @Override
    public void run(){
      //send handshake

    }

    private synchronized void incThreadCount(){
      numThreads++;
    }

}
