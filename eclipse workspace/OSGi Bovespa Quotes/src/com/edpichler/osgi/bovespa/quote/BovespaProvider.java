package com.edpichler.osgi.bovespa.quote;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.edpichler.osgi.bovespa.quote.impl.Quote;

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
	public List<IQuote> getQuotes(String[] codigoAtivo) throws Exception {
		List<IQuote> lista = new ArrayList<IQuote>();
		for (int i = 0; i < codigoAtivo.length; i++) {
			IQuote quote = getQuote(codigoAtivo[i]);
			if (quote != null) {
				//only valid quotes.
				lista.add(quote);
			}
		}
		return lista;
	}

	public IQuote getQuote(String codigoAtivo) throws Exception {
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

				Quote cot = createCotacao(ultimo, oscilacao, medio, maximo,
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
	private Quote createCotacao(String ultimo, String oscilacao, String medio,
			String maximo, String minimo, String abertura, String data,
			String nome, String codigo) throws ParseException {

		Quote cot = new Quote();
		// Data="27/10/2010 13:24:47"
		Date _dt = getData(data);
		cot.setCodigo(codigo);
		cot.setNome(nome);
		cot.setData(_dt);
		cot.setAbertura(getDouble(abertura));
		cot.setMaximo(getDouble(maximo));
		cot.setMinimo(getDouble(minimo));
		cot.setMedio(getDouble(medio));
		cot.setOscilacao(getDouble(oscilacao));		
		cot.setUltimo(getDouble(ultimo));

		return cot;
	}

	private double getDouble(String valor) {
		if(valor != null && valor.length() > 0 ){
			try{
			 return Double.parseDouble(valor.replace(",", "."));
			}catch(NumberFormatException ex){
				//if Bovespa send some bad string, I need to handle it.
				return 0;
			}
		}else{
			return 0;	
		}
		
	}

	private Date getData(String data) throws ParseException {

		String pattern = null;
		if (data.contains(" ")) {
			pattern = "dd/MM/yyyy HH:mm:ss";
		} else {
			pattern = "dd/MM/yyyyHH:mm:ss";
		}

		Date temp = new SimpleDateFormat(pattern).parse(data);
		Calendar instance = Calendar.getInstance();
		instance.setTime(temp);
		instance.setTimeZone(TimeZone.getTimeZone("GMT-3:00"));
		return instance.getTime();

	}
	// public static void main(String[] args) {
	// Date data;
	// try {
	// data = new BovespaProvider().getData("10/01/2011 17:36:56");
	// System.out.println(data.toString());
	// } catch (ParseException e) {
	//
	// e.printStackTrace();
	// }
	//
	// }
}