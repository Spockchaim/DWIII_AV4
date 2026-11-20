package com.autobots.automanager.modelos;

import java.util.Collection;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EmailControle;
import com.autobots.automanager.entidades.Email;

@Component
public class AdicionadorLinkEmail implements AdicionadorLink<Email> {

	@Override
	public void adicionarLink(List<Email> lista) {
		if (lista == null) {
			return;
		}
		for (Email em : lista) {
			adicionarLink(em);
		}
	}

	public void adicionarLink(Collection<Email> colecao) {
		if (colecao == null) {
			return;
		}
		for (Email em : colecao) {
			adicionarLink(em);
		}
	}

	@Override
	public void adicionarLink(Email objeto) {
		if (objeto == null || objeto.getId() == null) {
			return;
		}
		long id = objeto.getId();
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(EmailControle.class)
						.obterEmail(id))
				.withSelfRel();
		objeto.add(linkProprio);

		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(EmailControle.class)
						.obterEmails())
				.withRel("emails");
		objeto.add(linkLista);
	}
}

