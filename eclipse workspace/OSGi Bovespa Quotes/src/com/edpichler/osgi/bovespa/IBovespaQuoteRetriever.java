package com.edpichler.osgi.bovespa;

import java.util.List;

import com.edpichler.osgi.bovespa.impl.Quote;


/**Classe para comunica��o com a BMF Bovespa*/
public interface IBovespaQuoteRetriever {
	/**
	 * Retorna a cota��o do ativo.
	 * @throws Exception 
	 * */
	public List<Quote> getQuotes(String[] codigoAtivo) throws Exception;
	public Quote getQuote(String codigoAtivo) throws Exception;
}
