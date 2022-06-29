public class JavaOper7 {
    public static void main(String[] args) {
        //测试条件运算符 三元
        int a=60;
        int b=50;
        System.out.println(b>a?"及格":"不及格");//不及格
        int x=-10;
        int y=10;
        System.out.println(x>y?x:(x>0?x:0));//0
    }
}
