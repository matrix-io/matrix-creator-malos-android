package admobilize.matrix.malosclient.malos;

import admobilize.matrix.malosclient.Config;

/**
 * Created by Antonio Vanegas @hpsaturn on 11/10/16.
 */

public class MalosTarget {


    public static final int DEVICEINFO = 20012;
    public static final int IMU        = 20013;
    public static final int HUMIDITY   = 20017;
    public static final int EVERLOOP   = 20021;
    public static final int PRESSURE   = 20025;
    public static final int UV         = 20029;
    public static final int SERVO      = 20045;
    public static final int GPIO       = 20049;

    private int baseport;
    private String host;

    public MalosTarget(int baseport, String host) {
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

    public String getHost() {
        return host;
    }
}
