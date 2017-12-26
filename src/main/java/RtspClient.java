import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by wooseok on 17. 8. 9.
 */
public class RtspClient implements Runnable {
    private static final Logger logger = LogManager.getLogger(RtspClient.class);

    private static final int SERVER_PORT = 10353;
    private static final String STRING_RTSP_VER = "RTSP/1.0";
    private static final String STRING_USER_AGENT = "User-Agent: LibVLC/2.2.4 (LIVE555 Streaming Media v2016.02.22)";
    private static final String STRING_CRLF = "\r\n";

    private InetSocketAddress mlsServerAddr;
    private SocketChannel rtspChannel;

    private Map<Integer, PublishingPoint> _publishingPoints;
    private String session;
    private UdpClient udpClient;
    private String serverAddr;
    private int cseq;
    private int bitrate = 0;
    
    public String getServerAddr() {
        return serverAddr;
    }

    public RtspClient(int _bitrate) throws IOException {
        serverAddr = getTargetServer("https://kcm3itpdu3.execute-api.ap-northeast-2.amazonaws.com/beta/");
        mlsServerAddr = new InetSocketAddress(serverAddr, SERVER_PORT);
        _publishingPoints = new HashMap<>();
        cseq = 0;
        this.bitrate = _bitrate;
    }

    public RtspClient(String _serverAddr, int _bitrate) throws IOException {
        serverAddr = _serverAddr;
        mlsServerAddr = new InetSocketAddress(serverAddr, SERVER_PORT);
        _publishingPoints = new HashMap<>();
        cseq = 0;
        this.bitrate = _bitrate;
    }

    private String getTargetServer(String apiAddr) throws IOException {
        URL url = new URL(apiAddr);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setAllowUserInteraction(false);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-type", "text/plain");
        conn.setRequestProperty("accept", "text/plain");
        conn.setRequestProperty("x-api-key", "nHK1yUhL1s6kF8RuRLT3Hzd9xkLdHyoIu2wEdU80");

        int rspCode = conn.getResponseCode();
        if(rspCode == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String ret = br.readLine();
            return ret.replace("\"", "");
        }
        return null;
    }

    public void connect() throws IOException {
        rtspChannel = SocketChannel.open(mlsServerAddr);
    }

    private int sendMessage(String request) throws IOException {
        return rtspChannel.write(ByteBuffer.wrap(request.getBytes()));
    }

    public String recvResponse() throws IOException {
        ByteBuffer recvBuffer = ByteBuffer.allocate(1024*20);
        rtspChannel.read(recvBuffer);
        return new String(recvBuffer.array()).trim();
    }

    public void sendOptions() throws IOException {
        StringBuilder builder = new StringBuilder()
                .append("OPTIONS rtsp://").append(serverAddr).append(" ").append(STRING_RTSP_VER)
                .append(STRING_CRLF)
                .append("CSeq: ").append(cseq).append(STRING_CRLF)
                .append(STRING_USER_AGENT).append(STRING_CRLF)
                .append(STRING_CRLF);
        cseq++;
        sendMessage(builder.toString());
    }

    public void sendDescribe() throws IOException {
        StringBuilder builder = new StringBuilder()
                .append("DESCRIBE rtsp://").append(serverAddr).append("/live/ch01/multicast ")
                .append(STRING_RTSP_VER).append(STRING_CRLF)
                .append("CSeq: ").append(cseq).append(STRING_CRLF)
                .append(STRING_USER_AGENT).append(STRING_CRLF)
                .append(STRING_CRLF);
        cseq++;
        sendMessage(builder.toString());
    }

    public void getPublishingPoints(String describeResponse) throws IOException {
        BufferedReader bufReader = new BufferedReader(new StringReader(describeResponse));
        String line = null;
        String url = null;
        int bitrate = 0, apacketid = 0, vpacketid = 0;
        while( (line = bufReader.readLine()) != null ) {
            if(line.contains("m=video 0 MMTP/AVP")) {
                while( (line = bufReader.readLine()) != null ) {
                    if(line.startsWith("a=control:")) {
                        url = line.substring(10);
                    }
                    else if(line.startsWith("a=fmtp")) {
                        line = line.substring(line.indexOf("bitrate="));
                        String tokens[] = line.split(";");
                        for(String token : tokens) {
                            if(token.startsWith("bitrate=")) {
                                bitrate = Integer.parseInt(token.substring(8));
                                continue;
                            }
                            if(token.startsWith("apacketid=")) {
                                apacketid = Integer.parseInt(token.substring(10));
                                continue;
                            }
                            if(token.startsWith("vpacketid=")) {
                                vpacketid = Integer.parseInt(token.substring(10));
                            }
                        }
                        _publishingPoints.put(bitrate, new PublishingPoint(bitrate, apacketid, vpacketid, url));
                    }
                }
            }
        }
    }

    public void sendSetup() throws IOException {
        StringBuilder paramStringBuilder = new StringBuilder()
                .append("trs_faststart_option: 3000").append(STRING_CRLF);
        StringBuilder setupStringBuilder = new StringBuilder()
                .append("SETUP ").append(_publishingPoints.get(bitrate).getUrl()).append(" ")
                .append(STRING_RTSP_VER).append(STRING_CRLF)
                .append("CSeq: ").append(cseq).append(STRING_CRLF)
                .append("Transport: RTP/AVP;unicast;client_port=1234-1235").append(STRING_CRLF)
                .append("Content-Length: ").append(paramStringBuilder.length()).append(STRING_CRLF)
                .append(STRING_USER_AGENT).append(STRING_CRLF)
                .append(STRING_CRLF);
        cseq++;
        sendMessage(setupStringBuilder.toString());
        sendMessage(paramStringBuilder.toString());
    }

    public void setupSession(String setupResponse) throws IOException {
        BufferedReader bufReader = new BufferedReader(new StringReader(setupResponse));
        String line;
        while( (line = bufReader.readLine()) != null ) {
            if(line.startsWith("Session:")) {
                int end = line.indexOf(";");
                session = line.substring(9, end);
                break;
            }
        }

        udpClient = new UdpClient(serverAddr);
        udpClient.setSession(session);
        udpClient.sendSession();
    }

    public void sendPlay() throws IOException {
        StringBuilder builder = new StringBuilder()
                .append("PLAY ").append(_publishingPoints.get(bitrate).getUrl()).append(" ")
                .append(STRING_RTSP_VER).append(STRING_CRLF)
                .append("CSeq: ").append(cseq).append(STRING_CRLF)
                .append("Session: ").append(session).append(STRING_CRLF)
                .append(STRING_USER_AGENT).append(STRING_CRLF)
                .append(STRING_CRLF);
        cseq++;
        sendMessage(builder.toString());
        //logger.debug("====================\nPLAY REQUEST:\n" + builder.toString() + "\n\n");
    }

    public void sendMmtKeepAlive() throws IOException {
        String MMT_SERVER_INFO_KEY = "trs_server_info";
        StringBuilder builder = new StringBuilder()
                .append("GET_PARAMETER ").append(_publishingPoints.get(bitrate).getUrl()).append(" ")
                .append(STRING_RTSP_VER).append(STRING_CRLF)
                .append("CSeq: ").append(cseq).append(STRING_CRLF)
                .append("Session: ").append(session).append(STRING_CRLF)
                .append("Content-Type: text/plain").append(STRING_CRLF)
                .append("Content-Length: ").append(MMT_SERVER_INFO_KEY.length()).append(STRING_CRLF)
                .append(STRING_USER_AGENT).append(STRING_CRLF)
                .append(STRING_CRLF);
        cseq++;
        sendMessage(builder.toString());
        sendMessage(MMT_SERVER_INFO_KEY);
    }

    void sendTeardown() throws IOException {
        StringBuilder builder = new StringBuilder()
                .append("TEARDOWN ").append(_publishingPoints.get(bitrate).getUrl()).append(" ")
                .append(STRING_RTSP_VER).append(STRING_CRLF)
                .append("CSeq: ").append(cseq).append(STRING_CRLF)
                .append("Session: ").append(session).append(STRING_CRLF);
        cseq++;
        sendMessage(builder.toString());
        //logger.debug("====================\nTEARDOWN REQUEST:\n" + builder.toString() + "\n\n");
    }

    @Override
    public void run() {
        Random r = new Random();
        try {
            connect();
            sendDescribe();
            String res = recvResponse();
            //logger.debug("==================\nDESCRIBE response:\n" + res + "\n\n");
            getPublishingPoints(res);
            sendSetup();
            res = recvResponse();
            //logger.debug("==================\nSETUP response:\n" + res + "\n\n");
            setupSession(res);
            sendPlay();
            res = recvResponse();
            //logger.debug("==================\nPLAY response:\n" + res + "\n\n");

            while(!Thread.currentThread().isInterrupted()) {
                try {
                    sendMmtKeepAlive();
                    Thread.sleep(r.nextInt(10000)+10000);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    sendTeardown();
                    res = recvResponse();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
