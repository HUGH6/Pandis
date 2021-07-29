package cli;

import protocol.Reply;
import protocol.ReplyType;
import utils.StringUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static protocol.ReplyType.*;

/**
 * @Description 客户端cli
 * @Author huzihan
 * @Date 2021/7/24
 **/
public class ClientCli {
    private ClientConfig config;
    private ClientContext context;


    public ClientCli() {
        config = new ClientConfig();
    }


    private void initHelp() {

    }

    private int parseOption(String [] args) {
        int index = 0;
        for (; index < args.length; index++) {
            boolean lastarg = (index == args.length - 1);

            if ("-h".equals(args[index]) && !lastarg) {
                config.setHostip(args[++index]);
            } else if ("-h".equals(args[index]) && lastarg) {
                usage();
            } else if ("--help".equals(args[index])) {
                usage();
            } else if ("-x".equals(args[index])) {
                config.setStdinarg(true);
            } else if ("-p".equals(args[index]) && !lastarg) {
                config.setHostport(Integer.valueOf(args[++index]));
            } else if ("-s".equals(args[index]) && !lastarg) {
                config.setHostsocket(args[++index]);
            } else if ("-r".equals(args[index]) && !lastarg) {
                config.setRepeat(Integer.valueOf(args[++index]));
            } else if ("-i".equals(args[index]) && !lastarg) {
                double seconds = Double.valueOf(args[++index]);
                config.setInterval((long) (seconds * 1000000));
            } else if ("-n".equals(args[index]) && !lastarg) {
                config.setDbnum(Integer.valueOf(args[++index]));
            } else if ("-a".equals(args[index]) && !lastarg) {
                config.setAuth(args[++index]);
            } else if ("--raw".equals(args[index])) {
                config.setOutputType(ClientConfig.OutputType.OUTPUT_RAW);
            } else if ("--csv".equals(args[index])) {
                config.setOutputType(ClientConfig.OutputType.OUTPUT_CSV);
            } else if ("--latency".equals(args[index])) {
                config.setLatencyMode(true);
            } else if ("--latency-history".equals(args[index])) {
                config.setLatencyMode(true);
                config.setLatencyHistory(true);
            } else if ("--slave".equals(args[index])) {
                config.setSlaveMode(true);
            } else if ("--stat".equals(args[index])) {
                config.setStatMode(true);
            } else if ("--scan".equals(args[index])) {
                config.setScanMode(true);
            } else if ("--pattern".equals(args[index]) && !lastarg) {
                config.setPattern(args[++index]);
            } else if ("--intrinsic-latency".equals(args[index]) && !lastarg) {
                config.setIntrinsicLatencyMode(true);
                config.setIntrinsicLatencyDuration(Integer.valueOf(args[++index]));
            } else if ("--rdb".equals(args[index]) && !lastarg) {
                config.setGetrdbMode(true);
                config.setRdbFilename(args[++index]);
            } else if ("--pipe".equals(args[index])) {
                config.setPipeMode(true);
            } else if ("--pip-timeout".equals(args[index]) && !lastarg) {
                config.setPipeTimeout(Integer.valueOf(args[++index]));
            } else if ("--bigkeys".equals(args[index])) {
                config.setBigkeys(true);
            } else if ("--eval".equals(args[index]) && !lastarg) {
                config.setEval(args[++index]);
            } else if ("-c".equals(args[index])) {
                config.setClusterMode(true);
            } else if ("-d".equals(args[index]) && !lastarg) {
                config.setMbDelim(args[++index]);
            } else if ("-v".equals(args[index]) || "--version".equals(args[index])) {
                System.out.print("pandis-cli " + cliVersion() + "\n");
                System.exit(0);
            } else {
                if (args[index].charAt(0) == '-') {
                    System.err.println("Unrecognized option or bad number of args for: " + args[index]);
                    System.exit(1);
                } else {
                    /* Likely the command name, stop here. */
                    break;
                }
            }
        }

        return index;
    }

    /**
     * 打印使用说明
     */
    private void usage() {
        StringBuilder usage = new StringBuilder();

        usage.append("pandis-cli\n");
        usage.append("\n");
        usage.append("Usage: pandis-cli [OPTIONS] [cmd [arg [arg ...]]]\n");
        usage.append("  -h <hostname>      Server hostname (default: 127.0.0.1).\n");
        usage.append("  -p <port>          Server port (default: 6379).\n");
        usage.append("  -s <socket>        Server socket (overrides hostname and port).\n");
        usage.append("  -a <password>      Password to use when connecting to the server.\n");
        usage.append("  -r <repeat>        Execute specified command N times.\n");
        usage.append("  -i <interval>      When -r is used, waits <interval> seconds per command.\n");
        usage.append("                     It is possible to specify sub-second times like -i 0.1.\n");
        usage.append("  -n <db>            Database number.\n");
        usage.append("  -x                 Read last argument from STDIN.\n");
        usage.append("  -d <delimiter>     Multi-bulk delimiter in for raw formatting (default: \\n).\n");
        usage.append("  -c                 Enable cluster mode (follow -ASK and -MOVED redirections).\n");
        usage.append("  --raw              Use raw formatting for replies (default when STDOUT is\n");
        usage.append("                     not a tty).\n");
        usage.append("  --csv              Output in CSV format.\n");
        usage.append("  --latency          Enter a special mode continuously sampling latency.\n");
        usage.append("  --latency-history  Like --latency but tracking latency changes over time.\n");
        usage.append("                     Default time interval is 15 sec. Change it using -i.\n");
        usage.append("  --slave            Simulate a slave showing commands received from the master.\n");
        usage.append("  --rdb <filename>   Transfer an RDB dump from remote server to local file.\n");
        usage.append("  --pipe             Transfer raw Redis protocol from stdin to server.\n");
        usage.append("  --pipe-timeout <n> In --pipe mode, abort with error if after sending all data.\n");
        usage.append("                     no reply is received within <n> seconds.\n");
        usage.append("                     Default timeout: %d. Use 0 to wait forever.\n");
        usage.append("  --bigkeys          Sample Redis keys looking for big keys.\n");
        usage.append("  --scan             List all keys using the SCAN command.\n");
        usage.append("  --pattern <pat>    Useful with --scan to specify a SCAN pattern.\n");
        usage.append("  --intrinsic-latency <sec> Run a test to measure intrinsic system latency.\n");
        usage.append("                     The test will run for the specified amount of seconds.\n");
        usage.append("  --eval <file>      Send an EVAL command using the Lua script at <file>.\n");
        usage.append("  --help             Output this help and exit.\n");
        usage.append("  --version          Output version and exit.\n");
        usage.append("\n");
        usage.append("Examples:\n");
        usage.append("  cat /etc/passwd | redis-cli -x set mypasswd\n");
        usage.append("  redis-cli get mypasswd\n");
        usage.append("  redis-cli -r 100 lpush mylist x\n");
        usage.append("  redis-cli -r 100 -i 1 info | grep used_memory_human:\n");
        usage.append("  redis-cli --eval myscript.lua key1 key2 , arg1 arg2 arg3\n");
        usage.append("  redis-cli --scan --pattern '*:12345*'\n");
        usage.append("\n");
        usage.append("  (Note: when using --eval the comma separates KEYS[] from ARGV[] items)\n");
        usage.append("\n");
        usage.append("When no command is given, redis-cli starts in interactive mode.\n");
        usage.append("Type \"help\" in interactive mode for information on available commands.\n");
        usage.append("\n");

        System.err.print(usage.toString());
        System.exit(1);
    }

    private String cliVersion() {
        return "0.0.1";
    }

    private ClientContext connect(String hostip, int hostport) {
        ClientContext context = new ClientContext();
        context.connectTcp(hostip, hostport);
        return context;
    }

    private ClientContext connectUnix(String hostsocket) {
        //
        return null;
    }

    private boolean cliConnect(boolean force) {
        if (this.context == null || force) {
            if (this.config.getHostsocket() == null) {
                this.context = connect(this.config.getHostip(), this.config.getHostport());
            } else {
                this.context = connectUnix(this.config.getHostsocket());
            }

//            if (context->err) {
//                fprintf(stderr,"Could not connect to Redis at ");
//                if (config.hostsocket == NULL)
//                    fprintf(stderr,"%s:%d: %s\n",config.hostip,config.hostport,context->errstr);
//                else
//                    fprintf(stderr,"%s: %s\n",config.hostsocket,context->errstr);
//                redisFree(context);
//                context = NULL;
//                return REDIS_ERR;
//            }


            // 判断是否认证了以及是否选择的正确的db
            if (!cliAuth()) {
                return false;
            }
            if (!cliSelect()) {
                return false;
            }

        }

        return true;
    }

    private boolean cliAuth() {
         return true;
    }

    private boolean cliSelect() {
        return true;
    }

    /**
     * 打印congtext的错误信息
     */
    private void printContextError() {
        if (this.context == null) {
            return;
        }

        System.err.println("Error: " + this.context.getErrInfo());
    }

    private void outputHelp() {
        // todo: 输出帮助信息
        System.out.println("Warning: 该命令未实现");
    }

    private Reply getReply() {
        return this.context.getReply();
    }

    private boolean readReply(boolean outputRawString) {
        boolean output = true;
        StringBuilder out = new StringBuilder();

        Reply reply = getReply();

        // 如果获取回复失败
        if (reply == null) {
            if (this.config.isShutdown()) {
                return true;
            }

            printContextError();
            System.exit(1);
            return false;
        }

        // todo: Check if we need to connect to a different node and reissue the request.

        // 是否输出
        if (output) {
            // 以raw字符串格式输出
            if (outputRawString) {
                out.append(formatReplyRaw(reply));
            } else {
                // 判断以什么格式输出回复结果
                if (this.config.getOutputType() == ClientConfig.OutputType.OUTPUT_RAW) {
                    out.append(formatReplyRaw(reply));
                    out.append("\n");
                } else if (this.config.getOutputType() == ClientConfig.OutputType.OUTPUT_STANDARD) {
                    out.append(formatReplyTTY(reply));
                } else if (this.config.getOutputType() == ClientConfig.OutputType.OUTPUT_CSV) {
                    out.append(formatReplyCSV(reply));
                    out.append("\n");
                }
            }

            System.out.println(out.toString());
        }
        return true;
    }

    private String formatReplyRaw(Reply reply) {
        StringBuilder out = new StringBuilder();

        switch (reply.getType()) {
            case ERROR:
                out.append(reply.getStringReplyContent());
                out.append("\n");
                break;
            case STATUS:
            case BULK:
                out.append(reply.getStringReplyContent());
                break;
            case INTEGER:
                out.append(String.valueOf(reply.getIntegerReplyContent()));
                break;
            case MULTI_BULK:
                List<String> contents = reply.getMultiStringReplyContent();
                int len = contents.size();
                for (int i = 0; i < len; i++) {
                    if (i > 0) {
                        out.append(this.config.getMbDelim());
                    }
                    out.append(contents.get(i));
                }
                break;
            default:
                System.err.println("Unknown reply type");
                System.exit(1);
        }
        return out.toString();
    }

    private String formatReplyTTY(Reply reply) {
        StringBuilder out = new StringBuilder();

        switch (reply.getType()) {
            case ERROR:
                out.append("(error) ");
                out.append(reply.getStringReplyContent());
                out.append("\n");
                break;
            case STATUS:
                out.append(reply.getStringReplyContent());
                out.append("\n");
                break;
            case BULK:
                out.append(reply.getStringReplyContent());
                out.append("\n");
                break;
            case NIL:
                out.append("(nil)\n");
                break;
            case INTEGER:
                out.append("(integer) ");
                out.append(String.valueOf(reply.getIntegerReplyContent()));
                out.append("\n");
                break;
            case MULTI_BULK:
                List<String> contents = reply.getMultiStringReplyContent();
                int len = contents.size();

                if (len == 0) {
                    out.append("(empty list or set)\n");
                } else {
                    for (int i = 0; i < len; i++) {
                        out.append(i);
                        out.append(") ");
                        out.append(contents.get(i));
                        out.append("\n");
                    }
                }
                break;
            default:
                System.err.println("Unknown reply type");
                System.exit(1);
        }
        return null;
    }

    private String formatReplyCSV(Reply reply) {
        StringBuilder out = new StringBuilder();

        switch (reply.getType()) {
            case ERROR:
                out.append("ERROR, ");
                out.append("\"");
                out.append(reply.getStringReplyContent());
                out.append("\"");
                break;
            case STATUS:
                out.append("\"");
                out.append(reply.getStringReplyContent());
                out.append("\"");
                break;
            case BULK:
                out.append("\"");
                out.append(reply.getStringReplyContent());
                out.append("\"");
                break;
            case INTEGER:
                out.append(String.valueOf(reply.getIntegerReplyContent()));
                break;
            case MULTI_BULK:
                List<String> contents = reply.getMultiStringReplyContent();
                int len = contents.size();
                for (int i = 0; i < len; i++) {
                    out.append("\"");
                    out.append(contents.get(i));
                    out.append("\"");
                    if (i != len - 1) {
                        out.append(", ");
                    }
                }
                break;
            default:
                System.err.println("Unknown reply type");
                System.exit(1);
        }
        return out.toString();
    }

    /**
     * 发送命令
     * @param startIndex 从传入的参数数组的第几位开始解析
     * @param argv 参数数组
     * @param repeat 命令重复次数
     * @return 命令发送是否成功
     */
    private boolean sendCommand(int startIndex, String [] argv, int repeat) {
        // 参数检测
        if (startIndex >= argv.length) {
            return false;
        }

        // 实际可以解析的数组长度
        int argc = argv.length - startIndex;
        // 第一个元素，即命令
        String command = argv[startIndex].toLowerCase();

        // 帮助命令
        if ("help".equals(command) || "?".equals(command)) {
            outputHelp();
            return true;
        }

        if (this.context == null) {
            return false;
        }

        boolean outputRaw = false;
        if ("info".equals(command)) {
            // todo: 这里还有一些其他判断条件，但暂时未知功能，这里暂时不实现
            outputRaw = true;
        }

        if ("shutdown".equals(command)) {
            config.setShutdown(true);
        }

        if ("monitor".equals(command)) {
            config.setMonitorMode(true);
        }

        if ("subscribe".equals(command) || "psubscribe".equals(command)) {
            config.setPubsubMode(true);
        }

        if ("sync".equals(command) || "psync".equals(command)) {
            config.setSlaveMode(true);
        }

        /* Setup argument length */
        while (repeat > 0) {
            // 解析输入的命令参数，按照协议将其生成命令
            // 将命令缓存到客户端context的输出缓冲区outBuffer
            context.appendCommandArgv(startIndex, argv);

            while (config.isMonitorMode()) {
                // todo
            }

            if (config.isPubsubMode()) {
                // todo
            }

            if (config.isSlaveMode()) {
                // todo
            }

            if (!readReply(outputRaw)) {
                return false;
            } else {
                if ("select".equals(command) && argc == 2) {
                    config.setDbnum(Integer.valueOf(argv[startIndex + 1]));
                }
            }

            if (config.getInterval() > 0) {
                try {
                    Thread.sleep(config.getInterval());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.flush();

            repeat--;
        }

        return true;
    }

    /**
     * 以命令行交互模式运行
     * 用户可以输入repl格式的文本内容发送给服务器
     */
    private void runReplMode() {
        Scanner scanner = new Scanner(System.in);

        String line = null;
        String [] argv = null;
        while ((line = scanner.nextLine())!= null) {
            line = line.trim();
            if (line.length() > 0) {
                // 分割参数，注意:可以允许“a b c”这种用引号括起来的表示一个整体的形式，这种应该作为一个整体
                argv = StringUtil.splitArgs(line);

                System.out.println("测试 输入命令为：" + Arrays.toString(argv));

                if (argv == null) {
                    System.out.print("Invalid argument(s)\n");
                    continue;
                } else if (argv.length > 0) {
                    String command = argv[0].toLowerCase();
                    if ("quit".equals(command) || "exit".equals(command)) {
                        System.exit(0);
                    } else if (argv.length == 3 && "connect".equals(command)) {
                        config.setHostip(argv[1]);
                        config.setHostport(Integer.valueOf(argv[2]));
                        cliConnect(true);
                    } else if (argv.length == 1 && "clear".equals(command)) {
                        // todo: redis中这是实现了清屏功能，但java中貌似不方便实现，暂时不实现
                        System.out.println("Warning: 暂时未实现该命令");
                    } else {
                        long startTime = System.currentTimeMillis(), elapsed = 0;

                        // 因为命令第一个参数有可能是表示执行次数的数字，所以这里需要判断
                        int repeat = 1;     // 命令重复次数，默认为1
                        int skipArgs = 0;   // 若输入第一个参数是repeat次数，那么需要跳过1个参数解析后面的参数
                        if (StringUtil.isInteger(argv[0])) {
                            repeat = Integer.parseInt(argv[0]);
                            if (argv.length > 1 && repeat > 0) {
                                skipArgs = 1;
                            }
                        }

                        while (true) {
                            // 发送命令，若失败，则重试
                            if (!sendCommand(skipArgs, argv, repeat)) {
                                cliConnect(true);
                                // 若重试失败，输出错误信息
                                if (!sendCommand(skipArgs, argv, repeat)) {
                                    printContextError();
                                }
                            }

                            // Issue the command again if we got redirected in cluster mode
                            // todo: 暂时未知这段代码功能
                            if (config.isClusterMode() && config.getClusterReissueCommand()) {
                                cliConnect(true);
                            } else {
                                break;
                            }
                        }

                        // 计算执行时间
                        elapsed = System.currentTimeMillis() - startTime;
                        if (elapsed >= 500) {
                            System.out.println((double)elapsed / 1000);
                        }
                    }
                }
            }
        }

        System.exit(0);

    }

    public static void main(String [] args) {
        ClientCli cli = new ClientCli();

        cli.parseOption(args);

        /* Latency mode */
        if (cli.config.getLatencyMode()) {
            if (!cli.cliConnect(false)) {
                System.exit(1);
            }
            // todo: latencyMode();
        }

        /* Slave mode */
        if (cli.config.isSlaveMode()) {
            if (!cli.cliConnect(false)) {
                System.exit(1);
            }
            // todo: slaveMode();
        }

        /* Get RDB mode. */
        if (cli.config.isGetrdbMode()) {
            if (!cli.cliConnect(false)) {
                System.exit(1);
            }
            // todo: getRDB();
        }

        /* Pipe mode */
        if (cli.config.isPipeMode()) {
            if (!cli.cliConnect(false)) {
                System.exit(1);
            }
            // todo: pipeMode();
        }

        /* Find big keys */
        if (cli.config.getBigkeys()) {
            if (!cli.cliConnect(false)) {
                System.exit(1);
            }
            // todo: findBigKeys();
        }

        /* Stat mode */
        if (cli.config.isStatMode()) {
            if (!cli.cliConnect(false)) {
                System.exit(1);
            }

            if (cli.config.getInterval() == 0) {
                cli.config.setInterval(1000000);
            }
            // todo: statMode();
        }

        /* Scan mode */
        if (cli.config.isScanMode()) {
            if (!cli.cliConnect(false)) {
                System.exit(1);
            }
            // todo: scanMode();
        }

        /* Intrinsic latency mode */
        if (cli.config.isIntrinsicLatencyMode()) {
            // todo: intrinsicLatencyMode();
        }

        /* Start interactive mode when no command is provided */
        if (args.length == 0 && cli.config.getEval() == null) {
            /* Note that in repl mode we don't abort on connection error.
             * A new attempt will be performed for every command send. */
            cli.cliConnect(false);
            cli.runReplMode();
        }

        /* Otherwise, we have some arguments to execute */
        if (!cli.cliConnect(false)) {
            System.exit(1);
        }

        if (cli.config.getEval() != null) {
            // todo: return evalMode(argc,argv);
        } else {
            // todo: return noninteractive(argc,convertToSds(argc,argv));
        }
    }
}
