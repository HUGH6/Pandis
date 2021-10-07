package pubsub;

import server.client.Client;

import java.util.LinkedList;
import java.util.List;

/**
 * @Description
 * @Author huzihan
 * @Date 2021/10/6
 **/
public class PubSubPattern implements Pattern {
    private String patternName;
    private List<Client> subsrcibedClients;

    public PubSubPattern(String patternName) {
        this.patternName = patternName;
        this.subsrcibedClients = new LinkedList<>();
    }

    @Override
    public String getName() {
        return this.patternName;
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
    public List<Client> getSubscribers() {
        return this.subsrcibedClients;
    }

    @Override
    public boolean match(String key) {
        return java.util.regex.Pattern.matches(this.patternName, key);
    }
}
