package event;

/**
 * @Description
 * @Author huzihan
 * @Date 2021/10/4
 **/
public interface TimeEvent {
    void execute();
    long getWhen();
}
