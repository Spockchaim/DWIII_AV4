package com.autobots.automanager.modelos;

import java.util.Collection;

import com.autobots.automanager.entidades.Veiculo;

public class VeiculoAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Veiculo veiculo, Veiculo atualizacao) {
		if (atualizacao != null && veiculo != null) {
			if (atualizacao.getTipo() != null) {
				veiculo.setTipo(atualizacao.getTipo());
			}
			if (!verificador.verificar(atualizacao.getModelo())) {
				veiculo.setModelo(atualizacao.getModelo());
			}
			if (!verificador.verificar(atualizacao.getPlaca())) {
				veiculo.setPlaca(atualizacao.getPlaca());
			}
		}
	}

	public void atualizar(Collection<Veiculo> veiculos, Collection<Veiculo> atualizacoes) {
		if (veiculos == null || atualizacoes == null) {
			return;
		}
		for (Veiculo atualizacao : atualizacoes) {
			for (Veiculo veiculo : veiculos) {
				if (atualizacao.getId() != null && veiculo.getId() != null) {
					if (atualizacao.getId().equals(veiculo.getId())) {
						atualizar(veiculo, atualizacao);
					}
				}
			}
		}
	}
}

