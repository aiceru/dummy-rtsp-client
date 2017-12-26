import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UdpClient {
    private final static Logger logger = LogManager.getLogger(UdpClient.class);

    private final static int UDPSERVER_PORT = 19333;

    private DatagramChannel dChannel;
    private String session;
    private String udpServerAddr;

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

    public int sendSession() throws IOException {
        StringBuilder builder = new StringBuilder()
                .append("{\n")
                .append("\"cmd\": \"holepunch\",\n")
                .append("\"session\": \"").append(session).append("\"\n")
                .append("}");
        return send(builder.toString());
    }
}
