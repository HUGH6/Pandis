package command.instance;

import client.InnerClient;
import common.struct.PandisString;
import common.struct.impl.Sds;

/**
 * setnx命令
 * 命令格式：SETNX key value
 * @author: huzihan
 * @create: 2021-09-27
 */
public class SetnxCommand extends GenericSetCommand {

    public SetnxCommand() {
        super("setnx", 3, false, "wm");
    }

    @Override
    public void execute(InnerClient client) {
        Sds[] commandArgs = client.getCommandArgs().toArray(new Sds[0]);

        // 调用通用的set方法，将key/value保存
        PandisString key = commandArgs[1];
        PandisString value = commandArgs[2];

        genericSet(client, SET_NX, key, value, null, null);
    }


}
