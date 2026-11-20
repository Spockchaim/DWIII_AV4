package com.autobots.automanager.modelos;

import java.util.Collection;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.DocumentoControle;
import com.autobots.automanager.entidades.Documento;

@Component
public class AdicionadorLinkDocumento implements AdicionadorLink<Documento> {

	@Override
	public void adicionarLink(java.util.List<Documento> lista) {
		if (lista == null) {
			return;
		}
		for (Documento doc : lista) {
			adicionarLink(doc);
		}
	}

	public void adicionarLink(Collection<Documento> colecao) {
		if (colecao == null) {
			return;
		}
		for (Documento doc : colecao) {
			adicionarLink(doc);
		}
	}

	@Override
	public void adicionarLink(Documento objeto) {
		if (objeto == null || objeto.getId() == null) {
			return;
		}
		long id = objeto.getId();
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(DocumentoControle.class)
						.obterDocumento(id))
				.withSelfRel();
		objeto.add(linkProprio);

		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(DocumentoControle.class)
						.obterDocumentos())
				.withRel("documentos");
		objeto.add(linkLista);
	}
}

