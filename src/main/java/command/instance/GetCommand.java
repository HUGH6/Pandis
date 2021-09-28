package command.instance;

import client.PandisClient;
import command.AbstractCommand;
import server.PandisServer;

/**
 * @Description GET命令实现
 * @Author huzihan
 * @Date 2021-07-21
 */
public class GetCommand extends AbstractCommand {
    // 实际命令接受者
    private PandisServer server;

    public GetCommand(PandisServer server) {
        super("get", 2, false, "r");
        this.server = server;
    }

    @Override
    public void execute(PandisClient client) {
        System.out.println("get 命令模拟");
    }
}
