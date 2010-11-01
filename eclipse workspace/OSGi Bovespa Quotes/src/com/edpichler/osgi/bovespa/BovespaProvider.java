package com.edpichler.osgi.bovespa;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Classe que retorna um objeto para comunica��o com os servidores da Bovespa.
 * 
 * */
public class BovespaProvider implements IBovespaQuoteRetriever {
	private final static String BOVESPA_QUOTE_URL = "http://www.bmfbovespa.com.br/Pregao-Online/ExecutaAcaoAjax.asp";

	public static IBovespaQuoteRetriever createQuoteRetriever() {
		return new BovespaProvider();
	}

	@Override
	public List<Cotacao> getQuotes(String[] codigoAtivo) throws Exception {
		List<Cotacao> lista = new ArrayList<Cotacao>();
		for (int i = 0; i < codigoAtivo.length; i++) {
			Cotacao quote = getQuote(codigoAtivo[i]);
			lista.add(quote);
		}
		return lista;
	}

	public Cotacao getQuote(String codigoAtivo) throws Exception {
		String quoteUrlRequest = BOVESPA_QUOTE_URL + "?CodigoPapel="
				+ codigoAtivo;

		URL feedUrl;
		SAXBuilder builder = new SAXBuilder();
		Document doc;

		try {
			feedUrl = new URL(quoteUrlRequest);
			doc = builder.build(feedUrl);

			// parse
			List<Element> elements = doc.getRootElement().getChildren("Papel");
			if (elements.size() > 0) {
				Element papel = elements.get(0);

				// <Papel Codigo="OGXP3" Nome="OGX PETROLEO ON NM"
				// Ibovespa="#" Data="27/10/2010 13:24:47" Abertura="21,95"
				// Minimo="21,65"
				// Maximo="21,95" Medio="21,76" Ultimo="21,73" Oscilacao="-1,18"
				String ultimo = papel.getAttribute("Ultimo").getValue();
				String oscilacao = papel.getAttribute("Oscilacao").getValue();
				String medio = papel.getAttribute("Medio").getValue();
				String maximo = papel.getAttribute("Maximo").getValue();
				String minimo = papel.getAttribute("Minimo").getValue();
				String abertura = papel.getAttribute("Abertura").getValue();
				String data = papel.getAttribute("Data").getValue();
				String nome = papel.getAttribute("Nome").getValue();
				String codigo = papel.getAttribute("Codigo").getValue();

				Cotacao cot = createCotacao(ultimo, oscilacao, medio, maximo,
						minimo, abertura, data, nome, codigo);

				return cot;
			} else {
				return null;
			}
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Cria um objeto cota��o a partir dos parametros
	 * 
	 * @throws ParseException
	 * */
	private Cotacao createCotacao(String ultimo, String oscilacao,
			String medio, String maximo, String minimo, String abertura,
			String data, String nome, String codigo) throws ParseException {

		Ativo at = new Ativo();
		at.setCodigo(codigo);
		at.setNome(nome);

		Cotacao cot = new Cotacao(at);
		// Data="27/10/2010 13:24:47" e com hora zerada vem assim
		// "29/10/201000:00:00"
		String padrao = getPattern(data);
		Date _dt = new SimpleDateFormat(padrao)
				.parse(data);

		cot.setData(_dt);
		cot.setAbertura(getDouble(abertura));
		cot.setMaximo(getDouble(maximo.replace(",", ".")));
		cot.setMinimo(getDouble(minimo.replace(",", ".")));
		cot.setMedio(getDouble(medio.replace(",", ".")));
		cot.setOscilacao(getDouble(oscilacao.replace(",", ".")));
		cot.setUltimo(getDouble(ultimo.replace(",", ".")));	
		
		if(cot.getUltimo() == 0){
			return null;
		}
		return cot;
	}

	private double getDouble(String valor) {
		valor = valor.replaceAll(",", ".");
		if(valor.length() == 0){
			return 0;
		}
		return Double.parseDouble(valor);
	}

	private String getPattern(String example) {		
		if(example.contains(" ")){
			return "dd/MM/yyyy" + " " + "HH:mm:ss";
		}else{
			return "dd/MM/yyyyHH:mm:ss";
		}
		 
		
	}
}