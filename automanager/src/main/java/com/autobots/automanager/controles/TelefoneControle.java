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

import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.modelos.AdicionadorLinkTelefone;
import com.autobots.automanager.modelos.TelefoneAtualizador;
import com.autobots.automanager.repositorios.TelefoneRepositorio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/telefone")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
@Tag(name = "Telefones", description = "Endpoints para gerenciamento de telefones")
public class TelefoneControle {

	@Autowired
	private TelefoneRepositorio repositorio;

	@Autowired
	private AdicionadorLinkTelefone adicionadorLink;

	@Operation(summary = "Obter telefone por ID")
	@GetMapping("/{id}")
	public ResponseEntity<Telefone> obterTelefone(@PathVariable long id) {
		Telefone telefone = repositorio.findById(id).orElse(null);
		if (telefone == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(telefone);
		return new ResponseEntity<>(telefone, HttpStatus.OK);
	}

	@Operation(summary = "Listar todos os telefones")
	@GetMapping({ "/telefones", "" })
	public ResponseEntity<List<Telefone>> obterTelefones() {
		List<Telefone> telefones = repositorio.findAll();
		if (telefones.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(telefones);
		return new ResponseEntity<>(telefones, HttpStatus.OK);
	}

	@Operation(summary = "Cadastrar novo telefone")
	@PostMapping("/cadastro")
	public ResponseEntity<?> cadastrarTelefone(@RequestBody Telefone telefone) {
		if (telefone == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		if (telefone.getId() == null) {
			repositorio.save(telefone);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.CONFLICT);
	}

	@Operation(summary = "Atualizar telefone")
	@PutMapping("/atualizar")
	public ResponseEntity<?> atualizarTelefone(@RequestBody Telefone atualizacao) {
		if (atualizacao == null || atualizacao.getId() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Telefone telefone = repositorio.findById(atualizacao.getId()).orElse(null);
		if (telefone != null) {
			TelefoneAtualizador atualizador = new TelefoneAtualizador();
			atualizador.atualizar(telefone, atualizacao);
			repositorio.save(telefone);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@Operation(summary = "Excluir telefone por ID")
	@DeleteMapping("/excluir/{id}")
	public ResponseEntity<?> excluirTelefone(@PathVariable long id) {
		Telefone telefone = repositorio.findById(id).orElse(null);
		if (telefone != null) {
			repositorio.delete(telefone);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}

