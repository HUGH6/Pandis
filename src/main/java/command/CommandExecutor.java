package command;

import server.client.InnerClient;
import command.commands.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description 命令执行器
 * @Author huzihan
 * @Date 2021/7/21
 **/
public class CommandExecutor {
    private final static CommandExecutor executor = new CommandExecutor();

    // Pandis的所有命令映射表
    private Map<String, AbstractCommand> commandTable = new HashMap<>();

    private CommandExecutor() {
        // 将所有命令的实现写入表中
        commandTable.put("get", new GetCommand());
        commandTable.put("set", new SetCommand());
        commandTable.put("setnx", new SetnxCommand());
        commandTable.put("setex", new SetexCommand());
        commandTable.put("psetex", new PsetexCommand());
        commandTable.put("append", new AppendCommand());
        commandTable.put("strlen", new StrlenCommand());
        commandTable.put("del", new DelCommand());
        commandTable.put("exists", new ExistsCommand());
        commandTable.put("select", new SelectCommand());
        commandTable.put("ping", new PingCommand());
        commandTable.put("echo", new EchoCommand());
        commandTable.put("expire", new ExpireCommand());
        commandTable.put("expireat", new ExpireAtCommand());
        commandTable.put("pexpire", new PexpireCommand());
        commandTable.put("pexpireat", new PexpireAtCommand());
    }

    public static CommandExecutor getExecutor() {
        return executor;
    }

    /**
     * 从命令表中根据名字查找命令实现
     * @param commandName 命令名称
     * @return 命令实现
     */
    public Command lookupCommand(String commandName) {
        return commandTable.get(commandName);
    }

    /**
     * 核心方法执行命令
     * @param command 命令实现
     */
    public void execute(Command command, InnerClient client) {
        command.execute(client);
    }
}
