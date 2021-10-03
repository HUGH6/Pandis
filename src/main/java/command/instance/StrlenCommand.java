package command.instance;

import client.InnerClient;
import command.AbstractCommand;
import common.struct.PandisObject;
import common.struct.PandisString;
import common.struct.impl.Sds;
import constants.ErrorConstants;
import server.Database;

/**
 * @Description
 * @Author huzihan
 * @Date 2021/10/3
 **/
public class StrlenCommand extends AbstractCommand {
    public StrlenCommand() {
        super("strlen", 2, false, "r");
    }

    @Override
    public void execute(InnerClient client) {
        Sds[] commandArgs = client.getCommandArgs().toArray(new Sds[0]);
        Sds key = commandArgs[1];

        Database db = client.getDatabase();

        PandisObject value = db.lookupByKey(key);

        if (value == null) {
            client.replyInteger(0);
            return;
        }

        if (!(value instanceof PandisString)) {
            client.replyError(ErrorConstants.WRONG_TYPE_ERROR);
            return;
        }

        client.replyInteger(((PandisString) value).length());
    }
}
