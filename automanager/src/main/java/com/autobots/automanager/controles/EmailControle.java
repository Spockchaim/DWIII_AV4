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

import com.autobots.automanager.entidades.Email;
import com.autobots.automanager.modelos.AdicionadorLinkEmail;
import com.autobots.automanager.modelos.EmailAtualizador;
import com.autobots.automanager.repositorios.EmailRepositorio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/email")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
@Tag(name = "Emails", description = "Endpoints para gerenciamento de emails")
public class EmailControle {

	@Autowired
	private EmailRepositorio repositorio;

	@Autowired
	private AdicionadorLinkEmail adicionadorLink;

	@Operation(summary = "Obter email por ID")
	@GetMapping("/{id}")
	public ResponseEntity<Email> obterEmail(@PathVariable long id) {
		Email email = repositorio.findById(id).orElse(null);
		if (email == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(email);
		return new ResponseEntity<>(email, HttpStatus.OK);
	}

	@Operation(summary = "Listar todos os emails")
	@GetMapping({ "/emails", "" })
	public ResponseEntity<List<Email>> obterEmails() {
		List<Email> emails = repositorio.findAll();
		if (emails.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(emails);
		return new ResponseEntity<>(emails, HttpStatus.OK);
	}

	@Operation(summary = "Cadastrar novo email")
	@PostMapping("/cadastro")
	public ResponseEntity<?> cadastrarEmail(@RequestBody Email email) {
		if (email == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		if (email.getId() == null) {
			repositorio.save(email);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.CONFLICT);
	}

	@Operation(summary = "Atualizar email")
	@PutMapping("/atualizar")
	public ResponseEntity<?> atualizarEmail(@RequestBody Email atualizacao) {
		if (atualizacao == null || atualizacao.getId() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Email email = repositorio.findById(atualizacao.getId()).orElse(null);
		if (email != null) {
			EmailAtualizador atualizador = new EmailAtualizador();
			atualizador.atualizar(email, atualizacao);
			repositorio.save(email);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@Operation(summary = "Excluir email por ID")
	@DeleteMapping("/excluir/{id}")
	public ResponseEntity<?> excluirEmail(@PathVariable long id) {
		Email email = repositorio.findById(id).orElse(null);
		if (email != null) {
			repositorio.delete(email);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}

