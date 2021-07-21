package server.config;

/**
 * @description:
 * @author: huzihan
 * @create: 2021-07-04
 */
public class ServerConfig {
    private String configfile;      // 配置文件路径
    private int port;               // 服务器默认端口
    private int hz;                 // serverCron每秒调用次数
    private int dbNumber;           // 数据库数量
    private String requirePassword; // 是否设置了密码

    public static final int DEFAULT_PORT = 6379;
    public static final int DEFAULT_DB_NUMBER = 16;

    private ServerConfig() {
        super();

        this.port = DEFAULT_PORT;
        this.dbNumber = DEFAULT_DB_NUMBER;
    }

    public static ServerConfig build() {
        return build(null);
    }

    public static ServerConfig build(ServerConfigBuilder builder) {
        ServerConfig serverConfig = new ServerConfig();

        if (builder != null) {
            serverConfig.port = builder.port;
            serverConfig.dbNumber = builder.dbNumber;
        }

        return serverConfig;
    }

    public int getPort() {
        return this.port;
    }

    public int getDbNumber() {
        return this.dbNumber;
    }

    public static class ServerConfigBuilder {
        private int port = DEFAULT_PORT;    // 服务器默认端口
        private int dbNumber = DEFAULT_DB_NUMBER;

        public ServerConfigBuilder setPort(int port) {
            this.port = port;
            return this;
        }

        public ServerConfigBuilder setDbNumber(int dbNumber) {
            this.dbNumber = dbNumber;
            return this;
        }
    }

    /**
     * 获取是否设置了密码
     * @return
     */
    public String getRequirePassword() {
        return this.requirePassword;
    }
}
