package admobilize.matrix.malosclient.malos;

import admobilize.matrix.malosclient.Config;

/**
 * Created by Antonio Vanegas @hpsaturn on 11/10/16.
 */

public class MalosTarget {

    public static final int DEVICEINFO = 20012;
    public static final int GPIO       = 20049;
    public static final int HUMIDITY   = 20017;
    public static final int UV         = 20013 + (4 * 4);
    public static final int EVERLOOP   = 20013 + 8;

    private int baseport;
    private String host;

    public MalosTarget(int baseport) {
        this.baseport = baseport;
        this.host=Config.CREATORIP;
    }

    public MalosTarget(int baseport,String host) {
        this.baseport = baseport;
        this.host=host;
    }

    public String getBaseport() {
        return "tcp://"+host+":"+baseport;
    }

    public String getPushPort() {
        return "tcp://"+host+":"+(baseport+1);
    }

    public String getErrorPort() {
        return "tcp://"+host+":"+(baseport+2);
    }

    public String getSubPort() {
        return "tcp://"+host+":"+(baseport+3);
    }

}
