package com.autobots.automanager.modelos;

import java.util.Collection;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.CredencialControle;
import com.autobots.automanager.entidades.Credencial;

@Component
public class AdicionadorLinkCredencial implements AdicionadorLink<Credencial> {

	@Override
	public void adicionarLink(List<Credencial> lista) {
		if (lista == null) {
			return;
		}
		for (Credencial cr : lista) {
			adicionarLink(cr);
		}
	}

	public void adicionarLink(Collection<Credencial> colecao) {
		if (colecao == null) {
			return;
		}
		for (Credencial cr : colecao) {
			adicionarLink(cr);
		}
	}

	@Override
	public void adicionarLink(Credencial objeto) {
		if (objeto == null || objeto.getId() == null) {
			return;
		}
		long id = objeto.getId();
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(CredencialControle.class)
						.obterCredencial(id))
				.withSelfRel();
		objeto.add(linkProprio);

		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(CredencialControle.class)
						.obterCredenciais())
				.withRel("credenciais");
		objeto.add(linkLista);
	}
}

