public class PublishingPoint {
    private int bitrate;
    private int apacketid;
    private int vpacketid;
    private String url;

    public PublishingPoint(int bitrate, int apacketid, int vpacketid, String url) {
        this.bitrate = bitrate;
        this.apacketid = apacketid;
        this.vpacketid = vpacketid;
        this.url = url;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getApacketid() {
        return apacketid;
    }

    public void setApacketid(int apacketid) {
        this.apacketid = apacketid;
    }

    public int getVpacketid() {
        return vpacketid;
    }

    public void setVpacketid(int vpacketid) {
        this.vpacketid = vpacketid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
