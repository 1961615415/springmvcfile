public class JavaQiang {
    public static void main(String[] args) {
        int a=10;
        System.out.println((long)a);
        //数据溢出问题
        int b=1000000000;//10亿
        int year=1000;
        System.out.println(b*year);//超过了int的20亿的范围直接变负数
        System.out.println((long)b*year);//提升一个类型为long 1000000000000
        System.out.println(1L*b*year);//这种也可以

    }
}
