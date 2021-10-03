package command.instance;

import client.InnerClient;
import command.AbstractCommand;
import common.struct.impl.Sds;
import server.Database;

import java.util.ArrayList;

/**
 * @Description
 * @Author huzihan
 * @Date 2021/10/3
 **/
public class ExistsCommand extends AbstractCommand {
    public ExistsCommand() {
        super("exists", 2, false, "r");
    }

    @Override
    public void execute(InnerClient client) {
        // todo
        // 检查键是否已经过期，如果已过期的话，那么将它删除
        // 这可以避免已过期的键被误认为存在

        ArrayList<Sds> commandArgs = client.getCommandArgs();

        Database db = client.getDatabase();

        Sds key = commandArgs.get(1);

        if (db.exists(key)) {
            client.replyInteger(1);
        } else {
            client.replyInteger(0);
        }

    }
}
