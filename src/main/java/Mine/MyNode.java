package Mine;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class MyNode {
    /** It is a leaf or not **/
    protected boolean isLeaf;

    /** It is root or not **/
    protected boolean isRoot;

    /** parent node **/
    protected MyNode parent;

    /** chlid node **/
    protected List<MyNode> child;

    /** key list **/
    protected List<Entry<Integer,Object>> entries;

    /** leaf's previous node **/
    protected MyNode previous;

    /** leaf's next node **/
    protected MyNode next;

    public MyNode(boolean isLeaf){
        this.isLeaf = isLeaf;
        entries = new ArrayList<Entry<Integer, Object>>();
        if(!isLeaf){
            child = new ArrayList<MyNode>();
        }
    }

    public MyNode(boolean isLeaf,boolean isRoot){
        this(isLeaf);
        this.isRoot = isRoot;
    }

    public void setEntries(List<Entry<Integer,Object>> entries){
        this.entries = entries;
    }
    public List<Entry<Integer,Object>> getEntries(){
        return entries;
    }

    public Object search(Integer key){
        if(isLeaf){
            //It is leaf
            for(Entry<Integer,Object> entry : entries){
                if(entry.getKey().compareTo(key) == 0){
                    //find the key
//                    System.out.println("success");
                    return entry.getValue();
                }
            }
            return null;
        }
        else{
            //It isn't leaf
            if(key.compareTo(entries.get(0).getKey()) <= 0 ){
                //key less than minimum,keep find across the minimum
                return child.get(0).search(key);
            }
            else if(key.compareTo(entries.get(entries.size()-1).getKey()) >= 0){
                //key greater than maximum,keep find across the maximum
                return  child.get(child.size()-1).search(key);
            }
            else{
                for(int i = 0; i < entries.size(); i++){
                    if(key.compareTo(entries.get(i).getKey()) >= 0 && key.compareTo(entries.get(i+1).getKey()) < 0){
                        return child.get(i).search(key);
                    }
                }
            }
        }
        return null;
    }

    public boolean insertOrUpdate(Integer key,Object obj,MyBplusTree Tree){
        Entry<Integer,Object> insertObj = new SimpleEntry<Integer, Object>(key, obj);
        //It is leaf
        if(isLeaf){
            //Not need split
            if(entries.size() == 0){// node list is empty
                entries.add(insertObj);
                   return true;
            }
            if(entries.size() < Tree.getOrder()){// not full
                insertKey(insertObj);
                if(this.parent != null){ // update parent
                    parent.updateNode(Tree);
                }
            }
            else{
                insertKey(insertObj);
                if(entries.size() == Tree.getOrder()){
                    //key existing
                    return true;
                }
                //split to two node
                MyNode left = new MyNode(true);
                MyNode right = new MyNode(true);
                if(previous != null){
                    previous.next = left;
                    left.previous = previous;
                }
                if(next != null){
                    next.previous = right;
                    right.next = next;
                }
                if(previous == null){
                    left.previous = null;
                    Tree.head = left;
                }
                left.next = right;
                right.previous = left;
                previous = null;
                next = null;

                //insert to node and need to split
                int leftSize = (Tree.getOrder() + 1) / 2 + (Tree.getOrder() + 1) % 2;
                int rightSize = (Tree.getOrder()+ 1) / 2;
                for(int i = 0; i < leftSize; i++){
                    left.entries.add(entries.get(i));
                }
                for(int i = 0; i < rightSize; i++){
                    right.entries.add(entries.get(leftSize + i));
                }

                //It is not root node, update parent node
                if(parent != null){
                    int index = parent.child.indexOf(this);
                    parent.child.remove(this);
                    left.parent = parent;
                    right.parent = parent;
                    parent.child.add(index,left);
                    parent.child.add(index+1,right);
                    setEntries(null);
                    child = null;
                    parent.updateInsert(Tree);
                    parent = null;
                }
                else{ // It is root node
                    isRoot = false;
                    MyNode root = new MyNode(false,true);
                    Tree.root = root;
                    left.parent = root;
                    right.parent = root;
                    root.child.add(left);
                    root.child.add(right);
                    setEntries(null);
                    child = null;

                    root.updateInsert(Tree);
                }
            }
//            for(Entry<Integer,Object> entry : entries){
//                if(entry.getKey().compareTo(key) == 0){
//                }
//            }
//            if(key.compareTo(entries.get(0).getKey()) <0 ){
//                //key less than minimum
//
//            }
//            else if(key.compareTo(entries.get(entries.size()-1).getKey()) >= 0){
//                //key greater than maximum,keep find across the maximum
//            }
//            for(Entry<Integer,Object> entry : entries){
//            }
        }
        else{//It is not leaf node
            //key less than minimum,keep find across the minimum
            if (key.compareTo(entries.get(0).getKey()) <= 0) {
                child.get(0).insertOrUpdate(key, obj, Tree);
            }
            //key larger than maximum,keep find across the maximum
            else if (key.compareTo(entries.get(entries.size()-1).getKey()) >= 0) {
                child.get(child.size()-1).insertOrUpdate(key, obj, Tree);
            }else {
                for (int i = 0; i < entries.size(); i++) {
                    if (entries.get(i).getKey().compareTo(key) <= 0 && entries.get(i+1).getKey().compareTo(key) > 0) {
                        child.get(i).insertOrUpdate(key, obj, Tree);
                        break;
                    }
                }
            }
        }

        return false;
    }

    public void remove(Integer key, MyBplusTree Tree){
        if(isLeaf){//It is leaf
            // not contain
            int judge = 0;
            for(Entry<Integer, Object> entry : entries){
                if(entry.getKey().compareTo(key)== 0){
                    judge = 1;
                    break;
                }
            }
            if(judge == 0){
                return;
            }

            //leaf && root
            if(isLeaf && isRoot){
                remove(key);
            }
            else{
                if(entries.size() > Tree.getOrder()/2 && entries.size() > 2){// key list num> M/2
                     remove(key);
                }
                else{
                    //previous node key list num > M/2
                    if(previous != null && previous.entries.size() > Tree.getOrder()/2
                            && previous.entries.size() > 2
                            && previous.parent == parent){
                        Entry<Integer, Object> entry = previous.getEntries().get(previous.getEntries().size()-1);
                        previous.getEntries().remove(previous.getEntries().size()-1);
                        entries.add(0, entry);
                        remove(key);
                    }
                    //next node key list num > M/2
                    else if(next != null && next.entries.size() > Tree.getOrder()/2
                            && next.entries.size() > 2
                            && next.parent == parent){
                        Entry<Integer, Object> entry = next.getEntries().get(0);
                        next.getEntries().remove(0);
                        entries.add(entry);
                        remove(key);
                    }
                    else{
                        //merge previous node
                        if (previous != null
                                && (previous.getEntries().size() <= Tree.getOrder() / 2 || previous.getEntries().size() <= 2)
                                && previous.parent == parent) {
                            for (int i = previous.getEntries().size()-1; i >= 0; i--){
                                entries.add(0,previous.getEntries().get(i));
//                                previous.remove(previous.getEntries().size()-1-i);
                            }
                            remove(key);
                            previous.setEntries(null);
                            previous.parent = null;
                            parent.child.remove(previous);

                            //update list
                            if(previous.previous != null){
                                previous.previous.next = this;
                                MyNode temp = previous;
                                this.previous = previous.previous;
                                temp.previous = null;
                                temp.next = null;
                            }
                            else{
                                Tree.head = this;
                                previous.next = null;
                                previous = null;
                            }
                        }
                        //merge next node
                        else if(next != null
                                && (next.getEntries().size() <= Tree.getOrder() / 2 || next.getEntries().size() <= 2)
                                && next.parent == parent){
                            for (int i = 0; i < next.getEntries().size(); i++){
                                entries.add(next.getEntries().get(i));
//                                next.remove(next.getEntries().size()-1-i);
                            }
                            remove(key);
                            next.setEntries(null);
                            next.parent = null;
                            parent.child.remove(next);

                            //update list
                            if(next.next != null){
                                next.next.previous = this;
                                MyNode temp = next;
                                this.next = next.next;
                                temp.previous = null;
                                temp.next = null;
                            }
                            else{
                                next.previous = null;
                                next = null;
                            }
                        }
                    }
                }
                parent.updateRemove(Tree);
            }
        }
        else{
            //It is not leaf node
            if (key.compareTo(entries.get(0).getKey()) <= 0) {
                child.get(0).remove(key, Tree);
            }else if (key.compareTo(entries.get(entries.size()-1).getKey()) >= 0) {
                child.get(child.size()-1).remove(key, Tree);
            }else {
                for (int i = 0; i < entries.size(); i++) {
                    if (entries.get(i).getKey().compareTo(key) <= 0 && entries.get(i+1).getKey().compareTo(key) > 0) {
                        child.get(i).remove(key, Tree);
                        break;
                    }
                }
            }
        }
    }

    public void updateRemove(MyBplusTree Tree){
        updateNode(Tree);
        if(child.size() < Tree.getOrder()/2 || child.size() < 2){
            if(isRoot){
                if(child.size() >= 2){
                    return;
                }
                else{
                    //merge then child
                    MyNode root = child.get(0);
                    Tree.root = root;
                    root.isRoot = true;
                    root.parent = null;
                    setEntries(null);
                    child = null;
                }
            }
            else{
                int currIdx = parent.child.indexOf(this);
                int prevIdx = currIdx - 1;
                int nextIdx = currIdx + 1;
                MyNode prevNode = null,nextNode = null;
                if(prevIdx >= 0){
                    prevNode = parent.child.get(prevIdx);
                }
                if(nextIdx < parent.child.size()){
                    nextNode = parent.child.get(nextIdx);
                }

                //previous node's key more than M/2
                if(prevNode != null && prevNode.child.size() > Tree.getOrder()/2 && prevNode.child.size() > 2){
                    //add to the end
                    int idx = prevNode.child.size()-1;
                    MyNode temp = prevNode.child.get(idx);
                    prevNode.child.remove(idx);
                    temp.parent = this;
                    this.child.add(0,temp);
                    prevNode.updateNode(Tree);
                    this.updateNode(Tree);
                    parent.updateRemove(Tree);
                }
                //next node's key more than M/2
                else if(nextNode != null && nextNode.child.size() > Tree.getOrder()/2 && nextNode.child.size() > 2){
                    //add to the end
                    MyNode temp = nextNode.child.get(0);
                    nextNode.child.remove(0);
                    temp.parent = this;
                    this.child.add(temp);
                    nextNode.updateNode(Tree);
                    this.updateNode(Tree);
                    parent.updateRemove(Tree);
                }
                //merge node
                else{
                    //merge previous node
                    if(prevNode != null && (prevNode.child.size() <= Tree.getOrder()/2 || prevNode.child.size() <= 2)){
                        for(int i = prevNode.child.size()-1; i >= 0; i--){
                            MyNode node = prevNode.child.get(i);
                            this.child.add(0, node);
                            node.parent = this;
                        }
                        prevNode.parent = null;
                        prevNode.child = null;
                        prevNode.setEntries(null);
                        parent.child.remove(prevNode);
                        this.updateNode(Tree);
                        parent.updateRemove(Tree);
                    }
                    //merge next node
                    else if(nextNode != null && (nextNode.child.size() <= Tree.getOrder()/2 || nextNode.child.size() <= 2)){
                        for(int i =0; i < nextNode.child.size(); i++){
                            MyNode node = nextNode.child.get(i);
                            this.child.add(node);
                            node.parent = this;
                        }
                        nextNode.parent = null;
                        nextNode.child = null;
                        nextNode.setEntries(null);
                        parent.child.remove(nextNode);
                        this.updateNode(Tree);
                        parent.updateRemove(Tree);
                    }
                }
            }
        }
    }

    public void remove(Integer key){
        int index = -1;
        for(int i = 0; i < entries.size(); i++){
            if(entries.get(i).getKey().compareTo(key) == 0){
                index = i;
                break;
            }
        }
        if(index != -1){
            entries.remove(index);
        }
    }

    public void updateInsert(MyBplusTree Tree){

        updateNode(Tree);

        //child.size > M, split the node
        if (child.size() > Tree.getOrder()) {
            //split the node
            MyNode left = new MyNode(false);
            MyNode right = new MyNode(false);
            //the size of left node and right node
            int leftSize = (Tree.getOrder() + 1) / 2 + (Tree.getOrder() + 1) % 2;
            int rightSize = (Tree.getOrder() + 1) / 2;
            //copy the node key and update it
            for (int i = 0; i < leftSize; i++){
                left.child.add(child.get(i));
                left.getEntries().add(new SimpleEntry(child.get(i).getEntries().get(0).getKey(), null));
                child.get(i).parent = left;
            }
            for (int i = 0; i < rightSize; i++){
                right.child.add(child.get(leftSize + i));
                right.getEntries().add(new SimpleEntry(child.get(leftSize + i).getEntries().get(0).getKey(), null));
                child.get(leftSize + i).parent = right;
            }

            //It is not root node
            if (parent != null) {
                //Adjust the relationship between parent and child
                int index = parent.child.indexOf(this);
                parent.child.remove(this);
                left.parent = parent;
                right.parent = parent;
                parent.child.add(index,left);
                parent.child.add(index + 1, right);
                setEntries(null);
                child = null;

                //update the parent node
                parent.updateInsert(Tree);
                parent = null;
                //It is root node
            }else {
                isRoot = false;
                MyNode parent = new MyNode(false, true);
                Tree.root = parent;
                left.parent = parent;
                right.parent = parent;
                parent.child.add(left);
                parent.child.add(right);
                setEntries(null);
                child = null;

                //update root node
                parent.updateInsert(Tree);
            }
        }
    }

    public void updateNode(MyBplusTree Tree){
        //child size equal to entries size
        if(entries.size() == child.size()){
            for(int i = 0;i < entries.size();i++){
                Integer key = child.get(i).entries.get(0).getKey();
                if(entries.get(i).getKey().compareTo(key) != 0){
                    entries.remove(i);
                    entries.add(i,new SimpleEntry(key, null));
                    if(!isRoot){
                        parent.updateNode(Tree);
                    }
                }
            }
        }
        //child split and this don't need to split
        else if(isRoot && child.size() >= 2 ||
                child.size() >= Tree.getOrder()/2 && child.size() <= Tree.getOrder() && child.size() >= 2){
            entries.clear();
            for(int i=0; i < child.size(); i++){
                Integer key = child.get(i).getEntries().get(0).getKey();
                entries.add(new SimpleEntry(key, null));
                if(!isRoot){
                    parent.updateNode(Tree);
                }
            }
        }
    }

    public boolean insertKey(Entry<Integer,Object> insertObj){
        if(entries.get(0).getKey().compareTo(insertObj.getKey()) > 0){
            entries.add(0,insertObj);
            return true;
        }
        else if(entries.get(entries.size()-1).getKey().compareTo(insertObj.getKey()) < 0){
            entries.add(entries.size(),insertObj);
            return true;
        }
        else{
            int i = 0;
            for(Entry<Integer,Object> entry : entries){
                if(entry.getKey().compareTo(insertObj.getKey()) == 0){
                    entries.get(i).setValue(insertObj.getValue());
                    return true;
                }
                else if(entry.getKey().compareTo(insertObj.getKey()) > 0){
                    entries.add(i,insertObj);
                    return true;
                }
                i++;
            }
        }
        return false;
    }

    public void find(MyBplusTree Tree){
        MyNode node = Tree.head;
        System.out.print("key list : ");
        while (node != null){
            for(Entry<Integer, Object> entry : node.entries){
                System.out.print(entry.getValue() + " ");
            }
            node = node.next;
        }
        System.out.println();
    }

    public void test(){
        Entry<Integer,Object> test1 = new SimpleEntry<Integer, Object>(1,1);
        Entry<Integer,Object> test2 = new SimpleEntry<Integer, Object>(2,2);
        Entry<Integer,Object> test3 = new SimpleEntry<Integer, Object>(3,3);
        Entry<Integer,Object> test4 = new SimpleEntry<Integer, Object>(4,4);
        entries.add(test1);
        entries.add(test2);
        entries.add(0,test4);
        entries.add(test3);
//        entries.add(test4);
        for(Entry<Integer,Object> entry : entries){
            System.out.println(entry.getValue());
        }
    }

    public void InsertOrUpdate(MyNode Tree,Entry<Integer,Object> newEntry){
        if(isLeaf){
            //It is leaf node,insert or update directly
            if(newEntry.getKey().compareTo(entries.get(0).getKey()) <=0){
                //less than minimum
//                entries.add();
            }
        }
        else{

        }
    }

    public static void main(String[] args) {
        MyNode myNode = new MyNode(true);
        myNode.entries = new ArrayList<>();
        myNode.test();
    }
}
