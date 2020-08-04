import javax.swing.*;
import java.awt.*;

public class Game {

    private enum State {
        NORMAL,
        EXIT,
        ERROR
    }

    private State m_State;

    private String m_LastClear;
    private boolean m_Running, m_Send;

    // Java Components for the Game Window

    private JFrame m_Frame;
    private JTextArea m_GameArea;
    private JButton m_SendCommand;
    private JTextField m_UserInput;

    // End

    public Game() {
        m_State = State.NORMAL;

        m_Running = false;
        m_Send = false;
    }

    public void start() {
        m_Running = true;
        gameLoop();
    }

    private void gameLoop() {
        initializeWindow();

        while(m_Running) {
            switch(m_State)
            {
                case NORMAL:
                    break;
                case ERROR:
                    handleErrors();
                    break;
                case EXIT:
                    m_Running = false;
                    break;
            }
        }

        m_Frame.dispose();
        System.exit(0);
    }

    private void initializeWindow() {
        final JPanel panel = new JPanel();

        panel.setLayout(new BorderLayout());

        m_Frame = new JFrame("Window");
        m_Frame.setSize(400, 320);

        m_GameArea = new JTextArea();
        m_UserInput = new JFormattedTextField();
        m_SendCommand = new JButton("Send");

        m_GameArea.setToolTipText("This is the Game Board!");
        m_GameArea.setSize(400, 320);
        m_GameArea.setLineWrap(true);
        m_GameArea.setEditable(false);
        final JScrollPane scrollPane = new JScrollPane(m_GameArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        m_SendCommand.addActionListener((event)-> {
            handleCommands(m_UserInput.getText());
            m_UserInput.setText("");
        });

        panel.add(m_UserInput, BorderLayout.CENTER);
        panel.add(m_SendCommand, BorderLayout.EAST);

        m_Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        m_Frame.setLayout(new BorderLayout());
        m_Frame.add(scrollPane, BorderLayout.CENTER);
        m_Frame.add(panel, BorderLayout.SOUTH);
        m_Frame.setResizable(false);
        m_Frame.setLocationRelativeTo(null);
        m_Frame.setVisible(true);
    }

    private void handleCommands(String input) {
        switch (input) {
            case "/exit":
                m_State = State.EXIT;
                break;
            case "/clear":
                m_LastClear = m_GameArea.getText();
                clearText();
                break;
            case "/unclear":
                m_GameArea.append(String.format("%s", m_LastClear));
                break;
            case "/debug":
                m_State = State.ERROR;
                break;
            default:
                m_GameArea.append(input + "\n");
                break;
        }
    }

    private void handleErrors() {

    }

    private void clearText() {
        m_GameArea.replaceRange("", 0, m_GameArea.getText().length());
    }

}
