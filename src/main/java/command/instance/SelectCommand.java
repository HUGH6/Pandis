package command.instance;

import client.InnerClient;
import command.AbstractCommand;
import common.struct.impl.Sds;
import constants.StatusConstants;
import server.Database;
import server.ServerContext;

/**
 * @description:
 * @author: huzihan
 * @create: 2021-10-02
 */
public class SelectCommand extends AbstractCommand {

    public SelectCommand() {
        super("select", 2, false, "rl");
    }

    @Override
    public void execute(InnerClient client) {
        Sds[] commandArgs = client.getCommandArgs().toArray(new Sds[0]);

        int dbId = 0;
        try {
            dbId = Integer.parseInt(commandArgs[1].toString());
        } catch (NumberFormatException e) {
            client.replyError("invalid DB index");
            return;
        }

        // todo
        // 判断是否是cluster模式，如果是cluster模式，则select命令禁用

        Database [] dbs = ServerContext.getContext().getDatabases();
        if (dbs.length <= dbId || dbId < 0) {
            client.replyError("invalid DB index");
            return;
        }

        client.selectDatabase(dbs[dbId]);
        client.replyStatus(StatusConstants.OK_STATUS);
    }


}
