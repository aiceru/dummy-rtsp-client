import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class StressTestTest {
    Logger logger;
    
    @Before
    public void setUp() throws Exception {
        logger = LogManager.getLogger(this.getClass());
    }
    
    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void randomGen_1_200ms() {
        StressTest st = new StressTest();
        st.randomGen_1_200ms("rtsp://61.253.126.134:10353/app01/ch01/test", 500, 600);
    }
}