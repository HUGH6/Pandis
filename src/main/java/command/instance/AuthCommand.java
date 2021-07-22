package command.instance;

import command.AbstractCommand;

/**
 * @Description auth命令实现
 * @Author huzihan
 * @Date 2021/7/21
 **/
public class AuthCommand extends AbstractCommand {
    public AuthCommand() {
        super("auth", 2, false, "rslt");
    }
    @Override
    public void execute() {
        System.out.println("auth 测试");
    }
}