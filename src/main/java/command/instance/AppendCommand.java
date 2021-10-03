package command.instance;

import client.InnerClient;
import command.AbstractCommand;
import common.struct.PandisObject;
import common.struct.PandisString;
import common.struct.impl.Sds;
import server.Database;

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
    public void execute(InnerClient client) {

        long newLength = 0;

        Database db = client.getDatabase();

        Sds[] commandArgs = client.getCommandArgs().toArray(new Sds[0]);
        Sds key = commandArgs[1];

        PandisObject value = db.lookupByKey(key);

        Sds appendContent = commandArgs[2];

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
