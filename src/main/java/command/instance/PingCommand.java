package command.instance;

import client.InnerClient;
import command.AbstractCommand;
import constants.StatusConstants;

/**
 * @Description
 * @Author huzihan
 * @Date 2021/10/3
 **/
public class PingCommand extends AbstractCommand {
    public PingCommand() {
        super("pong", 1, false, "rt");
    }

    @Override
    public void execute(InnerClient client) {
        client.replyStatus(StatusConstants.PONG_STATUS);
    }
}
