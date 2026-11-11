package com.acme.payroll;

import com.acme.payroll.db.Database;
import com.acme.payroll.db.DbInitializer;
import com.acme.payroll.model.User;
import com.acme.payroll.ui.LoginDialog;
import com.acme.payroll.ui.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Application entry point. */
public final class PayrollApp {

    private static final Logger LOG = Logger.getLogger(PayrollApp.class.getName());

    private PayrollApp() {
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOG.log(Level.FINE, "System look and feel not available", e);
        }

        final AppContext ctx;
        try {
            Database db = Database.fileDatabase();
            DbInitializer.initialize(db);
            ctx = new AppContext(db);
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Startup failed", e);
            JOptionPane.showMessageDialog(null, "Could not open the payroll database:\n" + e.getMessage(),
                    "Acme Payroll", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> LOG.log(Level.SEVERE, "Uncaught exception in " + t.getName(), e));
        SwingUtilities.invokeLater(() -> showLogin(ctx));
    }

    static void showLogin(AppContext ctx) {
        LoginDialog login = new LoginDialog(null, ctx.getAuthService());
        login.setVisible(true);
        User user = login.getUser();
        if (user == null) {
            ctx.getDatabase().shutdown();
            System.exit(0);
            return;
        }
        MainFrame frame = new MainFrame(ctx, () -> showLogin(ctx));
        frame.setVisible(true);
    }
}
