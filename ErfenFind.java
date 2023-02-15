package suanFa;

/**
 * 二分查找
 * 练习
 * 1.1 假设有一个包含128个名字的有序列表，你要使用二分查找在其中查找一个名字，请
 * 问最多需要几步才能找到？
 * 1.2 上面列表的长度翻倍后，最多需要几步？
 */
public class ErfenFind {
    public static void main(String[] args) {
        String[] names=new String[]{"Aa","BC","Bd","CC","CD","DD","Er","Fr","Gr","HH","MM","NN","zz"};
        System.out.println("查找到的位置为"+find(names,"Bd"));
    }

    /***
     * 假设有一个包含128个名字的有序列表，你要使用二分查找在其中查找一个名字，请问最多需要几步才能找到？
     * @param names
     * @return
     */
    public static int find(String[] names,String findName){
        int step=1;
     int low=0,high=names.length-1;//查找范围
     while (high>=low){
         int mid=(high+low)/2;
         System.out.println("第"+step+"步,查到的词为"+names[mid]);
         if(names[mid].equals(findName)){
             System.out.println("共查找了"+step+"步");
             return mid;
         }
         //猜到的数比要找的大，那就缩小hight的范围
         if(names[mid].compareToIgnoreCase(findName)>0){
            high=mid-1;
         }
         if(names[mid].compareToIgnoreCase(findName)<0){
             low=mid+1;
         }
         step++;
     }
     return -1;//查找的名字的位置，没有查到直接返回-1
    }
}
