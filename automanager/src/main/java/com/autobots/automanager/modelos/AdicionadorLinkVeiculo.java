package com.autobots.automanager.modelos;

import java.util.Collection;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.VeiculoControle;
import com.autobots.automanager.entidades.Veiculo;

@Component
public class AdicionadorLinkVeiculo implements AdicionadorLink<Veiculo> {

	@Override
	public void adicionarLink(List<Veiculo> lista) {
		if (lista == null) {
			return;
		}
		for (Veiculo v : lista) {
			adicionarLink(v);
		}
	}

	public void adicionarLink(Collection<Veiculo> colecao) {
		if (colecao == null) {
			return;
		}
		for (Veiculo v : colecao) {
			adicionarLink(v);
		}
	}

	@Override
	public void adicionarLink(Veiculo objeto) {
		if (objeto == null || objeto.getId() == null) {
			return;
		}
		long id = objeto.getId();
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(VeiculoControle.class)
						.obterVeiculo(id))
				.withSelfRel();
		objeto.add(linkProprio);

		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(VeiculoControle.class)
						.obterVeiculos())
				.withRel("veiculos");
		objeto.add(linkLista);
	}
}

