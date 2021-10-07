package command.commands;

import server.client.InnerClient;
import command.AbstractCommand;
import common.struct.PandisObject;
import common.struct.PandisString;
import common.struct.impl.Sds;
import database.Database;

import java.util.List;

/**
 * @Description
 * @Author huzihan
 * @Date 2021/10/2
 **/
public class AppendCommand extends AbstractCommand {

    public AppendCommand() {
        super("append", 3, false, "wm");
    }

    @Override
    public boolean checkCommandArgs(InnerClient client) {
        return true;
    }

    @Override
    public void doExecute(InnerClient client) {
        Database db = client.getDatabase();
        long newLength = 0;

        List<Sds> commandArgs = client.getCommandArgs();

        Sds key = commandArgs.get(1);
        Sds appendContent = commandArgs.get(2);

        PandisObject value = db.lookupByKey(key);

        if (value == null) {
            // 键值不存在，则创建一个新的
            db.add(key, appendContent);
            newLength = appendContent.length();
        } else {
            if (!(value instanceof PandisString)) {
                return;
            }

            newLength = ((PandisString) value).length() + appendContent.length();
            if (newLength > 1024 * 1024 * 512) {
                client.replyError("string exceeds maximum allowed size (512MB)");
                return;
            }

            ((PandisString) value).append(appendContent);
            newLength = ((PandisString) value).length();
        }

        client.replyInteger(newLength);
    }
}
