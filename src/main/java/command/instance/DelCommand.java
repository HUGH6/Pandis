package command.instance;

import client.InnerClient;
import command.AbstractCommand;
import common.struct.impl.Sds;
import server.Database;
import server.ServerContext;

/**
 * @Description
 * @Author huzihan
 * @Date 2021/10/2
 **/
public class DelCommand extends AbstractCommand {
    public DelCommand() {
        super("del", 2, true, "w");
    }

    @Override
    public void execute(InnerClient client) {
        Sds[] commandArgs = client.getCommandArgs().toArray(new Sds[0]);
        Database db = client.getDatabase();

        int deleted = 0;

        for (int i = 1; i < commandArgs.length; i++) {
            // todo
            // 先删除过期的健

            if (db.delete(commandArgs[i])) {
                deleted++;
            }
        }

        client.replyInteger(deleted);
    }
}
