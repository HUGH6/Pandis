package command.commands;

import server.client.InnerClient;
import command.AbstractCommand;
import common.struct.impl.Sds;
import common.constants.StatusConstants;
import server.Database;
import server.ServerContext;
import common.utils.StringUtil;

import java.util.List;

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
    public boolean checkCommandArgs(InnerClient client) {
        List<Sds> commandArgs = client.getCommandArgs();

        if (!StringUtil.isInteger(commandArgs.get(1).toString())) {
            client.replyError("invalid DB index");
            return false;
        }

        return true;
    }

    @Override
    public void doExecute(InnerClient client) {
        List<Sds> commandArgs = client.getCommandArgs();
        // todo
        // 判断是否是cluster模式，如果是cluster模式，则select命令禁用

        int dbId = 0;
        try {
            dbId = Integer.parseInt(commandArgs.get(1).toString());
        } catch (NumberFormatException e) {
            client.replyError("invalid DB index");
            return;
        }

        Database [] dbs = ServerContext.getContext().getDatabases();
        if (dbs.length <= dbId || dbId < 0) {
            client.replyError("invalid DB index");
            return;
        }

        client.selectDatabase(dbs[dbId]);
        client.replyStatus(StatusConstants.OK_STATUS);

    }


}
