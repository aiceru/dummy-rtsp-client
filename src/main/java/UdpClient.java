import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.DatagramChannel;

public class UdpClient implements Runnable {
    private final static Logger logger = LogManager.getLogger(UdpClient.class);

    private final static int UDPSERVER_PORT = 19333;
    private final static int MAX_BUF = 4096;

    private DatagramChannel dChannel;
    private String session;
    private String udpServerAddr;
    
    boolean running;
    private long receivedBytes;

    public UdpClient(String serverAddr) throws IOException {
        this.udpServerAddr = serverAddr;
        this.dChannel = DatagramChannel.open();
        dChannel.socket().bind(new InetSocketAddress(0));
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public int send(String message) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(2048);
        buf.clear();
        buf.put(message.getBytes());
        buf.flip();

        return dChannel.send(buf, new InetSocketAddress(udpServerAddr, UDPSERVER_PORT));
    }

    public int holepunch() throws IOException {
        StringBuilder builder = new StringBuilder()
                .append("{\n")
                .append("\"cmd\": \"holepunch\",\n")
                .append("\"session\": \"").append(session).append("\"\n")
                .append("}");
        return send(builder.toString());
    }
    
    public void terminate() {
        running = false;
    }
    
    @Override
    public void run() {
        running = true;
        ByteBuffer buf = ByteBuffer.allocate(MAX_BUF);
        SocketAddress sa;
        receivedBytes = 0;
        
        while(running) {
            try {
                sa = dChannel.receive(buf);
                buf.flip();
                receivedBytes += buf.limit();
            } catch (ClosedByInterruptException e) {
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
