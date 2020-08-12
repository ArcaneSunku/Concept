import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Game implements Runnable{

    // Possible states for the application
    private enum AppState {
        NORMAL,
        EXIT,
        DEBUG,
        ERROR
    }

    // Possible states for the game
    private enum GameState {
        MAIN,
        GAME,
        BATTLE,
        CREDITS
    }

    private final List<String> m_Commands;
    private final List<String> m_UserInputBuffer;
    private final String m_Title = "Text-Based RPG";

    // Functional members
    private AppState m_AppState;
    private GameState m_GameState;
    private Thread m_Thread;

    private String m_LastClear;

    private int m_Error;
    private boolean m_Send;

    private int m_UserIndex;
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


        m_UserIndex = -1;
        m_Commands = new ArrayList<>();
        m_UserInputBuffer = new ArrayList<>();

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
                    checkState();

                    if(m_Send) {
                        handleCommands(m_UserInput.getText());
                        addToUserBuffer(m_UserInput.getText());
                        m_UserInput.setText("");
                        m_Send = false;
                    }
                    break;
                case DEBUG:
                    if(m_Send) {
                        handleDebugCommands(m_UserInput.getText());
                        addToUserBuffer(m_UserInput.getText());
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

        m_Frame = new JFrame(m_Title);
        m_Frame.setSize(400, 320);

        m_GameArea = new JTextArea();
        m_UserInput = new JFormattedTextField();

        m_GameArea.setToolTipText("This is the Game Board!");
        m_GameArea.setSize(400, 320);
        m_GameArea.setEnabled(false);
        changeTextColor(Color.WHITE);
        m_GameArea.setLineWrap(true);
        m_GameArea.setEditable(false);
        m_GameArea.setBackground(new Color(10, 52, 114));
        m_GameArea.setBorder(null);

        m_UserInput.setBorder(new EmptyBorder(0, 1, 0, 50));
        m_UserInput.setBackground(m_GameArea.getBackground());
        m_UserInput.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                int keyCode = e.getKeyCode();

                switch (keyCode) {
                    case KeyEvent.VK_UP:
                        if(m_UserIndex == -1)
                            break;

                        System.out.println(m_UserIndex);

                        if(m_UserIndex < 4) {
                            m_UserInput.setText(m_UserInputBuffer.get(m_UserIndex));
                            m_UserInput.repaint();

                            if(m_UserIndex != 0)
                                m_UserIndex--;
                        }

                        if(m_UserIndex == 0)
                            m_UserIndex = 3;

                        System.out.println(m_UserIndex);
                        break;
                    case KeyEvent.VK_ENTER:
                        m_Send = true;
                        break;
                }
            }
        });
        m_UserInput.setForeground(Color.WHITE);

        final DefaultCaret caret = (DefaultCaret) m_GameArea.getCaret();
        final JScrollPane scrollPane = new JScrollPane(m_GameArea);
        final JButton sendCommand = new JButton("Send");

        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        scrollPane.getVerticalScrollBar().setBorder(new LineBorder(new Color(4, 21, 57), 1, false));
        scrollPane.setBorder(new LineBorder(new Color(4, 21, 57), 1, false));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setBackground(null);
        scrollPane.createVerticalScrollBar();

        sendCommand.setBorder(new EmptyBorder(10,  10, 10, 10));
        sendCommand.setBackground(m_GameArea.getBackground());
        sendCommand.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                sendCommand.setForeground(Color.BLACK);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                sendCommand.setForeground(Color.WHITE);
            }
        });
        sendCommand.addActionListener((event)-> {
            if(!m_Send)
                m_Send = true;
        });
        sendCommand.setForeground(Color.WHITE);

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

        // Creating a list of commands
        m_Commands.add("/exit");
        m_Commands.add("/clear");
        m_Commands.add("/unclear");
        m_Commands.add("/debug");
    }

    private void checkState() {
        // TODO: Change what is displayed onto the game area depending on the game's enumerated state
    }

    // Decides what to do with what the user has typed
    private void handleCommands(String input) {
        if (input.equalsIgnoreCase(m_Commands.get(0))) {
            m_AppState = AppState.EXIT;
        } else if (input.equalsIgnoreCase(m_Commands.get(1))) { // probably temp
            m_LastClear = m_GameArea.getText();
            clearText();
        } else if (input.equalsIgnoreCase(m_Commands.get(2))) { // probably temp
            m_GameArea.append(String.format("%s", m_LastClear));
        } else if (input.equalsIgnoreCase(m_Commands.get(3))) {
            m_AppState = AppState.DEBUG;
            m_Frame.setTitle(m_Title + " - DEBUG MODE");
            changeTextColor(Color.RED);
        } else {
            appendLine(input);
        }
    }

    // Handles our commands when we're in debug mode
    private void handleDebugCommands(String input) {
        if (input.equalsIgnoreCase(m_Commands.get(0))) {
            m_AppState = AppState.EXIT;
        } else if(input.equalsIgnoreCase(m_Commands.get(3))) {
            m_AppState = AppState.NORMAL;
            m_Frame.setTitle(m_Title);
            changeTextColor(Color.WHITE);
        } else {
            appendLine(input);
        }
    }

    // Handles our errors we might run into during release and development
    private void handleErrors() {
        // TODO: Implement some sort of error handling
    }

    private void addToUserBuffer(String input) {
        boolean full = false;
        for(int i = 0; i < m_UserInputBuffer.size(); i++) {
            if(m_UserInputBuffer.get(i) == null) {
                m_UserInputBuffer.add(i, input);

                if(m_UserIndex < 3)
                    m_UserIndex++;
                break;
            } else {
                full = true;
            }
        }

        // if we get here that means the string is full
        if(full) {
            for(int i = 0; i < m_UserInputBuffer.size(); i++) {
                m_UserInputBuffer.add(i, m_UserInputBuffer.get(i + 1));
            }
        }
    }

    // Appends some text onto our game area
    private void append(String text) {
        m_GameArea.append(text);
    }

    // Appends some text onto our game area then goes to the next line
    private void appendLine(String text) {
        append(text + "\n");
    }

    // Changes the text on the game area, then refreshes it so we see the change immediately
    private void changeTextColor(Color color) {
        m_GameArea.setDisabledTextColor(color);
        m_GameArea.repaint();
    }

    // Simply clears the JTextArea we use as a "Game Board"
    private void clearText() {
        m_GameArea.replaceRange("", 0, m_GameArea.getText().length());
    }

}
