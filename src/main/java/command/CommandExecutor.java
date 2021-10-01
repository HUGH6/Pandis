package command;

import client.InnerClient;
import command.instance.GetCommand;
import server.PandisServer;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description 命令执行器
 * @Author huzihan
 * @Date 2021/7/21
 **/
public class CommandExecutor {

    // Pandis的所有命令映射表
    private static Map<String, AbstractCommand> commandTable = new HashMap<>();

    {
        // WANRING: 这里的实现有点问题，由于command内需要维持一个server的实例，这里获得的实例有可能是null
        commandTable.put("get", new GetCommand(PandisServer.getInstance()));
    }

    public CommandExecutor() {
    }

    /**
     * 从命令表中根据名字查找命令实现
     * @param commandName 命令名称
     * @return 命令实现
     */
    public static AbstractCommand lookupCommand(String commandName) {
        return commandTable.get(commandName);
    }

    /**
     * 核心方法执行命令
     * @param command 命令实现
     */
    public static void execute(Command command, InnerClient client) {
        command.execute(client);
    }
}
