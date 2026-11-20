package com.autobots.automanager.modelos;

import java.util.Collection;

import com.autobots.automanager.entidades.Empresa;

public class EmpresaAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();
	private EnderecoAtualizador enderecoAtualizador = new EnderecoAtualizador();
	private TelefoneAtualizador telefoneAtualizador = new TelefoneAtualizador();

	public void atualizar(Empresa empresa, Empresa atualizacao) {
		if (atualizacao != null && empresa != null) {
			if (!verificador.verificar(atualizacao.getRazaoSocial())) {
				empresa.setRazaoSocial(atualizacao.getRazaoSocial());
			}
			if (!verificador.verificar(atualizacao.getNomeFantasia())) {
				empresa.setNomeFantasia(atualizacao.getNomeFantasia());
			}
			if (atualizacao.getEndereco() != null) {
				if (empresa.getEndereco() == null) {
					empresa.setEndereco(atualizacao.getEndereco());
				} else {
					enderecoAtualizador.atualizar(empresa.getEndereco(), atualizacao.getEndereco());
				}
			}
			if (atualizacao.getTelefones() != null) {
				telefoneAtualizador.atualizar(empresa.getTelefones(), atualizacao.getTelefones());
			}
		}
	}

	public void atualizar(Collection<Empresa> empresas, Collection<Empresa> atualizacoes) {
		if (empresas == null || atualizacoes == null) {
			return;
		}
		for (Empresa atualizacao : atualizacoes) {
			for (Empresa empresa : empresas) {
				if (atualizacao.getId() != null && empresa.getId() != null) {
					if (atualizacao.getId().equals(empresa.getId())) {
						atualizar(empresa, atualizacao);
					}
				}
			}
		}
	}
}

