package it.fleetmanager.ui;

import it.fleetmanager.model.Utente;

/**
 * Interfaccia per i controller JavaFX che necessitano
 * di ricevere l'utente autenticato corrente.
 *
 * <p>
 * I controller che implementano questa interfaccia possono
 * essere riconosciuti automaticamente dal {@link SceneManager}
 * durante il cambio di scena.
 * </p>
 */

@FunctionalInterface
public interface UserAwareController {

    /**
     * Imposta l'utente corrente nel controller.
     *
     * @param utente utente autenticato
     */
    void setUtente(Utente utente);
}
