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
 * @Date 2021/10/7
 **/
public class PublishCommand extends AbstractCommand {
    public PublishCommand() {
        super("publish", 3, false, "pltr");
    }

    @Override
    public boolean checkCommandArgs(InnerClient client) {
        return true;
    }

    @Override
    public void doExecute(InnerClient client) {
        PubSub pubSub = ServerContext.getContext().getServerInstance().getPubSub();
        List<Sds> commandArgs = client.getCommandArgs();

        Sds channelName = commandArgs.get(1);
        Sds message = commandArgs.get(2);
        int receivers = pubSub.publishMessage(channelName.toString(), message);

        client.replyInteger(receivers);
    }
}
