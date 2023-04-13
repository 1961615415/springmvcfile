package dcx.pattern.singleton;

import lombok.Data;

@Data
public class User {
    private String name;
    private Integer age;
    User(){
        System.out.println("user 构造方法被调用");
    }
}
