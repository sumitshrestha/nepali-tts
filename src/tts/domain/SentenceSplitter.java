
//                          !! RAM !!

package tts.domain;

import java.util.*;

/**
 *
 * @author Sumit Shrestha
 */
      public class SentenceSplitter{
////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////     
       // function: splitby starts
       final static String[] splitby( String test, char s /* splitting character*/ )
       {
       
       //here it actually removes the surrounding splitting character 
       // 
       
       
       //printing the test
       //System.out.println("the incomming string is"+ test);
       /*
       here, i am atually trying to remove the surrounding splitting character in the given string by finding the beggining and the last position of the splitting character and then getting the substring of the given string between the begin and last index of the string which us then given to the given string. 
       */
        int begin=0;//initializing the start position.
       while( test.charAt(begin) == s )//loop to find the start of the splitting character in the string.
       {
        begin++;
        //System.out.println("\nvlaue of begin"+begin);
       }
        int last = test.length() -1 ;//this initializes the last position
       while (test.charAt( last) == s)//starting the loop to find the splitting character in the last position.
       {
        last--;
        //System.out.println("\nvalue of last:"+last);
       }
       /*System.out.println("\ndebugging\nthis is the string in splitby:="+ test+"\n this is the value of start"+begin+" \nthis is the value of last"+last+"\nthe length is "+test.length());*/
       if( begin <= last )//checks if the string is not only of the splitting character 
       {
        test = test.substring( begin, last+1 );//get the substring from the test. last is incremented to give index just the last position of the splitting character
       }
       else//else first prints the error message and then initializes string by the empty string.
       {
        System.out.println("\n\the string contains only "+s+" \n\t\t\t\twhich is error");
        test="";
        //System.exit(0);
        //return null;
       }
       /*System.out.println("\ndebugging\nthis is the string in splitby:="+ test+"\n this is the value of start"+begin+" \nthis is the value of last"+last+"\nthe length is "+test.length());*/
       
       ArrayList array=new ArrayList();//arraylist created
       // insert splitting character at the last
       test=test+ s;
       
       int ind=0;
       int previous=0;
       int len_of_substring=0;
       
       for ( int i=0; i<test.length(); )//no incremation which is done at the end of the loop.
       { 
             previous=ind;// handing the previous index  of the blank space  
             ind=test.indexOf( String.valueOf(s) , i);//finding index of the closest ' ' from i'th index of the string
             
             //System.out.println("the substring is "+test.substring ( previous,ind).replace(s,' ').trim() );
             array.add(test.substring ( previous,ind).replace(s, ' ').trim());
             /*
             the above statement first gets the substring and then converts the
             
             surrounding character of the string formed to the blank space to trim it
             
             thus removing it and then adds it to the arraylist
             */
             i=ind+1;//incrementing the loop for the next occurrence of the char
             
       }// for loop ends
       
       /*
       here, i am checking that if the size is zero or the particular element in the arraylist contains null string then we remove it.
       */
       
       for( int i=0;i<array.size();i++)
       {
       
       if( ((String) array.get(i)).length() == 0 )//if the array element is null
       {
       array.remove(i);
       --i;//take the increment variable back
       }//if ends
       
       }// for ends
       
       String [] a=new String[array.size()] ;// String array being created 
       //System.out.println("the size of the array list is "+array.size());
       array.toArray(a);	// the array of the string is created
       
       return a;// returning the String array
       
       }// function: split ends
       
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////   

}
