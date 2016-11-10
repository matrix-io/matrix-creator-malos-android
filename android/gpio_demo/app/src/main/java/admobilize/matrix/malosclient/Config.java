package admobilize.matrix.malosclient;

/**
 * Created by Antonio Vanegas @hpsaturn on 11/4/16.
 */
public class Config {
    public static final String CREATORIP="192.168.3.128";

    public static final int    MALOS_GPIO_BASE_PORT = 20049;
    public static final String MALOS_GPIO_CONFIG = "tcp://"+CREATORIP+":"+MALOS_GPIO_BASE_PORT;
    public static final String MALOS_GPIO_SUB = "tcp://"+CREATORIP+":"+(MALOS_GPIO_BASE_PORT+3);

}
