package admobilize.matrix.malosclient;

/**
 * Created by Antonio Vanegas @hpsaturn on 11/10/16.
 */

public class MalosTarget {

    public static final int GPIO = 20049;
    public static final int HUMIDITY = 20017;
    public static final int UV = 20013 + (4 * 4);

    private int baseport;

    public MalosTarget(int baseport) {
        this.baseport = baseport;
    }

    public String getBaseport() {
        return "tcp://"+Config.CREATORIP+":"+baseport;
    }

    public String getPushPort() {
        return "tcp://"+Config.CREATORIP+":"+(baseport+1);
    }

    public String getErrorPort() {
        return "tcp://"+Config.CREATORIP+":"+(baseport+2);
    }

    public String getSubPort() {
        return "tcp://"+Config.CREATORIP+":"+(baseport+3);
    }

}
