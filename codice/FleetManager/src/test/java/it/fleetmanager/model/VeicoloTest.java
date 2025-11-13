package it.fleetmanager.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import it.fleetmanager.util.StatoVeicolo;
import it.fleetmanager.util.TipoVeicolo;

class VeicoloTest {

    @Test
    void shouldCreateVeicoloWithCorrectData() {
    	
        Veicolo veicolo = new Veicolo(
                "FZ320YT",
                TipoVeicolo.AUTO,          
                "Fiat",
                "Panda",
                2020,
                StatoVeicolo.DISPONIBILE,
                45000
        );

        assertEquals("FZ320YT", veicolo.getTarga());
        assertEquals(TipoVeicolo.AUTO, veicolo.getTipoVeicolo());
        assertEquals("Fiat", veicolo.getMarca());
        assertEquals("Panda", veicolo.getModello());
        assertEquals(2020, veicolo.getAnnoImmatricolazione());
        assertEquals(StatoVeicolo.DISPONIBILE, veicolo.getStatoVeicolo());
        assertEquals(45000, veicolo.getKm());
    }

    @Test
    void shouldChangeVehicleStatus() {
        Veicolo veicolo = new Veicolo(
                "FZ320YT",
                TipoVeicolo.AUTO,
                "Fiat",
                "Panda",
                2020,
                StatoVeicolo.DISPONIBILE,
                45000
        );

        veicolo.setStatoVeicolo(StatoVeicolo.IN_MANUTENZIONE);

        assertEquals(StatoVeicolo.IN_MANUTENZIONE, veicolo.getStatoVeicolo());
    }

    @Test
    void shouldBeAvailableWhenStatusIsDisponibile() {
        Veicolo veicolo = new Veicolo(
                "FZ320YT",
                TipoVeicolo.AUTO,
                "Fiat",
                "Panda",
                2020,
                StatoVeicolo.DISPONIBILE,
                45000
        );

        assertEquals(StatoVeicolo.DISPONIBILE, veicolo.getStatoVeicolo());
    }

    @Test
    void shouldNotBeAvailableWhenStatusIsInManutenzione() {
        Veicolo veicolo = new Veicolo(
                "FZ320YT",
                TipoVeicolo.AUTO,
                "Fiat",
                "Panda",
                2020,
                StatoVeicolo.IN_MANUTENZIONE,
                45000
        );
        assertEquals(StatoVeicolo.IN_MANUTENZIONE, veicolo.getStatoVeicolo());
        
    }
}
