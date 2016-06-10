package pl.uncertainflowshopsolver.testdata;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Piotr Kubicki, created on 09.06.2016.
 */
public class SublistDemo {



    public static void main(String[] args) {

        //create an ArrayList object
        ArrayList arrayList = new ArrayList();

        //Add elements to Arraylist
        arrayList.add("1");
        arrayList.add("2");
        arrayList.add("3");
        arrayList.add("4");
        arrayList.add("5");

    /*
       To get a sub list of Java ArrayList use
       List subList(int startIndex, int endIndex) method.
       This method returns an object of type List containing elements from
       startIndex to endIndex - 1.
    */

//        List sublist = arrayList.subList(1,3);
//        List lst = new ArrayList(sublist);

        List lst = arrayList.subList(0,3);

                //display elements of sub list.
        System.out.println("Sub list contains : ");
        for(int i=0; i< lst.size() ; i++)
            System.out.println(lst.get(i));

        lst.toString();
        lst.removeAll(lst);
        lst.toString();

    /*
      Sub List returned by subList method is backed by original Arraylist. So any
      changes made to sub list will also be REFLECTED in the original Arraylist.
    */
        //remove one element from sub list
//        Object obj = lst.remove(0);
//        System.out.println(obj + " is removed from sub list");


        //print original ArrayList
        System.out.println("After removing " +  " from sub list, original ArrayList contains : ");
        for(int i=0; i< arrayList.size() ; i++)
            System.out.println(arrayList.get(i));

    }
}
