package com.autobots.automanager.modelos;

import java.util.Collection;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.CredencialCodigoBarra;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;

public class CredencialAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();
	private BCryptPasswordEncoder codificador = new BCryptPasswordEncoder();

	public void atualizar(Credencial credencial, Credencial atualizacao) {
		if (atualizacao != null && credencial != null) {
			if (atualizacao.getUltimoAcesso() != null) {
				credencial.setUltimoAcesso(atualizacao.getUltimoAcesso());
			}
			credencial.setInativo(atualizacao.isInativo());

			if (credencial instanceof CredencialUsuarioSenha && atualizacao instanceof CredencialUsuarioSenha) {
				CredencialUsuarioSenha credencialSenha = (CredencialUsuarioSenha) credencial;
				CredencialUsuarioSenha atualizacaoSenha = (CredencialUsuarioSenha) atualizacao;
				if (!verificador.verificar(atualizacaoSenha.getNomeUsuario())) {
					credencialSenha.setNomeUsuario(atualizacaoSenha.getNomeUsuario());
				}
				if (!verificador.verificar(atualizacaoSenha.getSenha())) {
					String senha = atualizacaoSenha.getSenha();
					if (!senha.startsWith("$2a$") && !senha.startsWith("$2b$")) {
						senha = codificador.encode(senha);
					}
					credencialSenha.setSenha(senha);
				}
			} else if (credencial instanceof CredencialCodigoBarra && atualizacao instanceof CredencialCodigoBarra) {
				CredencialCodigoBarra credencialBarra = (CredencialCodigoBarra) credencial;
				CredencialCodigoBarra atualizacaoBarra = (CredencialCodigoBarra) atualizacao;
				if (atualizacaoBarra.getCodigo() != 0) {
					credencialBarra.setCodigo(atualizacaoBarra.getCodigo());
				}
			}
		}
	}

	public void atualizar(Collection<Credencial> credenciais, Collection<Credencial> atualizacoes) {
		if (credenciais == null || atualizacoes == null) {
			return;
		}
		for (Credencial atualizacao : atualizacoes) {
			for (Credencial credencial : credenciais) {
				if (atualizacao.getId() != null && credencial.getId() != null) {
					if (atualizacao.getId().equals(credencial.getId())) {
						atualizar(credencial, atualizacao);
					}
				}
			}
		}
	}
}
