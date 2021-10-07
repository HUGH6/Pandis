package pubsub;

import server.client.Client;

import java.util.List;

/**
 * @Description
 * @Author huzihan
 * @Date 2021/10/6
 **/
public interface Pattern {
    String getName();
    void register(Client client);
    void unRegister(Client client);
    boolean isNoSubscriber();
    List<Client> getSubscribers();
    boolean match(String key);
}
