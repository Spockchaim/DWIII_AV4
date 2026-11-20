package com.autobots.automanager.adaptadores;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;

@SuppressWarnings("serial")
public class UserDetailsImpl implements UserDetails {
	private Usuario usuario;
	private CredencialUsuarioSenha credencial;

	public UserDetailsImpl(Usuario usuario, CredencialUsuarioSenha credencial) {
		this.usuario = usuario;
		this.credencial = credencial;
	}

	public Usuario getUsuario() {
		return this.usuario;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<SimpleGrantedAuthority> autoridades = new ArrayList<>();
		for (PerfilUsuario perfil : usuario.getPerfis()) {
			autoridades.add(new SimpleGrantedAuthority(perfil.name()));
		}
		return autoridades;
	}

	@Override
	public String getPassword() {
		return credencial != null ? credencial.getSenha() : "";
	}

	@Override
	public String getUsername() {
		return credencial != null ? credencial.getNomeUsuario() : "";
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		if (credencial == null) {
			return false;
		}
		for (Credencial c : usuario.getCredenciais()) {
			if (c instanceof CredencialUsuarioSenha
					&& ((CredencialUsuarioSenha) c).getNomeUsuario().equals(credencial.getNomeUsuario())) {
				return !c.isInativo();
			}
		}
		return !credencial.isInativo();
	}
}
