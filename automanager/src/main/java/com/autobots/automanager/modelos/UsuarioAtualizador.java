package com.autobots.automanager.modelos;

import java.util.Collection;

import com.autobots.automanager.entidades.Usuario;

public class UsuarioAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();
	private EnderecoAtualizador enderecoAtualizador = new EnderecoAtualizador();
	private TelefoneAtualizador telefoneAtualizador = new TelefoneAtualizador();
	private DocumentoAtualizador documentoAtualizador = new DocumentoAtualizador();
	private EmailAtualizador emailAtualizador = new EmailAtualizador();
	private CredencialAtualizador credencialAtualizador = new CredencialAtualizador();
	private VeiculoAtualizador veiculoAtualizador = new VeiculoAtualizador();

	public void atualizar(Usuario usuario, Usuario atualizacao) {
		if (atualizacao != null && usuario != null) {
			if (!verificador.verificar(atualizacao.getNome())) {
				usuario.setNome(atualizacao.getNome());
			}
			if (!verificador.verificar(atualizacao.getNomeSocial())) {
				usuario.setNomeSocial(atualizacao.getNomeSocial());
			}
			if (atualizacao.getPerfis() != null && !atualizacao.getPerfis().isEmpty()) {
				usuario.getPerfis().clear();
				usuario.getPerfis().addAll(atualizacao.getPerfis());
			}
			if (atualizacao.getEndereco() != null) {
				if (usuario.getEndereco() == null) {
					usuario.setEndereco(atualizacao.getEndereco());
				} else {
					enderecoAtualizador.atualizar(usuario.getEndereco(), atualizacao.getEndereco());
				}
			}
			if (atualizacao.getTelefones() != null) {
				telefoneAtualizador.atualizar(usuario.getTelefones(), atualizacao.getTelefones());
			}
			if (atualizacao.getDocumentos() != null) {
				documentoAtualizador.atualizar(usuario.getDocumentos(), atualizacao.getDocumentos());
			}
			if (atualizacao.getEmails() != null) {
				emailAtualizador.atualizar(usuario.getEmails(), atualizacao.getEmails());
			}
			if (atualizacao.getCredenciais() != null) {
				credencialAtualizador.atualizar(usuario.getCredenciais(), atualizacao.getCredenciais());
			}
			if (atualizacao.getVeiculos() != null) {
				veiculoAtualizador.atualizar(usuario.getVeiculos(), atualizacao.getVeiculos());
			}
		}
	}

	public void atualizar(Collection<Usuario> usuarios, Collection<Usuario> atualizacoes) {
		if (usuarios == null || atualizacoes == null) {
			return;
		}
		for (Usuario atualizacao : atualizacoes) {
			for (Usuario usuario : usuarios) {
				if (atualizacao.getId() != null && usuario.getId() != null) {
					if (atualizacao.getId().equals(usuario.getId())) {
						atualizar(usuario, atualizacao);
					}
				}
			}
		}
	}
}

