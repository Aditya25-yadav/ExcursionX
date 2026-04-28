import ui.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        // FIX: Force cross-platform (Metal) Look-and-Feel so our custom dark-theme colors
        //      on JTextField / JTextArea / JPasswordField always render correctly on Windows.
        //      The default Windows native L&F overrides setForeground/setBackground, making
        //      typed text invisible on dark backgrounds.
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("[Main] Could not set cross-platform L&F: " + e.getMessage());
        }
        SwingUtilities.invokeLater(() -> {
            new LoginFrame();
        });
    }
}