package com.autobots.automanager.controles;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelos.AdicionadorLinkUsuario;
import com.autobots.automanager.modelos.SegurancaUtil;
import com.autobots.automanager.modelos.UsuarioAtualizador;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/usuario")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários com controle de acesso por perfil (JWT)")
public class UsuarioControle {

	@Autowired
	private UsuarioRepositorio repositorio;

	@Autowired
	private AdicionadorLinkUsuario adicionadorLink;

	@Autowired
	private BCryptPasswordEncoder codificador;

	@Autowired
	private SegurancaUtil segurancaUtil;

	@Operation(summary = "Obter dados do usuário autenticado")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping("/me")
	public ResponseEntity<Usuario> obterProprioCadastro() {
		Usuario logado = segurancaUtil.getUsuarioLogado();
		if (logado == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		Usuario usuario = repositorio.findById(logado.getId()).orElse(null);
		if (usuario == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(usuario);
		return new ResponseEntity<>(usuario, HttpStatus.OK);
	}

	@Operation(summary = "Obter usuário por ID")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping("/{id}")
	public ResponseEntity<Usuario> obterUsuario(@PathVariable long id) {
		Usuario logado = segurancaUtil.getUsuarioLogado();
		Usuario usuario = repositorio.findById(id).orElse(null);
		if (usuario == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		boolean ehProprioUsuario = logado != null && logado.getId().equals(usuario.getId());
		if (!ehProprioUsuario && !segurancaUtil.podeGerenciarUsuario(logado, usuario)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		adicionadorLink.adicionarLink(usuario);
		return new ResponseEntity<>(usuario, HttpStatus.OK);
	}

	@Operation(summary = "Listar usuários permitidos conforme o perfil autenticado")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping({ "/usuarios", "" })
	public ResponseEntity<List<Usuario>> obterUsuarios() {
		Usuario logado = segurancaUtil.getUsuarioLogado();
		List<Usuario> todos = repositorio.findAll();

		List<Usuario> permitidos = todos.stream()
				.filter(u -> (logado != null && logado.getId().equals(u.getId()))
						|| segurancaUtil.podeGerenciarUsuario(logado, u))
				.collect(Collectors.toList());

		if (permitidos.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(permitidos);
		return new ResponseEntity<>(permitidos, HttpStatus.OK);
	}

	@Operation(summary = "Cadastrar novo usuário")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@PostMapping("/cadastro")
	public ResponseEntity<?> cadastrarUsuario(@RequestBody Usuario usuario) {
		if (usuario == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Usuario logado = segurancaUtil.getUsuarioLogado();
		if (!segurancaUtil.podeGerenciarUsuario(logado, usuario)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		if (usuario.getId() == null) {
			codificarCredenciais(usuario);
			Usuario salvo = repositorio.save(usuario);
			adicionadorLink.adicionarLink(salvo);
			return new ResponseEntity<>(salvo, HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.CONFLICT);
	}

	@Operation(summary = "Atualizar usuário")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@PutMapping("/atualizar")
	public ResponseEntity<?> atualizarUsuario(@RequestBody Usuario atualizacao) {
		if (atualizacao == null || atualizacao.getId() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Usuario logado = segurancaUtil.getUsuarioLogado();
		Usuario usuario = repositorio.findById(atualizacao.getId()).orElse(null);
		if (usuario == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!segurancaUtil.podeGerenciarUsuario(logado, usuario)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		if (atualizacao.getPerfis() != null && !atualizacao.getPerfis().isEmpty()
				&& !segurancaUtil.podeGerenciarUsuario(logado, atualizacao)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		UsuarioAtualizador atualizador = new UsuarioAtualizador();
		atualizador.atualizar(usuario, atualizacao);
		repositorio.save(usuario);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Operation(summary = "Excluir usuário por ID")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@DeleteMapping("/excluir/{id}")
	public ResponseEntity<?> excluirUsuario(@PathVariable long id) {
		Usuario logado = segurancaUtil.getUsuarioLogado();
		Usuario usuario = repositorio.findById(id).orElse(null);
		if (usuario == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!segurancaUtil.podeGerenciarUsuario(logado, usuario)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		repositorio.delete(usuario);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private void codificarCredenciais(Usuario usuario) {
		if (usuario.getCredenciais() != null) {
			for (Credencial cred : usuario.getCredenciais()) {
				if (cred.getCriacao() == null) {
					cred.setCriacao(new Date());
				}
				if (cred instanceof CredencialUsuarioSenha) {
					CredencialUsuarioSenha credSenha = (CredencialUsuarioSenha) cred;
					if (credSenha.getSenha() != null
							&& !credSenha.getSenha().startsWith("$2a$")
							&& !credSenha.getSenha().startsWith("$2b$")) {
						credSenha.setSenha(codificador.encode(credSenha.getSenha()));
					}
				}
			}
		}
	}
}
