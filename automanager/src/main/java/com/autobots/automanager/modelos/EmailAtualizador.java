package com.autobots.automanager.modelos;

import java.util.Collection;

import com.autobots.automanager.entidades.Email;

public class EmailAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Email email, Email atualizacao) {
		if (atualizacao != null && email != null) {
			if (!verificador.verificar(atualizacao.getEndereco())) {
				email.setEndereco(atualizacao.getEndereco());
			}
		}
	}

	public void atualizar(Collection<Email> emails, Collection<Email> atualizacoes) {
		if (emails == null || atualizacoes == null) {
			return;
		}
		for (Email atualizacao : atualizacoes) {
			for (Email email : emails) {
				if (atualizacao.getId() != null && email.getId() != null) {
					if (atualizacao.getId().equals(email.getId())) {
						atualizar(email, atualizacao);
					}
				}
			}
		}
	}
}

