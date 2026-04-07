
//                              !! RAM !!

package tts.domain;

/**
 *
 * @author Sumit Shresth
 */
public class Player extends javax.swing.SwingWorker<String,String>{
    
    public Player( javax.swing.JTextPane textarea, tts.domain.TTSEngine Player, final tts.domain.FrameInterface listner ){
        this.Text = textarea;
        this.Player = Player;
        this.Listener = listner;
    }
    
   public void playstring( String inputtext ) throws Exception
   {     
       this.txt_ptr = 0;
          String[] sentencearray = tts.domain.SentenceSplitter.splitby( inputtext, '.' );          
          for( int j=0;j<sentencearray.length;j++){          
          String[] array = tts.domain.SentenceSplitter.splitby( sentencearray[j] , ' ' );
          for( int i=0;i< array.length;i++){
              String SpokenWord = this.WordParser.parse( array[i] );              
              super.publish( array[i] );
             String[] wordstring = Player .returnwordarray( SpokenWord );
             for( int r=0; r<wordstring.length; r++ ){
                 if( super.isCancelled() ) return;
                 while( this.pause ){if( super.isCancelled() ) return;}
                    playFile( wordstring[r] );
             }             
         }
         // give some delay after each sentence spoken
         if( j < sentencearray.length -1 )
            java.lang.Thread.sleep( 500 );                
         }         
   }
   
   @Override
   public String doInBackground(){
       try{
           this.play();
           this.playstring( Text.getText() );
           return this.DONE;
       }
       catch( Exception e ){
           e.printStackTrace();
           return e.getMessage();
       }
   }
   
   @Override
   public void done(){
       try{
           String state = this.get();
           this.Listener.onPlayCompletion( state );
           if( state == this.DONE ){
               javax.swing.JOptionPane.showMessageDialog( null, "Text played Successfully","Played", javax.swing.JOptionPane.INFORMATION_MESSAGE );
           }
           else{
               javax.swing.JOptionPane.showMessageDialog( null, state,"Error Playing", javax.swing.JOptionPane.ERROR_MESSAGE );
           }
       }
       catch( Exception e ){
           
       }
   }
   
   @Override
   protected void process( java.util.List<String> words ){
       String tempText = this.Text.getText().substring(txt_ptr);
       String SpokenWord = words.get( words.size() -1 );
       
       int i = tempText.indexOf( SpokenWord ) + this.txt_ptr;
       if( i > -1 ){
           this.Text.setCaretPosition(i);
           this.Text.select( i, i + SpokenWord.length() );
           this.txt_ptr = i+SpokenWord.length();           
       }
       else{
           javax.swing.JOptionPane.showMessageDialog( null, "return -1","Error", javax.swing.JOptionPane.ERROR_MESSAGE );
       }
   }
   
   private void playFile( String file ) throws Exception {               
        Player.play( file );        
    }
   
   public void pause(){
       this.pause = true;
   }
   
   public void play(){
       this.pause = false;
   }
   
   public boolean isPaused(){
       return this.pause;
   }
   
   public static final String DONE = "Done";   
   private final tts.domain.FrameInterface Listener;
   private final javax.swing.JTextPane Text;
   private int txt_ptr;
   private tts.domain.TTSEngine Player;   
   private tts.domain.AlphabetParser WordParser = new tts.domain.AlphabetParser();
   private boolean pause = false;
}