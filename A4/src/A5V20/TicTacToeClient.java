// Fig. 18.9: TicTacToeClient.java
// Client that let a user play Tic-Tac-Toe with another across a network.
package A5V20;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class TicTacToeClient extends JApplet  implements Runnable{
   private JTextField idField;
   private JTextArea displayArea;
   private JPanel boardPanel, panel2;
   private Square board[][], currentSquare;
   private Socket connection;
   private DataInputStream input;
   private DataOutputStream output;
   private char myMark;
   private boolean myTurn;
   private final char X_MARK = 'X', O_MARK = 'O';
   private JButton restartButton;
   private boolean gameOver1 = false;
   private boolean gameOver2 = false;
   private boolean flag = false;

   // Set up user-interface and board
   public void init()// only called once
   {

      Container container = getContentPane();
 
      // set up JTextArea to display messages to user
      displayArea = new JTextArea( 4, 30 );
      displayArea.setEditable( false );
      container.add( new JScrollPane( displayArea ), BorderLayout.SOUTH );

      // set up panel for squares in board
      boardPanel = new JPanel();
      boardPanel.setLayout( new GridLayout( 3, 3, 0, 0 ) );

      // create board
      board = new Square[ 3 ][ 3 ];

      // When creating a Square, the location argument to the constructor 
      // is a value from 0 to 8 indicating the position of the Square on 
      // the board. Values 0, 1, and 2 are the first row, values 3, 4, 
      // and 5 are the second row. Values 6, 7, and 8 are the third row.
      for ( int row = 0; row < board.length; row++ ) {

         for ( int column = 0; column < board[ row ].length; column++ ) {

            // create Square
            board[ row ][ column ] = new Square( ' ', row * 3 + column );
            boardPanel.add( board[ row ][ column ] );        
         }
      }

      // textfield to display player's mark
      idField = new JTextField();
      idField.setEditable( false );
      container.add( idField, BorderLayout.NORTH );
      
      // set up panel to contain boardPanel (for layout purposes)
      panel2 = new JPanel();
      panel2.add( boardPanel, BorderLayout.CENTER );
      container.add( panel2, BorderLayout.CENTER );


      restartButton = new JButton("Restart");
      restartButton.addActionListener(new ActionListener1());
      container.add(restartButton, BorderLayout.WEST);

   } // end method init

   public void start()
   {
      try {
         System.out.print("client:" + this.toString()+ " connecting through "+ getCodeBase().getHost() );
         connection = new Socket( getCodeBase().getHost(), 12345 );

         input = new DataInputStream( connection.getInputStream() );
         output = new DataOutputStream( connection.getOutputStream() );
      }
      catch ( IOException ioException ) {
         ioException.printStackTrace();         
      }

      // create and start output thread
      Thread outputThread = new Thread( this );
      outputThread.start();

   } // end method start

   // control thread that allows continuous update of displayArea
   public void run()
   {
      // get player's mark (X or O)
      try {
         myMark = input.readChar();

         // display player ID in event-dispatch thread
         SwingUtilities.invokeLater( 
            new Runnable() {         
               public void run()
               {
                  idField.setText("You are player \"" + myMark + "\"");
               }
            }
         );

        // logger.info("started player " + myMark );

         myTurn = ( myMark == X_MARK ? true : false );

         // receive messages sent to client and output them
         while ( connection.isConnected() ) {
            try {
               if(input.available()>0)
                  processMessage( input.readUTF() );//constantly reading processmessage, is it a valid move?
            } catch (IOException e) {
              // logger.error(e);
               e.printStackTrace();
            }
         }

      } // end try

      // process problems communicating with server
      catch ( IOException ioException ) {
        // logger.error(ioException);
         ioException.printStackTrace();
      }

   }  // end method run

   // process messages received by client
   private void processMessage( String message ) {
      try {
         switch (message) {
             case "Valid move.":
                 displayMessage("Valid move, please wait.\n");
                 setMark(currentSquare, myMark);
                 break;

             case "Invalid move, try again":
                 displayMessage(message + "\n");
                 myTurn = true;
                 break;
             case "Opponent moved":
                updateMove();
                displayMessage("Opponent moved. Your turn I think.\n");
                myTurn = true;
                 break;
            case "winner":
              // updateMove();
               setMark(currentSquare, myMark);
               displayMessage("you win. please wait for loser.\n");
              // myTurn = true;
               break;
            case "loser":
               updateMove();
               displayMessage("you lost. hit restart to play again.\n");
               gameOver1 = true;
               //flag = true;
               break;
            case "restart":
               char playerChar = ( myMark == X_MARK ? 'O' : 'X' );
               displayMessage("received restart request from player " + playerChar + "\n");
               //clear();
               gameOver2 = true;
               //myTurn = true;
               break;
            case "request ok":
              // char playerChar2 = ( myMark == X_MARK ? 'O' : 'X' );
               displayMessage("restart request successfully sent to other player\n please wait\n");
               break;
            case "restarted": // the loser
                //char playerChar2 = ( myMark == X_MARK ? 'O' : 'X' );
               clear();
               //myMark = ( myMark == X_MARK ? 'O' : 'X' );

               if (myMark == 'O') {  // if I'm the loser and I am now player O
                  myMark = 'X';
                          //output.writeInt( 12 );
                  myTurn = true;
               }
               else if (myMark == 'X'){ // if I'm the loser and I am now player X
                  myMark = 'O';
                  myTurn = true;
               }
               SwingUtilities.invokeLater(
                       new Runnable() {
                          public void run()
                          {
                             idField.setText( "\rYou are player \"" + myMark + "\"" );
                          }
                       }
               );

               displayMessage("Your opponent has agreed to a new game\n");
               //displayMessage("your are now player " + myMark + "\n");
               //myTurn = true;
               break;
            case "game reset":  // the winner
            if (myMark == 'X') { // If I'm the winner and now I'm X
               myMark = 'O';
               myTurn = false;
            }
            else if (myMark == 'O'){ // If I'm the winner and now I'm O
               myMark = 'X';
               //displayMessage("attempting to send switch \n");
               //output.writeInt( 12 );
               myTurn = false;
            }
               SwingUtilities.invokeLater(
                       new Runnable() {
                          public void run() {
                             idField.setText("\rYou are player \"" + myMark + "\"");
                          }
                       }
               );
               displayMessage("Your accept was successful.\n");
            break;

            case "switch1": // validate move
               displayMessage("good to go 1\n");
               myTurn = true;
               break;
            case "switch2": // other player moved
               displayMessage("please wait for opponent to move\n");
               myTurn = false;
               break;
             default:
                 displayMessage(message + "\n" );
                break;
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private void updateMove() {
      try {
         int location = input.readInt();
         int row = location / 3;
         int column = location % 3;

         setMark(board[row][column],
                 (myMark == X_MARK ? O_MARK : X_MARK));
         // displayMessage("Opponent moved. Your turn I think.\n");
      } catch (IOException io) {
         System.out.println("problem updating move");
         //logger.error(io);
         io.printStackTrace();
      }
   }

   // utility method called from other threads to manipulate 
   // outputArea in the event-dispatch thread
   private void displayMessage( final String messageToDisplay )
   {
      // display message from event-dispatch thread of execution
      SwingUtilities.invokeLater(
         new Runnable() {  // inner class to ensure GUI updates properly

            public void run() // updates displayArea
            {
               displayArea.append( messageToDisplay );
               displayArea.setCaretPosition( 
                  displayArea.getText().length() );
            }

         }  // end inner class

      ); // end call to SwingUtilities.invokeLater
   }

   // utility method to set mark on board in event-dispatch thread
   private void setMark( final Square squareToMark, final char mark )
   {
      SwingUtilities.invokeLater(
         new Runnable() {
            public void run()
            {
               squareToMark.setMark( mark );
            }
         }
      ); 
   }

   private void clear() {
         for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
               setMark(board[i][j], ' ');
      }

   /****************************************************************************
    **********************************************************
    **************************   TO   THE    SERVER
    *****************
    *********/
   // send message to server indicating clicked square
   public void sendClickedSquare( int location )
   {
      if ( myTurn && gameOver1 == false && gameOver2 == false) {
         // send location to server
         try {
            output.writeInt( location );  /** TO   THE    SERVER**/
            displayMessage("attempting to send " + location + "\n");
            myTurn = false; // now wait
         }

         // process problems communicating with server
         catch ( IOException ioException) {
            //logger.error(ioException);
            ioException.printStackTrace();
         }
      }
   }


   /******************************************************************/
   private class ActionListener1 implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent e) {
         if(gameOver1 == true) {
               try {
                  output.writeInt(10);
                  myTurn = false; // now wait
               } catch (IOException e1) {
                  e1.printStackTrace();
               }
               gameOver1 = false;
         }
         else if(gameOver2 == true) {
            try {
               output.writeInt(11);
               displayMessage("attempting to send 'I accept request' \n");
               clearBoard();
               myTurn = false; // now wait
            } catch (IOException e1) {
               e1.printStackTrace();
            }
            gameOver2 = false;
         }
         else {}
      }
  }

   public void clearBoard(){
      for (int i = 0; i < 3; i++)
         for (int j = 0; j < 3; j++)
            setMark(board[i][j], ' ');
   }
   /******************************************************************/
   // set current Square
   public void setCurrentSquare( Square square )
   {
      currentSquare = square;
   }

   // private inner class for the squares on the board
   private class Square extends JPanel { // known only to this.object
      private char mark;
      private int location;
   
      public Square( char squareMark, int squareLocation )
      {
         mark = squareMark;
         location = squareLocation;

         addMouseListener( 
            new MouseAdapter() {// multiple mouse events, has em all
               public void mouseReleased( MouseEvent e )
               {
                  setCurrentSquare( Square.this );//
                  sendClickedSquare( getSquareLocation() );
               }
            }  
         ); 

      } // end Square constructor

      // return preferred size of Square
      public Dimension getPreferredSize() 
      { 
         return new Dimension( 30, 30 );
      }

      // return minimum size of Square
      public Dimension getMinimumSize() 
      {
         return getPreferredSize();
      }

      // set mark for Square
      public void setMark( char newMark ) 
      { 
         mark = newMark; 
         repaint(); 
      }
   
      // return Square location
      public int getSquareLocation() 
      {
         return location; 
      }
   
      // draw Square
      public void paintComponent( Graphics g )
      {
         super.paintComponent( g );//inherit all the things paintComponent can do

         g.drawRect( 0, 0, 29, 29 );
         g.drawString( String.valueOf( mark ), 11, 20 );   
      }



   } // end inner-class Square

 
} // end class TicTacToeClient
