package command.instance;

import client.InnerClient;

/**
 * set命令
 * 命令格式：SET key value [NX] [XX] [EX <seconds>] [PX <milliseconds>]
 * @author: huzihan
 * @create: 2021-09-27
 */
public class SetCommand extends GenericSetCommand {

    public SetCommand() {
        super("set", 3, true, "wm");
    }

    @Override
    public void execute(InnerClient client) {
        // 解析参数，记录标记

        // 尝试对对象进行编码
    }


}
