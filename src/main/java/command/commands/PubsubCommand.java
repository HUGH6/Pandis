package command.commands;

import command.AbstractCommand;
import common.struct.impl.Sds;
import pubsub.Channel;
import pubsub.PubSub;
import server.ServerContext;
import server.client.InnerClient;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @Description
 * @Author huzihan
 * @Date 2021/10/7
 **/
public class PubsubCommand extends AbstractCommand {
    public PubsubCommand() {
        super("pubsub", 2, true, "pltrR");
    }

    @Override
    public boolean checkCommandArgs(InnerClient client) {
        return true;
    }

    @Override
    public void doExecute(InnerClient client) {
        PubSub pubSub = ServerContext.getContext().getServerInstance().getPubSub();
        List<Sds> commandArgs = client.getCommandArgs();
        InnerClient innerClient = (InnerClient)client;
        List<String> multiReply = new LinkedList<>();

        // PUBSUB CHANNELS [pattern] 子命令
        String subCommand = commandArgs.get(1).toString();

        if ("channels".equals(subCommand) && (commandArgs.size() == 2 || commandArgs.size() == 3)) {
            /* PUBSUB CHANNELS [<pattern>] */
            // 检查命令请求是否给定了 pattern 参数
            // 如果没有给定的话，就设为 NULL
            String patternName = commandArgs.size() == 2 ? null : commandArgs.get(2).toString();

            // 创建 pubsub_channels 的字典迭代器
            // 链表中保存了所有订阅键所对应的频道的客户端

            Map<String, Channel> channels = pubSub.getPubSubChannels();
            for (Map.Entry<String, Channel> entry : channels.entrySet()) {
                String channel = entry.getKey();
                // 如果没有给定 pattern 参数，那么打印所有找到的频道
                // 如果给定了 pattern 参数，那么只打印和 pattern 相匹配的频道
                if (patternName == null) {
                    multiReply.add(channel);
                } else {
                    if (Pattern.matches(patternName, channel)) {
                        multiReply.add(channel);
                    }
                }
            }

            innerClient.replyMultiBulk(multiReply);
        } else if ("numsub".equals(subCommand) && commandArgs.size() >= 2) {
            // PUBSUB NUMSUB [channel-1 channel-2 ... channel-N] 子命令
            Map<String, Channel> channels = pubSub.getPubSubChannels();

            for (int i = 2; i < commandArgs.size(); i++) {
                // c->argv[j] 也即是客户端输入的第 N 个频道名字
                // pubsub_channels 的字典为频道名字
                // 而值则是保存了 c->argv[j] 频道所有订阅者的链表
                Channel channel = channels.get(commandArgs.get(i).toString());
                if (channel == null) {
                    continue;
                }

                multiReply.add(channel.getName());
                multiReply.add(String.valueOf(channel.getSubscriberNums()));
            }
            client.replyMultiBulk(multiReply);
        } else if ("numpat".equals(subCommand) && commandArgs.size() == 2) {
            // PUBSUB NUMPAT 子命令
            client.replyInteger(pubSub.getPubSubPatternNums());
        } else {
            // 错误处理
            client.replyError("Unknown PUBSUB subcommand or wrong number of arguments for '%s'" + subCommand);
        }
    }
}
