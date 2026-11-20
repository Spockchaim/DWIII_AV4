package com.autobots.automanager.modelos;

import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.adaptadores.UserDetailsImpl;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;

@Component
public class SegurancaUtil {

	public Usuario getUsuarioLogado() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl) {
			return ((UserDetailsImpl) auth.getPrincipal()).getUsuario();
		}
		return null;
	}

	public boolean temPerfil(Usuario usuario, PerfilUsuario perfil) {
		return usuario != null && usuario.getPerfis() != null && usuario.getPerfis().contains(perfil);
	}

	public boolean isAdmin(Usuario usuario) {
		return temPerfil(usuario, PerfilUsuario.ROLE_ADMIN);
	}

	public boolean isGerente(Usuario usuario) {
		return temPerfil(usuario, PerfilUsuario.ROLE_GERENTE);
	}

	public boolean isVendedor(Usuario usuario) {
		return temPerfil(usuario, PerfilUsuario.ROLE_VENDEDOR);
	}

	public boolean isCliente(Usuario usuario) {
		return temPerfil(usuario, PerfilUsuario.ROLE_CLIENTE);
	}

	/**
	 * Verifica se o usuário autenticado tem permissão de CRUD sobre o usuário alvo
	 * conforme a Tabela 1 da ATVIV:
	 * - ADMIN: qualquer usuário (incluindo outros ADMINs)
	 * - GERENTE: GERENTE, VENDEDOR e CLIENTE (não pode manipular ADMIN)
	 * - VENDEDOR: apenas CLIENTE (não pode manipular ADMIN, GERENTE ou VENDEDOR)
	 */
	public boolean podeGerenciarUsuario(Usuario logado, Usuario alvo) {
		if (logado == null || alvo == null) {
			return false;
		}
		if (isAdmin(logado)) {
			return true;
		}
		Set<PerfilUsuario> perfisAlvo = alvo.getPerfis();
		if (isGerente(logado)) {
			return perfisAlvo == null || !perfisAlvo.contains(PerfilUsuario.ROLE_ADMIN);
		}
		if (isVendedor(logado)) {
			if (perfisAlvo == null || perfisAlvo.isEmpty()) {
				return false;
			}
			return perfisAlvo.contains(PerfilUsuario.ROLE_CLIENTE)
					&& !perfisAlvo.contains(PerfilUsuario.ROLE_ADMIN)
					&& !perfisAlvo.contains(PerfilUsuario.ROLE_GERENTE)
					&& !perfisAlvo.contains(PerfilUsuario.ROLE_VENDEDOR);
		}
		return false;
	}
}
