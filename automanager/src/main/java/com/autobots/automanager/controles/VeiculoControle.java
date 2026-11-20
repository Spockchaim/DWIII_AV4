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

import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.modelos.AdicionadorLinkVeiculo;
import com.autobots.automanager.modelos.VeiculoAtualizador;
import com.autobots.automanager.repositorios.VeiculoRepositorio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/veiculo")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
@Tag(name = "Veículos", description = "Endpoints para gerenciamento de veículos")
public class VeiculoControle {

	@Autowired
	private VeiculoRepositorio repositorio;

	@Autowired
	private AdicionadorLinkVeiculo adicionadorLink;

	@Operation(summary = "Obter veículo por ID")
	@GetMapping("/{id}")
	public ResponseEntity<Veiculo> obterVeiculo(@PathVariable long id) {
		Veiculo veiculo = repositorio.findById(id).orElse(null);
		if (veiculo == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(veiculo);
		return new ResponseEntity<>(veiculo, HttpStatus.OK);
	}

	@Operation(summary = "Listar todos os veículos")
	@GetMapping({ "/veiculos", "" })
	public ResponseEntity<List<Veiculo>> obterVeiculos() {
		List<Veiculo> veiculos = repositorio.findAll();
		if (veiculos.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(veiculos);
		return new ResponseEntity<>(veiculos, HttpStatus.OK);
	}

	@Operation(summary = "Cadastrar novo veículo")
	@PostMapping("/cadastro")
	public ResponseEntity<?> cadastrarVeiculo(@RequestBody Veiculo veiculo) {
		if (veiculo == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		if (veiculo.getId() == null) {
			repositorio.save(veiculo);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.CONFLICT);
	}

	@Operation(summary = "Atualizar veículo")
	@PutMapping("/atualizar")
	public ResponseEntity<?> atualizarVeiculo(@RequestBody Veiculo atualizacao) {
		if (atualizacao == null || atualizacao.getId() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Veiculo veiculo = repositorio.findById(atualizacao.getId()).orElse(null);
		if (veiculo != null) {
			VeiculoAtualizador atualizador = new VeiculoAtualizador();
			atualizador.atualizar(veiculo, atualizacao);
			repositorio.save(veiculo);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@Operation(summary = "Excluir veículo por ID")
	@DeleteMapping("/excluir/{id}")
	public ResponseEntity<?> excluirVeiculo(@PathVariable long id) {
		Veiculo veiculo = repositorio.findById(id).orElse(null);
		if (veiculo != null) {
			repositorio.delete(veiculo);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}

