package com.autobots.automanager.modelos;

import java.util.Collection;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.MercadoriaControle;
import com.autobots.automanager.entidades.Mercadoria;

@Component
public class AdicionadorLinkMercadoria implements AdicionadorLink<Mercadoria> {

	@Override
	public void adicionarLink(List<Mercadoria> lista) {
		if (lista == null) {
			return;
		}
		for (Mercadoria m : lista) {
			adicionarLink(m);
		}
	}

	public void adicionarLink(Collection<Mercadoria> colecao) {
		if (colecao == null) {
			return;
		}
		for (Mercadoria m : colecao) {
			adicionarLink(m);
		}
	}

	@Override
	public void adicionarLink(Mercadoria objeto) {
		if (objeto == null || objeto.getId() == null) {
			return;
		}
		long id = objeto.getId();
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(MercadoriaControle.class)
						.obterMercadoria(id))
				.withSelfRel();
		objeto.add(linkProprio);

		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(MercadoriaControle.class)
						.obterMercadorias())
				.withRel("mercadorias");
		objeto.add(linkLista);
	}
}

