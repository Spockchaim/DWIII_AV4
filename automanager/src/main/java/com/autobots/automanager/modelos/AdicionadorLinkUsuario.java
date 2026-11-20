package com.autobots.automanager.modelos;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.UsuarioControle;
import com.autobots.automanager.entidades.Usuario;

@Component
public class AdicionadorLinkUsuario implements AdicionadorLink<Usuario> {

	@Autowired
	private AdicionadorLinkEndereco adicionadorLinkEndereco;

	@Autowired
	private AdicionadorLinkTelefone adicionadorLinkTelefone;

	@Autowired
	private AdicionadorLinkDocumento adicionadorLinkDocumento;

	@Autowired
	private AdicionadorLinkEmail adicionadorLinkEmail;

	@Autowired
	private AdicionadorLinkCredencial adicionadorLinkCredencial;

	@Autowired
	private AdicionadorLinkVeiculo adicionadorLinkVeiculo;

	@Override
	public void adicionarLink(List<Usuario> lista) {
		if (lista == null) {
			return;
		}
		for (Usuario u : lista) {
			adicionarLink(u);
		}
	}

	public void adicionarLink(Collection<Usuario> colecao) {
		if (colecao == null) {
			return;
		}
		for (Usuario u : colecao) {
			adicionarLink(u);
		}
	}

	@Override
	public void adicionarLink(Usuario objeto) {
		if (objeto == null || objeto.getId() == null) {
			return;
		}
		long id = objeto.getId();
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(UsuarioControle.class)
						.obterUsuario(id))
				.withSelfRel();
		objeto.add(linkProprio);

		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(UsuarioControle.class)
						.obterUsuarios())
				.withRel("usuarios");
		objeto.add(linkLista);

		if (objeto.getEndereco() != null) {
			adicionadorLinkEndereco.adicionarLink(objeto.getEndereco());
		}
		if (objeto.getTelefones() != null) {
			adicionadorLinkTelefone.adicionarLink(objeto.getTelefones());
		}
		if (objeto.getDocumentos() != null) {
			adicionadorLinkDocumento.adicionarLink(objeto.getDocumentos());
		}
		if (objeto.getEmails() != null) {
			adicionadorLinkEmail.adicionarLink(objeto.getEmails());
		}
		if (objeto.getCredenciais() != null) {
			adicionadorLinkCredencial.adicionarLink(objeto.getCredenciais());
		}
		if (objeto.getVeiculos() != null) {
			adicionadorLinkVeiculo.adicionarLink(objeto.getVeiculos());
		}
	}
}

