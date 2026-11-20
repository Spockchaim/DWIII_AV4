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

import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.modelos.AdicionadorLinkEndereco;
import com.autobots.automanager.modelos.EnderecoAtualizador;
import com.autobots.automanager.repositorios.EnderecoRepositorio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/endereco")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
@Tag(name = "Endereços", description = "Endpoints para gerenciamento de endereços")
public class EnderecoControle {

	@Autowired
	private EnderecoRepositorio repositorio;

	@Autowired
	private AdicionadorLinkEndereco adicionadorLink;

	@Operation(summary = "Obter endereço por ID")
	@GetMapping("/{id}")
	public ResponseEntity<Endereco> obterEndereco(@PathVariable long id) {
		Endereco endereco = repositorio.findById(id).orElse(null);
		if (endereco == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(endereco);
		return new ResponseEntity<>(endereco, HttpStatus.OK);
	}

	@Operation(summary = "Listar todos os endereços")
	@GetMapping({ "/enderecos", "" })
	public ResponseEntity<List<Endereco>> obterEnderecos() {
		List<Endereco> enderecos = repositorio.findAll();
		if (enderecos.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(enderecos);
		return new ResponseEntity<>(enderecos, HttpStatus.OK);
	}

	@Operation(summary = "Cadastrar novo endereço")
	@PostMapping("/cadastro")
	public ResponseEntity<?> cadastrarEndereco(@RequestBody Endereco endereco) {
		if (endereco == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		if (endereco.getId() == null) {
			repositorio.save(endereco);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.CONFLICT);
	}

	@Operation(summary = "Atualizar endereço")
	@PutMapping("/atualizar")
	public ResponseEntity<?> atualizarEndereco(@RequestBody Endereco atualizacao) {
		if (atualizacao == null || atualizacao.getId() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Endereco endereco = repositorio.findById(atualizacao.getId()).orElse(null);
		if (endereco != null) {
			EnderecoAtualizador atualizador = new EnderecoAtualizador();
			atualizador.atualizar(endereco, atualizacao);
			repositorio.save(endereco);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@Operation(summary = "Excluir endereço por ID")
	@DeleteMapping("/excluir/{id}")
	public ResponseEntity<?> excluirEndereco(@PathVariable long id) {
		Endereco endereco = repositorio.findById(id).orElse(null);
		if (endereco != null) {
			repositorio.delete(endereco);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}

