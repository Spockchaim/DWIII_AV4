package com.autobots.automanager.modelos;

import java.util.Collection;

import com.autobots.automanager.entidades.Mercadoria;

public class MercadoriaAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Mercadoria mercadoria, Mercadoria atualizacao) {
		if (atualizacao != null && mercadoria != null) {
			if (!verificador.verificar(atualizacao.getNome())) {
				mercadoria.setNome(atualizacao.getNome());
			}
			if (!verificador.verificar(atualizacao.getDescricao())) {
				mercadoria.setDescricao(atualizacao.getDescricao());
			}
			if (atualizacao.getValor() > 0) {
				mercadoria.setValor(atualizacao.getValor());
			}
			if (atualizacao.getQuantidade() >= 0) {
				mercadoria.setQuantidade(atualizacao.getQuantidade());
			}
			if (atualizacao.getValidade() != null) {
				mercadoria.setValidade(atualizacao.getValidade());
			}
			if (atualizacao.getFabricao() != null) {
				mercadoria.setFabricao(atualizacao.getFabricao());
			}
		}
	}

	public void atualizar(Collection<Mercadoria> mercadorias, Collection<Mercadoria> atualizacoes) {
		if (mercadorias == null || atualizacoes == null) {
			return;
		}
		for (Mercadoria atualizacao : atualizacoes) {
			for (Mercadoria mercadoria : mercadorias) {
				if (atualizacao.getId() != null && mercadoria.getId() != null) {
					if (atualizacao.getId().equals(mercadoria.getId())) {
						atualizar(mercadoria, atualizacao);
					}
				}
			}
		}
	}
}

