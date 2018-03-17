package Mine;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class MyBplusTree {
    /**root node**/
    protected MyNode root;

    /**head node**/
    protected MyNode head;

    /**M value**/
    protected int order;


    public int getOrder(){
        return this.order;
    }

    public void setOrder(Integer order){
        this.order = order;
    }

    public MyBplusTree(int order){
        if(order < 3){
            System.out.println("order must be greater than 2");
            System.exit(0);
        }
        this.order = order;
        root = new MyNode(true, true);
        head = root;
    }

    public void insertOrUpdate(Integer key, Object obj){
        root.insertOrUpdate(key, obj, this);
    } // Remove the specified keyword

    public void remove(Integer key){
        root.remove(key, this);
    } // Add the specified entry

    public Object search(Integer key) { // Find the specified entry
        return root.search(key);
    }

    public void find(MyBplusTree Tree) {
        root.find(Tree);
    }// Iterate over node list

    public static void main(String[] args) {
        MyBplusTree tree = new MyBplusTree(4);
        Random random = new Random();
        long current = System.currentTimeMillis();
        for (int j = 0; j < 10000; j++) {
            for (int i = 0; i < 100; i++) {
                int randomNumber = random.nextInt(1000);
                tree.insertOrUpdate(randomNumber, randomNumber);
            }

            for (int i = 0; i < 100; i++) {
                int randomNumber = random.nextInt(1000);
                tree.remove(randomNumber);
            }
        }
        long duration = System.currentTimeMillis() - current;
        System.out.println("time elpsed for duration: " + duration);
        int testKey = 80;
        System.out.print("Find the specified keyword : ");
        Object testFind = tree.search(testKey);
        if(testFind != null){
            System.out.println("Success, key : " + testKey + ", value : " + testFind);
        }
        else {
            System.out.println("Failed, " + testKey + " is not exist");
        }
        tree.find(tree);
    }
}
