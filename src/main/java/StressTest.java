import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StressTest {
    private final static Logger logger = LogManager.getLogger(StressTest.class);
    
    public void randomGen_1_200ms(String _uri, int clientCount, int durationSec) {
        int threadCount = clientCount;
        String uri = _uri;
        ExecutorService service = Executors.newCachedThreadPool();
        
        for(int i = 0; i < threadCount; i++)
        {
            try {
                service.execute(new RtspClient(uri, 4000));
            } catch (IOException e) {
                e.printStackTrace();
            }
            logger.debug("Thread #" + i + " added");
        }
        
        service.shutdown();
        try {
            if( !service.awaitTermination(durationSec, TimeUnit.SECONDS) ) {
                service.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            service.shutdownNow();
        }
    }
/*
    public static void main(String[] args) {
        List<Thread> threadList = new ArrayList<>();
        Random r = new Random();

        if("aws_inc".equals(args[0])) {
            int threadCount = Integer.parseInt(args[1]);
            for (int i = 0; i < threadCount; i++) {
                Thread t = null;
                try {
                    t = new Thread(new RtspClient(4000));
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                t.start();
                threadList.add( t);
                logger.debug("Thread #" + i + " added");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        else if("aws_static".equals(args[0])) {
            int threadCount = Integer.parseInt(args[1]);
            for(int i = 0; i < threadCount; i++) {
                Thread t = null;
                RtspClient client = null;
                try {
                    client = new RtspClient(4000);
                    t = new Thread(client);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                t.start();
                threadList.add(t);
                logger.debug("Thread #" + i + " added");
                try {
                    Thread.sleep(r.nextInt(20)+1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    logger.debug("Server Address : " + client.getUri());
                }
            }

            for(int i = 0; i < threadCount; i++) {
                try {
                    threadList.get(i).join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else if ("aws_gls".equals(args[0])) {
            int threadCount = Integer.parseInt(args[1]);
            int loopCount = Integer.parseInt(args[2]);
            int bitrate = 0;
            for(int j = 0; j < loopCount; j++) {
                for (int i = threadList.size(); i < threadCount; i++) {
                    if(i%10 < 2) bitrate = 750;
                    else if(i%10 < 5) bitrate = 2000;
                    else bitrate = 4000;
                    Thread t = null;
                    try {
                        t = new Thread(new RtspClient(bitrate));
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                    t.start();
                    threadList.add(t);
                    logger.debug("Thread #" + i + "added");
                    try {
                        Thread.sleep(r.nextInt(1500) + 1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(1000*60*5);
                    for(int i = threadList.size()-1 ; i > threadCount/10; i--) {
                        threadList.remove(i).interrupt();
                        logger.debug("Thread #" + i + " interrupted");
                    }
                    Thread.sleep(1000*60*10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while(threadList.size() > 0) {
                threadList.remove(0).interrupt();
            }
        } else if ("loopback".equals(args[0])) {
            int threadCount = Integer.parseInt(args[1]);
            int bitrate = 4000;
            for (int i = 0; i < threadCount; i++) {
                Thread t = null;
                try {
                    t = new Thread(new RtspClient("127.0.0.1", 4000));
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                t.start();
                threadList.add( t);
                logger.debug("Thread #" + i + " added");

                try {
                    Thread.sleep(r.nextInt(500)+1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for(int i = 0; i < threadCount; i++) {
                try {
                    threadList.get(i).join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        } else if("customserver".equals(args[0])) {
        }
    }*/
}
