package com.autobots.automanager.controles;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.modelos.AdicionadorLinkCredencial;
import com.autobots.automanager.modelos.CredencialAtualizador;
import com.autobots.automanager.repositorios.CredencialRepositorio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/credencial")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Credenciais", description = "Endpoints para gerenciamento de credenciais (apenas ADMIN)")
public class CredencialControle {

	@Autowired
	private CredencialRepositorio repositorio;

	@Autowired
	private AdicionadorLinkCredencial adicionadorLink;

	@Operation(summary = "Obter credencial por ID")
	@GetMapping("/{id}")
	public ResponseEntity<Credencial> obterCredencial(@PathVariable long id) {
		Credencial credencial = repositorio.findById(id).orElse(null);
		if (credencial == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(credencial);
		return new ResponseEntity<>(credencial, HttpStatus.OK);
	}

	@Operation(summary = "Listar todas as credenciais")
	@GetMapping({ "/credenciais", "" })
	public ResponseEntity<List<Credencial>> obterCredenciais() {
		List<Credencial> credenciais = repositorio.findAll();
		if (credenciais.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(credenciais);
		return new ResponseEntity<>(credenciais, HttpStatus.OK);
	}

	@Operation(summary = "Cadastrar nova credencial")
	@PostMapping("/cadastro")
	public ResponseEntity<?> cadastrarCredencial(@RequestBody Credencial credencial) {
		if (credencial == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		if (credencial.getId() == null) {
			repositorio.save(credencial);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.CONFLICT);
	}

	@Operation(summary = "Atualizar credencial")
	@PutMapping("/atualizar")
	public ResponseEntity<?> atualizarCredencial(@RequestBody Credencial atualizacao) {
		if (atualizacao == null || atualizacao.getId() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Credencial credencial = repositorio.findById(atualizacao.getId()).orElse(null);
		if (credencial != null) {
			CredencialAtualizador atualizador = new CredencialAtualizador();
			atualizador.atualizar(credencial, atualizacao);
			repositorio.save(credencial);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@Operation(summary = "Excluir credencial por ID")
	@DeleteMapping("/excluir/{id}")
	public ResponseEntity<?> excluirCredencial(@PathVariable long id) {
		Credencial credencial = repositorio.findById(id).orElse(null);
		if (credencial != null) {
			repositorio.delete(credencial);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}

