package dcx.pattern.singleton;

/***
 * 单一模式 双重锁+
 */
public class Singleton {
    private static volatile User instance;

    public static User getInstance() {
        if(null==instance){
            synchronized (User.class){
                if (null==instance)
                    instance=new User();
            }
            return instance;
        }
        return instance;
    }
}
