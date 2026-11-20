package com.autobots.automanager.modelos;

import java.util.Collection;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.ServicoControle;
import com.autobots.automanager.entidades.Servico;

@Component
public class AdicionadorLinkServico implements AdicionadorLink<Servico> {

	@Override
	public void adicionarLink(List<Servico> lista) {
		if (lista == null) {
			return;
		}
		for (Servico s : lista) {
			adicionarLink(s);
		}
	}

	public void adicionarLink(Collection<Servico> colecao) {
		if (colecao == null) {
			return;
		}
		for (Servico s : colecao) {
			adicionarLink(s);
		}
	}

	@Override
	public void adicionarLink(Servico objeto) {
		if (objeto == null || objeto.getId() == null) {
			return;
		}
		long id = objeto.getId();
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(ServicoControle.class)
						.obterServico(id))
				.withSelfRel();
		objeto.add(linkProprio);

		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(ServicoControle.class)
						.obterServicos())
				.withRel("servicos");
		objeto.add(linkLista);
	}
}

