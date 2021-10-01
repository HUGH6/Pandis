package command;

import client.InnerClient;

/**
 * @Description Pandis命令抽象接口
 * @Author huzihan
 * @Date 2021-07-21
 */
@FunctionalInterface
public interface Command {
    void execute(InnerClient client);
}
