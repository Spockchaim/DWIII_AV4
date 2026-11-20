package com.autobots.automanager.controles;

import java.util.Date;
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

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.modelos.AdicionadorLinkEmpresa;
import com.autobots.automanager.modelos.EmpresaAtualizador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/empresa")
@Tag(name = "Empresas", description = "Endpoints para gerenciamento de unidades comerciais de oficinas e autopeças")
public class EmpresaControle {

	@Autowired
	private EmpresaRepositorio repositorio;

	@Autowired
	private AdicionadorLinkEmpresa adicionadorLink;

	@Operation(summary = "Obter empresa por ID")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@GetMapping("/{id}")
	public ResponseEntity<Empresa> obterEmpresa(@PathVariable long id) {
		Empresa empresa = repositorio.findById(id).orElse(null);
		if (empresa == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(empresa);
		return new ResponseEntity<>(empresa, HttpStatus.OK);
	}

	@Operation(summary = "Listar todas as empresas")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@GetMapping({ "/empresas", "" })
	public ResponseEntity<List<Empresa>> obterEmpresas() {
		List<Empresa> empresas = repositorio.findAll();
		if (empresas.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(empresas);
		return new ResponseEntity<>(empresas, HttpStatus.OK);
	}

	@Operation(summary = "Cadastrar nova empresa")
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/cadastro")
	public ResponseEntity<?> cadastrarEmpresa(@RequestBody Empresa empresa) {
		if (empresa == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		if (empresa.getId() == null) {
			if (empresa.getCadastro() == null) {
				empresa.setCadastro(new Date());
			}
			Empresa salva = repositorio.save(empresa);
			adicionadorLink.adicionarLink(salva);
			return new ResponseEntity<>(salva, HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.CONFLICT);
	}

	@Operation(summary = "Atualizar empresa")
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/atualizar")
	public ResponseEntity<?> atualizarEmpresa(@RequestBody Empresa atualizacao) {
		if (atualizacao == null || atualizacao.getId() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Empresa empresa = repositorio.findById(atualizacao.getId()).orElse(null);
		if (empresa != null) {
			EmpresaAtualizador atualizador = new EmpresaAtualizador();
			atualizador.atualizar(empresa, atualizacao);
			repositorio.save(empresa);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@Operation(summary = "Excluir empresa por ID")
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/excluir/{id}")
	public ResponseEntity<?> excluirEmpresa(@PathVariable long id) {
		Empresa empresa = repositorio.findById(id).orElse(null);
		if (empresa != null) {
			repositorio.delete(empresa);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}

