package command.commands;

import server.client.InnerClient;
import command.AbstractCommand;
import common.struct.PandisObject;
import common.struct.PandisString;
import common.struct.impl.Sds;
import common.constants.ErrorConstants;
import database.Database;

import java.util.List;

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
    public boolean checkCommandArgs(InnerClient client) {
        return true;
    }

    @Override
    public void doExecute(InnerClient client) {
        List<Sds> commandArgs = client.getCommandArgs();

        Sds key = commandArgs.get(1);

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
