package it.fleetmanager.ui.utenti;

import it.fleetmanager.model.Utente;
import it.fleetmanager.util.RuoloUtente;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class UtenteDialog {

    private final Dialog<Utente> dialog = new Dialog<>();

    private final TextField txtNome = new TextField();
    private final TextField txtCognome = new TextField();
    private final TextField txtEmail = new TextField();
    private final PasswordField txtPassword = new PasswordField();
    private final TextField txtPatente = new TextField();

    private final Utente base;

    public UtenteDialog(Utente u) {
        this.base = u;

        dialog.setTitle(u == null ? "Nuovo Utente" : "Modifica Utente");
        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK, ButtonType.CANCEL
        );

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Nome"), 0, 0);
        grid.add(txtNome, 1, 0);
        grid.add(new Label("Cognome"), 0, 1);
        grid.add(txtCognome, 1, 1);
        grid.add(new Label("Email"), 0, 2);
        grid.add(txtEmail, 1, 2);
        grid.add(new Label("Password"), 0, 3);
        grid.add(txtPassword, 1, 3);
        grid.add(new Label("Patente"), 0, 4);
        grid.add(txtPatente, 1, 4);

        if (u != null) {
            txtNome.setText(u.getNome());
            txtCognome.setText(u.getCognome());
            txtEmail.setText(u.getEmail());
            txtPatente.setText(u.getPatente());
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                return new Utente(
                        base != null ? base.getIdUtente() : 0,
                        txtNome.getText(),
                        txtCognome.getText(),
                        txtEmail.getText(),
                        txtPassword.getText(),
                        RuoloUtente.DRIVER,
                        txtPatente.getText()
                );
            }
            return null;
        });
    }

    public Optional<Utente> mostra() {
        return dialog.showAndWait();
    }
}
