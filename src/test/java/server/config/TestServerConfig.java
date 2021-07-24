package server.config;

import org.junit.Test;

/**
 * @description:
 * @author: huzihan
 * @create: 2021-07-24
 */
public class TestServerConfig {
    @Test
    public void testLoadConfigFromString() {
        String conf = "# slfjsljfoourourow\n" +
                "# kfjrjfslk\n" +
                "hz \t-10000 \t\n" +
                "daemonize\t no \n" +
                "databases 20\n" +
                "port 100\n" +
                "requirepass 2342424\n";

        ServerConfig config = ServerConfig.build();
        config.loadConfigFromString(conf);
    }

    @Test
    public void testLoadConfigFromFile() {
        String file = "pandis.conf";
        ServerConfig config = ServerConfig.build();
        config.loadConfigFromFile(file);
    }

}
