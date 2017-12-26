import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.io.IOException;

public class aaaa {
    Logger logger = LogManager.getLogger(aaaa.class);

    @Test
    public void testaaaa() {
        RtspClient t = null;
        try {
            t = new RtspClient("https://kcm3itpdu3.execute-api.ap-northeast-2.amazonaws.com/beta/", 750);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            t.connect();
            t.sendDescribe();
            String res = t.recvResponse();
            //logger.debug("==================\nDESCRIBE response:\n" + res + "\n\n");
            t.getPublishingPoints(res);
            t.sendSetup();
            res = t.recvResponse();
            //logger.debug("==================\nSETUP response:\n" + res + "\n\n");
            t.setupSession(res);
            t.sendPlay();
            res = t.recvResponse();
            //logger.debug("==================\nPLAY response:\n" + res + "\n\n");

            t.sendMmtKeepAlive();
            t.recvResponse();
            Thread.sleep(20000);

            t.sendMmtKeepAlive();
            t.recvResponse();
            Thread.sleep(20000);

            t.sendMmtKeepAlive();
            t.recvResponse();
            Thread.sleep(20000);

            t.sendMmtKeepAlive();
            t.recvResponse();
            Thread.sleep(20000);

            t.sendTeardown();
            res = t.recvResponse();
            logger.debug(res);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
