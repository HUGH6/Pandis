package event;

/**
 * @Description
 * @Author huzihan
 * @Date 2021/10/4
 **/
public interface CycleTimeEvent extends TimeEvent {
    CycleTimeEvent nextCycleTimeEvent();
    void resetFireTime();
}
