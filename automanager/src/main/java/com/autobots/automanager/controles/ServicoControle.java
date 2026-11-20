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

import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.modelos.AdicionadorLinkServico;
import com.autobots.automanager.modelos.ServicoAtualizador;
import com.autobots.automanager.repositorios.ServicoRepositorio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/servico")
@Tag(name = "Serviços", description = "Endpoints para gerenciamento do catálogo de serviços de oficina (JWT)")
public class ServicoControle {

	@Autowired
	private ServicoRepositorio repositorio;

	@Autowired
	private AdicionadorLinkServico adicionadorLink;

	@Operation(summary = "Obter serviço por ID")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@GetMapping("/{id}")
	public ResponseEntity<Servico> obterServico(@PathVariable long id) {
		Servico servico = repositorio.findById(id).orElse(null);
		if (servico == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(servico);
		return new ResponseEntity<>(servico, HttpStatus.OK);
	}

	@Operation(summary = "Listar todos os serviços")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@GetMapping({ "/servicos", "" })
	public ResponseEntity<List<Servico>> obterServicos() {
		List<Servico> servicos = repositorio.findAll();
		if (servicos.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(servicos);
		return new ResponseEntity<>(servicos, HttpStatus.OK);
	}

	@Operation(summary = "Cadastrar novo serviço")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
	@PostMapping("/cadastro")
	public ResponseEntity<?> cadastrarServico(@RequestBody Servico servico) {
		if (servico == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		if (servico.getId() == null) {
			repositorio.save(servico);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.CONFLICT);
	}

	@Operation(summary = "Atualizar serviço")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
	@PutMapping("/atualizar")
	public ResponseEntity<?> atualizarServico(@RequestBody Servico atualizacao) {
		if (atualizacao == null || atualizacao.getId() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Servico servico = repositorio.findById(atualizacao.getId()).orElse(null);
		if (servico != null) {
			ServicoAtualizador atualizador = new ServicoAtualizador();
			atualizador.atualizar(servico, atualizacao);
			repositorio.save(servico);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@Operation(summary = "Excluir serviço por ID")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
	@DeleteMapping("/excluir/{id}")
	public ResponseEntity<?> excluirServico(@PathVariable long id) {
		Servico servico = repositorio.findById(id).orElse(null);
		if (servico != null) {
			repositorio.delete(servico);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}
