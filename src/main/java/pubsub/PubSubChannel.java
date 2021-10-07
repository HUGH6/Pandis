package pubsub;

import server.client.Client;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @Description
 * @Author huzihan
 * @Date 2021/10/5
 **/
public class PubSubChannel implements Channel {
    private String name;
    private List<Client> subsrcibedClients;

    public PubSubChannel(String name) {
        this.name = name;
        this.subsrcibedClients = new LinkedList<>();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void register(Client client) {
        if (!this.subsrcibedClients.contains(client)) {
            this.subsrcibedClients.add(client);
        }
    }

    @Override
    public void unRegister(Client client) {
        this.subsrcibedClients.remove(client);
    }

    @Override
    public boolean isNoSubscriber() {
        return this.subsrcibedClients.isEmpty();
    }

    @Override
    public int getSubscriberNums() {
        return this.subsrcibedClients.size();
    }

    @Override
    public List<Client> getSubscribers() {
        return this.subsrcibedClients;
    }

    /********************************************
     * 一定要重写hashCode和equals方法，Channels会被注册到map中
     * 要保证同样的name的channel被认为是相同的
     *******************************************/

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof PubSubChannel) {
            PubSubChannel otherChannel = (PubSubChannel) o;

            if (otherChannel.getName().equals(this.getName())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }
}
