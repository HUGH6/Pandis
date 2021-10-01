package command.instance;

import client.InnerClient;
import server.PandisServer;

/**
 * @Description GET命令实现 用于根据key获取string类型的value
 * @Author huzihan
 * @Date 2021-07-21
 */
public class GetCommand extends GenericGetCommand {
    public GetCommand(PandisServer server) {
        super("get", 2, false, "r");
    }

    @Override
    public void execute(InnerClient client) {
        genericGet(client);
    }
}
