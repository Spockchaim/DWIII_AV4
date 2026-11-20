package com.autobots.automanager.modelos;

import java.util.Collection;

import com.autobots.automanager.entidades.Telefone;

public class TelefoneAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Telefone telefone, Telefone atualizacao) {
		if (atualizacao != null && telefone != null) {
			if (!verificador.verificar(atualizacao.getDdd())) {
				telefone.setDdd(atualizacao.getDdd());
			}
			if (!verificador.verificar(atualizacao.getNumero())) {
				telefone.setNumero(atualizacao.getNumero());
			}
		}
	}

	public void atualizar(Collection<Telefone> telefones, Collection<Telefone> atualizacoes) {
		if (telefones == null || atualizacoes == null) {
			return;
		}
		for (Telefone atualizacao : atualizacoes) {
			for (Telefone telefone : telefones) {
				if (atualizacao.getId() != null && telefone.getId() != null) {
					if (atualizacao.getId().equals(telefone.getId())) {
						atualizar(telefone, atualizacao);
					}
				}
			}
		}
	}
}

