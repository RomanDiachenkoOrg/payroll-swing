package com.acme.payroll.ui;

import com.acme.payroll.service.AuthService;
import com.acme.payroll.service.ValidationException;
import com.acme.payroll.model.User;
import com.acme.payroll.ui.util.UiUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

/** Modal sign-in dialog. {@link #getUser()} returns the authenticated user, or null if cancelled. */
public class LoginDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final AuthService authService;
    private final JTextField emailField = new JTextField(22);
    private final JPasswordField passwordField = new JPasswordField(22);
    private final JLabel errorLabel = new JLabel(" ");
    private User user;

    public LoginDialog(JFrame owner, AuthService authService) {
        super(owner, "Acme Payroll - Sign in", true);
        this.authService = authService;
        setName("loginDialog");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        emailField.setName("login.email");
        passwordField.setName("login.password");
        errorLabel.setName("login.error");
        errorLabel.setForeground(new Color(180, 30, 30));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        GridBagConstraints c = UiUtil.gbc(0, 0);
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.LINE_START;
        form.add(UiUtil.heading("Acme Payroll"), c);
        form.add(UiUtil.label("Email:", emailField), UiUtil.gbc(0, 1));
        form.add(emailField, UiUtil.gbc(1, 1));
        form.add(UiUtil.label("Password:", passwordField), UiUtil.gbc(0, 2));
        form.add(passwordField, UiUtil.gbc(1, 2));
        c = UiUtil.gbc(1, 3);
        form.add(errorLabel, c);

        JButton loginButton = UiUtil.button("Sign in", "login.submit");
        JButton cancelButton = UiUtil.button("Cancel", "login.cancel");
        loginButton.addActionListener(e -> doLogin());
        cancelButton.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(loginButton);
        buttons.add(cancelButton);

        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(loginButton);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void doLogin() {
        try {
            user = authService.login(emailField.getText(), passwordField.getPassword());
            dispose();
        } catch (ValidationException ex) {
            errorLabel.setText(ex.getMessage());
            passwordField.setText("");
            if (ex.getMessage().startsWith("Email") || ex.getMessage().startsWith("Enter")) {
                emailField.requestFocusInWindow();
            } else {
                passwordField.requestFocusInWindow();
            }
            pack();
        } catch (RuntimeException ex) {
            UiUtil.showError(this, ex);
        }
    }

    public User getUser() {
        return user;
    }
}
