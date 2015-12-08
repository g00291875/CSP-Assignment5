// Fig. 18.8: TicTacToeServer.java
// This class maintains a game of Tic-Tac-Toe for two client applets.#
package A5V21;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TicTacToeServer extends JFrame {
   private char[] board;           
   private JTextArea outputArea;
   private Player[] players;
   private ServerSocket server;
   private int currentPlayer;
   private final int PLAYER_X = 0, PLAYER_O = 1;
   private final char X_MARK = 'X', O_MARK = 'O';
   private String whoIsWinner = null;
    private boolean flag = false;
    private boolean reset = false;
    private boolean reset2 = false;
    private boolean winnerFlag = false;
   //private volatile int [] checkBoard = []
   ExecutorService executor = Executors.newFixedThreadPool(2);
   private static final Logger logger = Logger.getLogger(TicTacToeServer.class);


   // set up tic-tac-toe server and GUI that displays messages
   public TicTacToeServer()
   {
      super( "Tic-Tac-Toe Server" );

      board = new char[ 9 ]; 
      players = new Player[ 2 ];
      currentPlayer = PLAYER_X;
     // checkBoard = [3][3];
      //ExecutorService endGameThread = Executors.newCachedThreadPool();
 
      // set up ServerSocket
      try {
         server = new ServerSocket( 12345, 2 );
      }

      // process problems creating ServerSocket
      catch( IOException ioException ) {
         ioException.printStackTrace();
         //logger.error(ioException);
         ioException.printStackTrace();
         System.exit( 1 );
      }

      // set up JTextArea to display messages during execution
      outputArea = new JTextArea();
      getContentPane().add( outputArea, BorderLayout.CENTER );
       //container.add( new JScrollPane( displayArea ), BorderLayout.SOUTH );
      //outputArea.setText( "Server awaiting connections\n" );

      setSize( 300, 300 );
      setVisible( true );

   } // end TicTacToeServer constructor

   /************   *         *
    *                *     *
    ************        *
    *                 *    *
    ************   *          */

   // wait for two connections so game can be played
   public void execute()
   {
      // wait for each client to connect
      for ( int i = 0; i < players.length; i++ ) {


         try {
            players[ i ] = new Player( server.accept(), i );//array of threads
            executor.execute(players[ i ]);
            logger.info("player  " + i + " connected" );
            //Runnable worker = new players[ i ];
           // players[ i ].start();//runs new thread L211
            // players [1] / go to L175
         }

         // process problems receiving connection from client
         catch( IOException ioException ) {
            //logger.error(ioException);
            ioException.printStackTrace();
            System.exit( 1 );
         }
      }//finished this

      // Player X is suspended until Player O connects. 
      // Resume player X now.          
      synchronized ( players[ PLAYER_X ] ) {//this synchronised block for playerx
         players[ PLAYER_X ].setSuspended( false );   //call set suspend = false L278
         players[ PLAYER_X ].notify();// We wake up thread
      }//player x is now in L230
  
   }  // end method execute

   /************************
    *                  ***************
    *              displayMessage  ******************
    *                                       ********************
    ********************************************************************************/
   
   // utility method called from other threads to manipulate 
   // outputArea in the event-dispatch thread
   private void displayMessage( final String messageToDisplay ) // called by both threads
   {
      // display message from event-dispatch thread of execution
      SwingUtilities.invokeLater(
         new Runnable() {  // inner class to ensure GUI updates properly

            public void run() // updates outputArea
            {
               outputArea.append( messageToDisplay );
               outputArea.setCaretPosition( 
                  outputArea.getText().length() );
            }

         }  // end inner class

      ); // end call to SwingUtilities.invokeLater
   }
   /*****************************************************************
    *          validateAndMove    *******************
    *         *********************
    **********************/
   // Determine if a move is valid. This method is synchronized because
   // only one move can be made at a time.      //1-9           //[;ayer
   public synchronized boolean validateAndMove( int location, int player )
   {
      boolean moveDone = false;

      // while not current player, must wait for turn
      while ( player != currentPlayer ) { // where is current player? L25 at start
         
         // wait for turn
         try {
            wait();
         }

         // catch wait interruptions
         catch( InterruptedException interruptedException ) {
            //logger.error(interruptedException);
            interruptedException.printStackTrace();
         }
      }

       if (location == 10){
           //clear board
           currentPlayer = (currentPlayer + 1) % 2; //
           players[currentPlayer].otherPlayerMoved(location);// this method L196
           notify(); // tell waiting player to continue
           return true;
       }
       else if (location == 11){
           //clear board
           currentPlayer = (currentPlayer + 1) % 2; //
           players[currentPlayer].otherPlayerMoved(location);// this method L196
           notify(); // tell waiting player to continue
           return true;
       }
       else if (location == 12){
           currentPlayer = (currentPlayer + 1) % 2; //
           players[currentPlayer].otherPlayerMoved(location);// this method L196
           notify(); // tell waiting player to continue
           return true;
       }
       else if (!isOccupied(location) && !isGameOver()) {// is there a number in here?

            // set move in board array, the mark put it
            board[location] = currentPlayer == PLAYER_X ? X_MARK : O_MARK;

            /*** CHANGE CURRENT PLAYER *** *** CHANGE CURRENT PLAYER *** *** CHANGE CURRENT PLAYER ***/
            currentPlayer = (currentPlayer + 1) % 2; //

            // let new current player know that move occurred, ie other o
            players[currentPlayer].otherPlayerMoved(location);// this method L196

            notify(); // tell waiting player to continue
            //

            // tell player that made move that the move was valid
            return true;
         }
       else if (isGameOver()) {// is there a number in here?

           // set move in board array, the mark put it
           //board[location] = currentPlayer == PLAYER_X ? X_MARK : O_MARK;

           /*** CHANGE CURRENT PLAYER *** *** CHANGE CURRENT PLAYER *** *** CHANGE CURRENT PLAYER ***/
           //currentPlayer = (currentPlayer + 1) % 2; //

           // let new current player know that move occurred, ie other o
           //players[currentPlayer].otherPlayerMoved(11);// this method L196

           //notify(); // tell waiting player to continue
           //

           // tell player that made move that the move was valid
           return true;
       }
      // tell player that made move that the move was not valid
      else 
         return false;

   } // end method validateAndMove

   /******
    ***************  end validateAndMove
    *******************************************************************/

   // determine whether location is occupied
   public boolean isOccupied( int location )
   {
      if ( board[ location ] == X_MARK || board [ location ] == O_MARK )
          return true;
      else
          return false;
   }

                                             /** IS GAME OVER *** IS GAME OVER ** * IS GAME OVER ** * IS GAME OVER **/
   public boolean isGameOver()//method outside the 2 threads, has to access the board
   {
      //NW to SE diagonal
      if (board [0] == X_MARK && board [1] == X_MARK && board [2] == X_MARK) {
           whoIsWinner = "Player X is the winner";
           return true;
      }
      else if (board [3] == X_MARK && board [4] == X_MARK && board [5] == X_MARK) {
         whoIsWinner = "Player X is the winner";
         return true;
      }
      else if (board [6] == X_MARK && board [7] == X_MARK && board [8] == X_MARK) {
         whoIsWinner = "Player X is the winner";
         return true;
      }
      else if (board [0] == X_MARK && board [3] == X_MARK && board [6] == X_MARK) {
         whoIsWinner = "Player X is the winner";
         return true;
      }
      else if (board [1] == X_MARK && board [4] == X_MARK && board [7] == X_MARK) {
         whoIsWinner = "Player X is the winner";
         return true;
      }
      else if (board [2] == X_MARK && board [5] == X_MARK && board [8] == X_MARK) {
         whoIsWinner = "Player X is the winner";
         return true;
      }
      else if (board [0] == X_MARK && board [4] == X_MARK && board [8] == X_MARK) {
         whoIsWinner = "Player X is the winner";
         return true;
      }
      else if (board [6] == X_MARK && board [4] == X_MARK && board [2] == X_MARK) {
         whoIsWinner = "Player X is the winner";
         return true;
      }
      /********************************************************/
      //NW to SE diagonal
      else if (board [0] == O_MARK && board [1] == O_MARK && board [2] == O_MARK) {
         whoIsWinner = "Player O is the winner";
          winnerFlag = true;
         return true;
      }
      else if (board [3] == O_MARK && board [4] == O_MARK && board [5] == O_MARK) {
         whoIsWinner = "Player O is the winner";
          winnerFlag = true;
         return true;
      }
      else if (board [6] == O_MARK && board [7] == O_MARK && board [8] ==O_MARK) {
         whoIsWinner = "Player O is the winner";
          winnerFlag = true;
         return true;
      }
      else if (board [0] == O_MARK && board [3] == O_MARK && board [6] == O_MARK) {
         whoIsWinner = "Player O is the winner";
          winnerFlag = true;
         return true;
      }
      else if (board [1] == O_MARK  && board [4] == O_MARK  && board [7] == O_MARK ) {
         whoIsWinner = "Player O is the winner";
          winnerFlag = true;
         return true;
      }
      else if (board [2] == O_MARK  && board [5] == O_MARK  && board [8] == O_MARK ) {
         whoIsWinner = "Player O is the winner";
          winnerFlag = true;
         return true;
      }
      else if (board [0] == O_MARK  && board [4] ==O_MARK  && board [8] == O_MARK ) {
         whoIsWinner = "Player O is the winner";
          winnerFlag = true;
         return true;
      }
      else if (board [6] == X_MARK && board [4] == X_MARK && board [2] == X_MARK) {
         whoIsWinner = "Player O is the winner";
          winnerFlag = true;
         return true;
      }
      else if (board[0] != '\0' && board[1] != '\0' && board[2] != '\0' && board[3] != '\0'
              && board[4] != '\0' && board[5] != '\0' && board[6] != '\0' && board[7] != '\0' && board[7] != '\0' && board[8] != '\0'){
         whoIsWinner = "draw";
         return true;
      }
      return false;  // this is left as an exercise
   }

   public void clearBoard(){
      for (int i=0; i < board.length; i++)
         board [i]= '\0';
   }

   /****** ######### ******** &&&&&&&&&& ######### *************************/
   public static void main( String args[] )                       /********/
   {
      TicTacToeServer application = new TicTacToeServer();
      application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE ); /****/
      application.execute();
   }                                                           /*******/
   /****** ######### ******** &&&&&&&&&& ######### ******************/

   // private inner class Player manages each Player as a thread
   private class Player extends Thread {
      private Socket connection;
      private DataInputStream input;
      private DataOutputStream output;
      private int playerNumber;
      private char mark;
      protected boolean suspended = true;

      // set up Player thread
      public Player( Socket socket, int number )
      {
         playerNumber = number;

         // specify player's mark
         //variable at top
         //player x = 0 and y = 1
         mark = ( playerNumber == PLAYER_X ? X_MARK : O_MARK );// if this is true

         connection = socket;
         
         // obtain streams from Socket
         try {
            input = new DataInputStream( connection.getInputStream() );
            output = new DataOutputStream( connection.getOutputStream() );
         }

         // process problems getting streams
         catch( IOException ioException ) {
            logger.error(ioException);
            ioException.printStackTrace();
            System.exit( 1 );
         }

      } // end Player constructor

      /**$$$$$$$$$$$$$$$$$$$$$$$$$$$ otherPlayerMoved    otherPlayerMoved   otherPlayerMoved   **********/
      /**$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$**/
      // send message that other player moved
      public void otherPlayerMoved( int location )  // where does this return to????
      {
         try {
            if (location == 10) {
               displayMessage("\nsending restart request to player [" +  playerNumber + "]\n");
               output.writeUTF("restart");
                reset2 = true;
            }
            else if (location == 11) {
                               displayMessage("\nsending player accepts restart request to player [" +  playerNumber + "]\n");
                               output.writeUTF("restarted");
                reset = true;
            }
            else if (location == 12) {
                displayMessage("\nletting player [" +  playerNumber + "] go first \n");
                output.writeUTF("switch2");
            }
            else if (isGameOver()){
                displayMessage("\nsending losing move to to player [" +  playerNumber + "]\n");
                output.writeUTF("loser");
                output.writeInt(location);
            }
            else {
                displayMessage("\nsending location: " + location + " to player [" +  playerNumber + "]\n");
               output.writeUTF("Opponent moved");
               output.writeInt(location);
            }
         }

         // process problems sending message
         catch ( IOException ioException ) {
            //logger.error(ioException);
            ioException.printStackTrace();
         }
      }
      /**$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$**/
      /**$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$********/
       // implement thread pool here, what common variable are the 2 threads sharing?
      // where are the threads being started from? ANS: server constructor
      // control thread's execution

      public void run()
      {
               try {/*************************************************************START RUN ***/
                   displayMessage("Player " + (playerNumber ==
                           PLAYER_X ? X_MARK : O_MARK) + " connected\n");//ternary operator, x_mark true? or 0_mark false?
                   //ie. someone has to be x and someone has to o

                   output.writeChar(mark); // send player's mark// output = stream

                   // send message indicating connection
                   output.writeUTF("Player " + (playerNumber == PLAYER_X ?
                           "X connected\n" : "O connected, please wait\n"));

                   // if player X, wait for another player to arrive
                   if (mark == X_MARK) {  // Only if we're player x
                       output.writeUTF("Waiting for another player");

                       // wait for player O
                       try {
                           synchronized (this) {   // synchronised on this object
                               while (suspended)// variable suspend is set to true, while it's true
                                   wait();  //once
                           }
                       }

                       // process interruptions while waiting
                       catch (InterruptedException exception) {
                           logger.error(exception);
                           exception.printStackTrace();
                       }

                       // send message that other player connected and
                       // player X can make a move
                       output.writeUTF("Other player connected. Your move.");
                   }/*****************************************************************************/

                   // while game not over
                   //  while (true) {
                   while(true){
                       if (reset == true) {
                           displayMessage("exited accept loop\n");
                           reset2 = true;
//                           while(true) {
//                               int location = input.readInt();// read something from stream
//                               displayMessage(" received " + location + " from  player [" + currentPlayer + "]\n");
//                               if (winnerFlag == true) {
//                                   if (validateAndMove(location, playerNumber)) { // returns true if it's a valid move
//                                       displayMessage("current player thread is now [" + currentPlayer + "]" + "\n");
//
//                                       output.writeUTF("switch2");// this thread to other client
//                                       displayMessage("switching 2\n");
//                                       break;
//                                   }//player x is now in L230
//                               }
//                           }
                       }

                   while (!isGameOver()) {          /***** MAIN LOOP MAIN LOOP MAIN LOOP MAIN LOOP MAIN LOOP **/
                       try {
                           reset = false;
                           reset2 = false;

                           int location = input.readInt();// read something from stream
                           displayMessage(" received " + location + " from  player [" + currentPlayer + "]\n");
                           if (validateAndMove(location, playerNumber)) { // returns true if it's a valid move
                              // displayMessage("current player thread is now [" + currentPlayer + "]" + "\n");

                               if (location == 10 && !isGameOver() ) {
                                   //displayMessage("bug testing: received " + location + "\n");
                                   //displayMessage("current player thread is now [" + currentPlayer + "]" + "\n");
                                   if(reset == false) {
                                       output.writeUTF("request ok");// this thread to other client
                                    //   displayMessage("validating request ok to player [" + currentPlayer + "]\n");
                                       break;
                                   }
                               } else if (location == 11) {
                                   displayMessage("\n3rd testing: received " + location);
                                   output.writeUTF("restart");// this thread to other client
                                   break;
                               }
                               else if (location == 12) {
                                   output.writeUTF("switch1");// this thread to other client
                                   displayMessage("switching 2\n");
                               }else if (location != 10 && !isGameOver()) {
                                   output.writeUTF("Valid move.");// this thread to other client
                                    displayMessage("validating player who made move\n");
                               } else if (isGameOver() && flag == false) {
                                   displayMessage("validating player who made move\n");
                                   output.writeUTF("winner");
                                   flag = true;
                               }
                           } else
                               output.writeUTF("Invalid move, try again");
                       } catch (IOException ioException) {
                           logger.error(ioException);
                           ioException.printStackTrace();
                       }
                   }//end while game over

/********************************************************************************************************/

/********************************************************************************************************/

                   try {
                       //currentPlayer = (currentPlayer + 1) % 2; //
                       //notify(); // tell waiting player to continue
                       clearBoard();
                           while (reset == false && reset2 == false) {
                               //  displayMessage("\ncurrent player [" + currentPlayer + "] \n");
                               int location = input.readInt();// read something from stream
                               //   displayMessage("\nlocation is " + location +  " for current player [" + currentPlayer + "]\n");

                               displayMessage("reset loop received " + location + " from  player [" + currentPlayer + "]\n");

                               if (validateAndMove(location, playerNumber)) { // returns true if it's a valid move
                                   displayMessage("current player thread is now [" + currentPlayer + "]" + "\n");

                                   if (location == 11) {
                                       //displayMessage("2nd loop testing: received " + location);
                                       displayMessage("validating request accepted to player [" + currentPlayer + "]\n");
                                       output.writeUTF("game reset");// this thread to other client
                                       break;
                                   }
                                   else if (location == 12) {
                                       output.writeUTF("switch2");// this thread to other client
                                       displayMessage("switching 2\n");
                                       break;
                                   }
                               }
                           }
                       }
                       catch(Exception e){
                           e.printStackTrace();
                           // System.exit(1);
                       }
                   //displayMessage("exited 2nd loop\n");
                   }// while true
               }

                   // process problems communicating with client
                   catch(IOException ioException){
                       logger.error(ioException);
                       ioException.printStackTrace();
                        System.exit(1);
                   }
      } // end method run

      // set whether or not thread is suspended
      public void setSuspended( boolean status )
      {
         suspended = status;
      }
   
   } // end class Player
} // end class TicTacToeServer

