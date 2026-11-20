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

import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.modelos.AdicionadorLinkMercadoria;
import com.autobots.automanager.modelos.MercadoriaAtualizador;
import com.autobots.automanager.repositorios.MercadoriaRepositorio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/mercadoria")
@Tag(name = "Mercadorias", description = "Endpoints para gerenciamento de estoque de autopeças e mercadorias (JWT)")
public class MercadoriaControle {

	@Autowired
	private MercadoriaRepositorio repositorio;

	@Autowired
	private AdicionadorLinkMercadoria adicionadorLink;

	@Operation(summary = "Obter mercadoria por ID")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@GetMapping("/{id}")
	public ResponseEntity<Mercadoria> obterMercadoria(@PathVariable long id) {
		Mercadoria mercadoria = repositorio.findById(id).orElse(null);
		if (mercadoria == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(mercadoria);
		return new ResponseEntity<>(mercadoria, HttpStatus.OK);
	}

	@Operation(summary = "Listar todas as mercadorias")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@GetMapping({ "/mercadorias", "" })
	public ResponseEntity<List<Mercadoria>> obterMercadorias() {
		List<Mercadoria> mercadorias = repositorio.findAll();
		if (mercadorias.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(mercadorias);
		return new ResponseEntity<>(mercadorias, HttpStatus.OK);
	}

	@Operation(summary = "Cadastrar nova mercadoria")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
	@PostMapping("/cadastro")
	public ResponseEntity<?> cadastrarMercadoria(@RequestBody Mercadoria mercadoria) {
		if (mercadoria == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		if (mercadoria.getId() == null) {
			if (mercadoria.getCadastro() == null) {
				mercadoria.setCadastro(new java.util.Date());
			}
			Mercadoria salva = repositorio.save(mercadoria);
			adicionadorLink.adicionarLink(salva);
			return new ResponseEntity<>(salva, HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.CONFLICT);
	}

	@Operation(summary = "Atualizar mercadoria")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
	@PutMapping("/atualizar")
	public ResponseEntity<?> atualizarMercadoria(@RequestBody Mercadoria atualizacao) {
		if (atualizacao == null || atualizacao.getId() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Mercadoria mercadoria = repositorio.findById(atualizacao.getId()).orElse(null);
		if (mercadoria != null) {
			MercadoriaAtualizador atualizador = new MercadoriaAtualizador();
			atualizador.atualizar(mercadoria, atualizacao);
			repositorio.save(mercadoria);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@Operation(summary = "Excluir mercadoria por ID")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
	@DeleteMapping("/excluir/{id}")
	public ResponseEntity<?> excluirMercadoria(@PathVariable long id) {
		Mercadoria mercadoria = repositorio.findById(id).orElse(null);
		if (mercadoria != null) {
			repositorio.delete(mercadoria);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}
