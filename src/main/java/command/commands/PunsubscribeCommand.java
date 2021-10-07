package command.commands;

import command.AbstractCommand;
import common.struct.impl.Sds;
import pubsub.PubSub;
import server.ServerContext;
import server.client.InnerClient;

import java.util.List;

/**
 * @Description
 * @Author huzihan
 * @Date 2021/10/6
 **/
public class PunsubscribeCommand extends AbstractCommand {
    public PunsubscribeCommand() {
        super("punsubscribe", 1, true, "rpslt");
    }

    @Override
    public boolean checkCommandArgs(InnerClient client) {
        return true;
    }

    @Override
    public void doExecute(InnerClient client) {
        PubSub pubSub = ServerContext.getContext().getServerInstance().getPubSub();
        List<Sds> commandArgs = client.getCommandArgs();
        if (commandArgs.size() == 1) {
            pubSub.unsubscribeAllPatterns(client, true);
        } else {
            for (int i = 1; i < commandArgs.size(); i++) {
                pubSub.unsubscribePattern(client, commandArgs.get(i).toString(), true);
            }
        }
    }
}
