
//                                          !! RAM !!

package tts.domain;

import java.util.Deque;

/**
 * This class parses the single word n gives the original word as output.
 * Nepali like other informal language is very ambiguous.
 * So, user will normally input ambigous words.
 * The job of this class is to analyse the input words n remove its ambiguity which can be known by the TTS Engine.
 *
 * @author Sumit Shrestha
 */
public class AlphabetParser {
    
    public String parse( String Token ){
        Token = Token.toLowerCase();
        java.util.LinkedList<String> tempStack = new java.util.LinkedList<String>();
        char[] array = Token.toCharArray();
        for( int i=0; i<array.length; i++ ){
            if( tempStack.isEmpty() )
                tempStack.push( String.valueOf(array[i]) );
            else{
                String last = tempStack.getFirst();
                if( this.Vowels.contains(last) && last.equals( String.valueOf( array[i] )) ){// last is vowel and last is equal to current character i.e. aa or ee or oo
                    change( last, tempStack );                           
                }
                else
                    if( this.Consonants.contains(last) && array[i] == 'h' ){// previous char was consonant and present character(next) is 'h' i.e. gh, kh, etc
                        change( last, tempStack );                           
                    }
                    else
                        if( array[i-1]=='a' && array[i] == 'u' ){
                            change( "o", tempStack );
                        }
                        else
                            if( array[i-1] == 'a' && array[i] == 'i' ){
                                change("e", tempStack );
                            }
                            else{
                                tempStack.push( String.valueOf( array[i] ) );
                            }
            }
        }
        String result = "";        
        while( !tempStack.isEmpty() ){
            result += tempStack.pollLast();
        }        
        return result;
    }
    
    private void change(String last, Deque<String> tempStack) {
        // last is vowel and last is equal to current character i.e. aa or ee or oo
        tempStack.pop();
        tempStack.push(last + "1");
    }
    
    private final String Vowels = "aeiou";
    private final String Consonants = "kgcjtdpbs";
}