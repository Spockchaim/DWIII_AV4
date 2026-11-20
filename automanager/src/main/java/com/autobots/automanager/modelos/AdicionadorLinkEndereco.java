package com.autobots.automanager.modelos;

import java.util.Collection;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EnderecoControle;
import com.autobots.automanager.entidades.Endereco;

@Component
public class AdicionadorLinkEndereco implements AdicionadorLink<Endereco> {

	@Override
	public void adicionarLink(List<Endereco> lista) {
		if (lista == null) {
			return;
		}
		for (Endereco end : lista) {
			adicionarLink(end);
		}
	}

	public void adicionarLink(Collection<Endereco> colecao) {
		if (colecao == null) {
			return;
		}
		for (Endereco end : colecao) {
			adicionarLink(end);
		}
	}

	@Override
	public void adicionarLink(Endereco objeto) {
		if (objeto == null || objeto.getId() == null) {
			return;
		}
		long id = objeto.getId();
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(EnderecoControle.class)
						.obterEndereco(id))
				.withSelfRel();
		objeto.add(linkProprio);

		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(EnderecoControle.class)
						.obterEnderecos())
				.withRel("enderecos");
		objeto.add(linkLista);
	}
}

