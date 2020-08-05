import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Game implements Runnable{

    // Possible states for the application
    private enum AppState {
        NORMAL,
        EXIT,
        ERROR
    }

    // Possible states for the game
    private enum GameState {
        MAIN,
        GAME,
        BATTLE,
        CREDITS
    }

    // Functional members
    private AppState m_AppState;
    private GameState m_GameState;
    private Thread m_Thread;

    private String m_LastClear;

    private int m_Error;
    private boolean m_Send;
    private volatile boolean mv_Running;

    // Java Components for the Game Window

    private JFrame m_Frame;
    private JTextArea m_GameArea;
    private JTextField m_UserInput;

    // End

    // Game Members
    boolean m_NewCharacter;

    public Game() {
        m_AppState = AppState.NORMAL;
        m_GameState = GameState.MAIN;

        mv_Running = false;
        m_Send = false;
    }

    // Sets everything to on, so we can start
    public synchronized void start() {
        if(mv_Running) return;

        m_Thread = new Thread(this);
        mv_Running = true;
        m_Thread.start();
    }

    // Main game loop
    @Override
    public void run() {
        initializeWindow();

        while(mv_Running) {
            switch (m_AppState) {
                case NORMAL:
                    if(m_Send) {
                        handleCommands(m_UserInput.getText());
                        m_UserInput.setText("");
                        m_Send = false;
                    }
                    break;
                case ERROR:
                    handleErrors();
                    break;
                case EXIT:
                    mv_Running = false;
                    break;
            }
        }

        stop();
    }

    // Stops and closes everything, hopefully
    private synchronized void stop() {
        try {
            m_Thread.join(1);
            m_Frame.dispose();
            System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    // Create everything we need for a functioning Java Window
    private void initializeWindow() {
        final JPanel panel = new JPanel();

        panel.setLayout(new BorderLayout());

        m_Frame = new JFrame("Window");
        m_Frame.setSize(400, 320);

        m_GameArea = new JTextArea();
        m_UserInput = new JFormattedTextField();

        m_GameArea.setToolTipText("This is the Game Board!");
        m_GameArea.setSize(400, 320);
        m_GameArea.setLineWrap(true);
        m_GameArea.setEditable(false);

        m_UserInput.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    m_Send = true;
                }
            }
        });

        final DefaultCaret caret = (DefaultCaret) m_GameArea.getCaret();
        final JScrollPane scrollPane = new JScrollPane(m_GameArea);
        final JButton sendCommand = new JButton("Send");

        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.createVerticalScrollBar();

        sendCommand.addActionListener((event)-> {
            if(!m_Send)
                m_Send = true;
        });

        panel.add(m_UserInput, BorderLayout.CENTER);
        panel.add(sendCommand, BorderLayout.EAST);

        m_Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        m_Frame.setLayout(new BorderLayout());
        m_Frame.add(scrollPane, BorderLayout.CENTER);
        m_Frame.add(panel, BorderLayout.SOUTH);
        m_Frame.setResizable(false);
        m_Frame.setLocationRelativeTo(null);
        m_Frame.setVisible(true);

        m_Frame.setFocusable(true);
    }

    // Decides what to do with what the user has typed
    private void handleCommands(String input) {
        if (input.equalsIgnoreCase("/exit")) {
            m_AppState = AppState.EXIT;
        } else if (input.equalsIgnoreCase("/clear")) { // probably temp
            m_LastClear = m_GameArea.getText();
            clearText();
        } else if (input.equalsIgnoreCase("/unclear")) { // probably temp
            m_GameArea.append(String.format("%s", m_LastClear));
        } else if (input.equalsIgnoreCase("/debug")) {
            m_AppState = AppState.ERROR;
        } else {
            m_GameArea.append(input + "\n");
        }
    }

    private void handleErrors() {
        // TODO: Implement some sort of error handling
    }

    // Simply clears the JTextArea we use as a "Game Board"
    private void clearText() {
        m_GameArea.replaceRange("", 0, m_GameArea.getText().length());
    }

}
