package server.config;

/**
 * @description:
 * @author: huzihan
 * @create: 2021-07-04
 */
public class ServerConfig {
    private int port;    // 服务器默认端口

    private ServerConfig() {
        super();

        this.port = 6379;
    }

    public static ServerConfig build() {
        return build(null);
    }

    public static ServerConfig build(ServerConfigBuilder builder) {
        ServerConfig serverConfig = new ServerConfig();

        if (builder != null) {
            serverConfig.port = builder.port;
        }

        return serverConfig;
    }

    public int getPort() {
        return this.port;
    }

    public static class ServerConfigBuilder {
        private int port = 6379;    // 服务器默认端口

        public ServerConfigBuilder setPort(int port) {
            this.port = port;
            return this;

        }
    }

    public static void main(String[] args) {
        ServerConfigBuilder builder = new ServerConfigBuilder();
        builder
                .setPort(7878)
                .setPort(45)
                .setPort(4234);

        ServerConfig config = ServerConfig.build(builder);

    }
}
