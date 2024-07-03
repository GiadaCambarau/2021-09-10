package it.polito.tdp.yelp.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.yelp.db.YelpDao;

public class Model {
	private YelpDao dao;
	private List<String> cities;
	private Graph<Business, DefaultWeightedEdge> grafo;
	private double max;
	private List<Business> best;
	private int nMax;
	
	public Model() {
		this.dao = new YelpDao();
		this.cities = dao.getCities();
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
	}
	
	public List<String> getCities(){
		return this.cities;
	}
	
	public void creaGrafo(String c) {
		List<Business> business = dao.getBusiness(c);
		Graphs.addAllVertices(this.grafo, business);
		for (Business b1: this.grafo.vertexSet()) {
			for (Business b2: this.grafo.vertexSet()) {
				if (!b1.equals(b2)) {
					LatLng coordinate1 = new LatLng(b1.getLatitude(), b1.getLongitude());
					LatLng coordinate2 = new LatLng(b2.getLatitude(), b2.getLongitude());
					double peso = LatLngTool.distance(coordinate1, coordinate2, LengthUnit.KILOMETER);
					Graphs.addEdgeWithVertices(this.grafo, b1, b2, peso);
				}
			}
		}
	}
	
	public int getV() {
		return this.grafo.vertexSet().size();
		
	}
	public int getA() {
		return this.grafo.edgeSet().size();
	}
	
	public Business trovaLontano(Business b1) {
		Business b = null;
		this.max=0;
		List<Business> adiacenti = Graphs.neighborListOf(this.grafo, b1);
		for (Business b2: adiacenti) {
			double peso = this.grafo.getEdgeWeight(this.grafo.getEdge(b1, b2));
			if (peso>= max) {
				max = peso;
			}
		}
		for (Business b2: adiacenti) {
			double peso = this.grafo.getEdgeWeight(this.grafo.getEdge(b1, b2));
			if (peso== max) {
				b = b2;
			}
		}
		return b;
	}
	
	public Set<Business> getVertici(){
		return this.grafo.vertexSet();
	}
	public double getDistanza() {
		return this.max;
	}
	
	public List<Business> trovaPercorso(Business partenza, Business arrivo, double soglia){
		this.best = new ArrayList<>();
		this.nMax =0;
		List<Business> parziale = new ArrayList<>();
		parziale.add(partenza);
		ricorsione(partenza, arrivo, soglia, parziale);
		return best;
	}

	private void ricorsione(Business partenza, Business arrivo, double soglia, List<Business> parziale) {
		//uscita
		Business corrente = parziale.get(parziale.size()-1);
		if (corrente.equals(arrivo)) {
			if (parziale.size()>=nMax) {
				this.best = new ArrayList<>(parziale);
				this.nMax = parziale.size();
			}
			return;
		}
		
		//condizione normale 
		List<Business> vicini = Graphs.neighborListOf(this.grafo, corrente);
		for (Business b: vicini) {
			if ((b.getStars()>soglia && !parziale.contains(b)) || b.equals(arrivo)) {
				parziale.add(b);
				ricorsione(partenza, arrivo, soglia, parziale);
				parziale.remove(parziale.size()-1);
			}
		}
		
		
		
	}
	
	
}
