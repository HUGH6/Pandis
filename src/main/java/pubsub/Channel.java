package pubsub;

import server.client.Client;

import java.util.List;

/**
 * @Description
 * @Author huzihan
 * @Date 2021/10/5
 **/
public interface Channel {
    String getName();
    void register(Client client);
    void unRegister(Client client);
    boolean isNoSubscriber();
    int getSubscriberNums();
    List<Client> getSubscribers();
}
