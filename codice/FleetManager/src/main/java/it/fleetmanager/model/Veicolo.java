package it.fleetmanager.model;

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

	public Veicolo(String targa, TipoVeicolo tipoVeicolo, String marca, String modello, int annoImmatricolazione,
			StatoVeicolo statoVeicolo, int km) {
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

	@Override
	public String toString() {
		return "Veicolo {" + "targa='" + targa + '\'' + ", tipoVeicolo=" + tipoVeicolo + ", marca='" + marca + '\''
				+ ", modello='" + modello + '\'' + ", annoImmatricolazione=" + annoImmatricolazione + ", statoVeicolo="
				+ statoVeicolo + ", km=" + km + '}';
	}

}
