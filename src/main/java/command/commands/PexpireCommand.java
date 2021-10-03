package command.commands;

import server.client.InnerClient;

import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Author huzihan
 * @Date 2021/10/3
 **/
public class PexpireCommand extends GenericExpireCommand {
    public PexpireCommand() {
        super("pexpire", 3, false, "w");
    }

    @Override
    public boolean checkCommandArgs(InnerClient client) {
        return true;
    }

    @Override
    public void doExecute(InnerClient client) {
        genericExpire(client, System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }
}
