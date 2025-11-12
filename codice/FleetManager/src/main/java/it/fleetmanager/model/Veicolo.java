package it.fleetmanager.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import it.fleetmanager.util.StatoVeicolo;
import it.fleetmanager.util.TipoVeicolo;

public class Veicolo {

	private String targa;
	private TipoVeicolo tipoVeicolo;
	private String marca;
	private String modello;
	private int annoImmatricolazione;
	private StatoVeicolo statoVeicolo;
	private int km;

	@JsonCreator
	public Veicolo(@JsonProperty("targa") String targa, @JsonProperty("tipoVeicolo") TipoVeicolo tipoVeicolo,
			@JsonProperty("marca") String marca, @JsonProperty("modello") String modello,
			@JsonProperty("annoImmatricolazione") int annoImmatricolazione,
			@JsonProperty("statoVeicolo") StatoVeicolo statoVeicolo, @JsonProperty("km") int km) {
		this.targa = targa;
		this.tipoVeicolo = tipoVeicolo;
		this.marca = marca;
		this.modello = modello;
		this.annoImmatricolazione = annoImmatricolazione;
		this.statoVeicolo = statoVeicolo;
		this.km = km;
	}

	public String getTarga() {
		return targa;
	}

	public TipoVeicolo getTipoVeicolo() {
		return tipoVeicolo;
	}

	public String getMarca() {
		return marca;
	}

	public String getModello() {
		return modello;
	}

	public int getAnnoImmatricolazione() {
		return annoImmatricolazione;
	}

	public StatoVeicolo getStatoVeicolo() {
		return statoVeicolo;
	}

	public void setStatoVeicolo(StatoVeicolo statoVeicolo) {
		this.statoVeicolo = statoVeicolo;
	}

	public int getKm() {
		return km;
	}

	public void setKm(int km) {
		this.km = km;
	}
}
