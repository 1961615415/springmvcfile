package dcx.pattern.singleton;

/**
 * 单一模式  只会初始化一个对象
 * 打印结果：user 构造方法被调用
 *         true
 */
public class Test {
    public static void main(String[] args) {
        User user1=Singleton.getInstance();
        User user2=Singleton.getInstance();
        System.out.println(user1==user2);
    }
}
