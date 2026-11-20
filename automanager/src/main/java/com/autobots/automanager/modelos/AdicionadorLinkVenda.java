package com.autobots.automanager.modelos;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.VendaControle;
import com.autobots.automanager.entidades.Venda;

@Component
public class AdicionadorLinkVenda implements AdicionadorLink<Venda> {

	@Autowired
	private AdicionadorLinkMercadoria adicionadorLinkMercadoria;

	@Autowired
	private AdicionadorLinkServico adicionadorLinkServico;

	@Autowired
	private AdicionadorLinkVeiculo adicionadorLinkVeiculo;

	@Override
	public void adicionarLink(List<Venda> lista) {
		if (lista == null) {
			return;
		}
		for (Venda v : lista) {
			adicionarLink(v);
		}
	}

	public void adicionarLink(Collection<Venda> colecao) {
		if (colecao == null) {
			return;
		}
		for (Venda v : colecao) {
			adicionarLink(v);
		}
	}

	@Override
	public void adicionarLink(Venda objeto) {
		if (objeto == null || objeto.getId() == null) {
			return;
		}
		long id = objeto.getId();
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(VendaControle.class)
						.obterVenda(id))
				.withSelfRel();
		objeto.add(linkProprio);

		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(VendaControle.class)
						.obterVendas())
				.withRel("vendas");
		objeto.add(linkLista);

		if (objeto.getVeiculo() != null) {
			adicionadorLinkVeiculo.adicionarLink(objeto.getVeiculo());
		}
		if (objeto.getMercadorias() != null) {
			adicionadorLinkMercadoria.adicionarLink(objeto.getMercadorias());
		}
		if (objeto.getServicos() != null) {
			adicionadorLinkServico.adicionarLink(objeto.getServicos());
		}
	}
}

