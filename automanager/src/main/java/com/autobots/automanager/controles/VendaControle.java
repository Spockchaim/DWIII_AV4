package com.autobots.automanager.controles;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.modelos.AdicionadorLinkVenda;
import com.autobots.automanager.modelos.SegurancaUtil;
import com.autobots.automanager.modelos.VendaAtualizador;
import com.autobots.automanager.repositorios.MercadoriaRepositorio;
import com.autobots.automanager.repositorios.ServicoRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.VeiculoRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/venda")
@Tag(name = "Vendas", description = "Endpoints para gerenciamento de vendas e ordens de serviço prestadas (JWT)")
public class VendaControle {

	@Autowired
	private VendaRepositorio repositorio;

	@Autowired
	private UsuarioRepositorio usuarioRepositorio;

	@Autowired
	private VeiculoRepositorio veiculoRepositorio;

	@Autowired
	private MercadoriaRepositorio mercadoriaRepositorio;

	@Autowired
	private ServicoRepositorio servicoRepositorio;

	@Autowired
	private AdicionadorLinkVenda adicionadorLink;

	@Autowired
	private SegurancaUtil segurancaUtil;

	@Operation(summary = "Obter venda por ID")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping("/{id}")
	public ResponseEntity<Venda> obterVenda(@PathVariable long id) {
		Venda venda = repositorio.findById(id).orElse(null);
		if (venda == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Usuario logado = segurancaUtil.getUsuarioLogado();
		if (!podeLerVenda(logado, venda)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		adicionadorLink.adicionarLink(venda);
		return new ResponseEntity<>(venda, HttpStatus.OK);
	}

	@Operation(summary = "Listar vendas permitidas conforme o perfil autenticado")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping({ "/vendas", "" })
	public ResponseEntity<List<Venda>> obterVendas() {
		Usuario logado = segurancaUtil.getUsuarioLogado();
		List<Venda> vendas = repositorio.findAll().stream()
				.filter(v -> podeLerVenda(logado, v))
				.collect(Collectors.toList());

		if (vendas.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(vendas);
		return new ResponseEntity<>(vendas, HttpStatus.OK);
	}

	@Operation(summary = "Cadastrar nova venda")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@PostMapping("/cadastro")
	public ResponseEntity<?> cadastrarVenda(@RequestBody Venda venda) {
		if (venda == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Usuario logado = segurancaUtil.getUsuarioLogado();

		// Vendedor só pode criar vendas feitas por si mesmo
		if (!segurancaUtil.isAdmin(logado) && !segurancaUtil.isGerente(logado) && segurancaUtil.isVendedor(logado)) {
			if (venda.getFuncionario() != null && venda.getFuncionario().getId() != null
					&& !venda.getFuncionario().getId().equals(logado.getId())) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			venda.setFuncionario(usuarioRepositorio.findById(logado.getId()).orElse(null));
		} else if (venda.getFuncionario() != null && venda.getFuncionario().getId() != null) {
			venda.setFuncionario(usuarioRepositorio.findById(venda.getFuncionario().getId()).orElse(null));
		}

		resolverReferencias(venda);

		if (venda.getId() == null) {
			if (venda.getCadastro() == null) {
				venda.setCadastro(new Date());
			}
			Venda salva = repositorio.save(venda);
			adicionadorLink.adicionarLink(salva);
			return new ResponseEntity<>(salva, HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.CONFLICT);
	}

	@Operation(summary = "Atualizar venda")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
	@PutMapping("/atualizar")
	public ResponseEntity<?> atualizarVenda(@RequestBody Venda atualizacao) {
		if (atualizacao == null || atualizacao.getId() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Venda venda = repositorio.findById(atualizacao.getId()).orElse(null);
		if (venda != null) {
			if (atualizacao.getFuncionario() != null && atualizacao.getFuncionario().getId() != null) {
				atualizacao.setFuncionario(usuarioRepositorio.findById(atualizacao.getFuncionario().getId()).orElse(null));
			}
			resolverReferencias(atualizacao);
			VendaAtualizador atualizador = new VendaAtualizador();
			atualizador.atualizar(venda, atualizacao);
			Venda salva = repositorio.save(venda);
			adicionadorLink.adicionarLink(salva);
			return new ResponseEntity<>(salva, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@Operation(summary = "Excluir venda por ID")
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
	@DeleteMapping("/excluir/{id}")
	public ResponseEntity<?> excluirVenda(@PathVariable long id) {
		Venda venda = repositorio.findById(id).orElse(null);
		if (venda != null) {
			repositorio.delete(venda);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	private void resolverReferencias(Venda venda) {
		if (venda.getCliente() != null && venda.getCliente().getId() != null) {
			venda.setCliente(usuarioRepositorio.findById(venda.getCliente().getId()).orElse(null));
		}
		if (venda.getVeiculo() != null && venda.getVeiculo().getId() != null) {
			Veiculo v = veiculoRepositorio.findById(venda.getVeiculo().getId()).orElse(null);
			venda.setVeiculo(v);
		}
		if (venda.getMercadorias() != null && !venda.getMercadorias().isEmpty()) {
			Set<Mercadoria> resolvidas = new HashSet<>();
			for (Mercadoria m : venda.getMercadorias()) {
				if (m.getId() != null) {
					mercadoriaRepositorio.findById(m.getId()).ifPresent(resolvidas::add);
				} else {
					resolvidas.add(m);
				}
			}
			venda.setMercadorias(resolvidas);
		}
		if (venda.getServicos() != null && !venda.getServicos().isEmpty()) {
			Set<Servico> resolvidos = new HashSet<>();
			for (Servico s : venda.getServicos()) {
				if (s.getId() != null) {
					servicoRepositorio.findById(s.getId()).ifPresent(resolvidos::add);
				} else {
					resolvidos.add(s);
				}
			}
			venda.setServicos(resolvidos);
		}
	}

	private boolean podeLerVenda(Usuario logado, Venda venda) {
		if (logado == null || venda == null) {
			return false;
		}
		if (segurancaUtil.isAdmin(logado) || segurancaUtil.isGerente(logado)) {
			return true;
		}
		if (segurancaUtil.isVendedor(logado)) {
			return venda.getFuncionario() != null && logado.getId().equals(venda.getFuncionario().getId());
		}
		if (segurancaUtil.isCliente(logado)) {
			return venda.getCliente() != null && logado.getId().equals(venda.getCliente().getId());
		}
		return false;
	}
}
