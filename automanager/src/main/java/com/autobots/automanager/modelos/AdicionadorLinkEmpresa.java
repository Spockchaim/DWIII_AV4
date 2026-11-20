package com.autobots.automanager.modelos;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EmpresaControle;
import com.autobots.automanager.entidades.Empresa;

@Component
public class AdicionadorLinkEmpresa implements AdicionadorLink<Empresa> {

	@Autowired
	private AdicionadorLinkEndereco adicionadorLinkEndereco;

	@Autowired
	private AdicionadorLinkTelefone adicionadorLinkTelefone;

	@Autowired
	private AdicionadorLinkUsuario adicionadorLinkUsuario;

	@Autowired
	private AdicionadorLinkMercadoria adicionadorLinkMercadoria;

	@Autowired
	private AdicionadorLinkServico adicionadorLinkServico;

	@Autowired
	private AdicionadorLinkVenda adicionadorLinkVenda;

	@Override
	public void adicionarLink(List<Empresa> lista) {
		if (lista == null) {
			return;
		}
		for (Empresa emp : lista) {
			adicionarLink(emp);
		}
	}

	public void adicionarLink(Collection<Empresa> colecao) {
		if (colecao == null) {
			return;
		}
		for (Empresa emp : colecao) {
			adicionarLink(emp);
		}
	}

	@Override
	public void adicionarLink(Empresa objeto) {
		if (objeto == null || objeto.getId() == null) {
			return;
		}
		long id = objeto.getId();
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(EmpresaControle.class)
						.obterEmpresa(id))
				.withSelfRel();
		objeto.add(linkProprio);

		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(EmpresaControle.class)
						.obterEmpresas())
				.withRel("empresas");
		objeto.add(linkLista);

		if (objeto.getEndereco() != null) {
			adicionadorLinkEndereco.adicionarLink(objeto.getEndereco());
		}
		if (objeto.getTelefones() != null) {
			adicionadorLinkTelefone.adicionarLink(objeto.getTelefones());
		}
		if (objeto.getUsuarios() != null) {
			adicionadorLinkUsuario.adicionarLink(objeto.getUsuarios());
		}
		if (objeto.getMercadorias() != null) {
			adicionadorLinkMercadoria.adicionarLink(objeto.getMercadorias());
		}
		if (objeto.getServicos() != null) {
			adicionadorLinkServico.adicionarLink(objeto.getServicos());
		}
		if (objeto.getVendas() != null) {
			adicionadorLinkVenda.adicionarLink(objeto.getVendas());
		}
	}
}

