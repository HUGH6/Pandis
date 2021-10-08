package event;

//import jdk.nashorn.internal.objects.annotations.Function;

/**
 * @description:
 * @author: huzihan
 * @create: 2021-06-27
 */
@FunctionalInterface
public interface Procedure<T> {
    void call(T t);
}
