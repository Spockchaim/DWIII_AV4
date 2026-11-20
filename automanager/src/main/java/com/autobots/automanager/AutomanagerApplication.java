package com.autobots.automanager;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Email;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.enumeracoes.TipoDocumento;
import com.autobots.automanager.enumeracoes.TipoVeiculo;
import com.autobots.automanager.repositorios.RepositorioEmpresa;

@SpringBootApplication
public class AutomanagerApplication implements CommandLineRunner {

	@Autowired
	private RepositorioEmpresa repositorioEmpresa;

	@Autowired
	private BCryptPasswordEncoder codificador;

	public static void main(String[] args) {
		SpringApplication.run(AutomanagerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if (repositorioEmpresa.count() > 0) {
			return;
		}

		Empresa empresa = new Empresa();
		empresa.setRazaoSocial("Car service toyota ltda");
		empresa.setNomeFantasia("Car service manutenção veicular");
		empresa.setCadastro(new Date());

		Endereco enderecoEmpresa = new Endereco();
		enderecoEmpresa.setEstado("São Paulo");
		enderecoEmpresa.setCidade("São Paulo");
		enderecoEmpresa.setBairro("Centro");
		enderecoEmpresa.setRua("Av. São João");
		enderecoEmpresa.setNumero("100");
		enderecoEmpresa.setCodigoPostal("01035-000");
		empresa.setEndereco(enderecoEmpresa);

		Telefone telefoneEmpresa = new Telefone();
		telefoneEmpresa.setDdd("011");
		telefoneEmpresa.setNumero("986454527");
		empresa.getTelefones().add(telefoneEmpresa);

		// 1. Administrador
		Usuario admin = new Usuario();
		admin.setNome("Administrador AutoBots");
		admin.setNomeSocial("Admin");
		admin.getPerfis().add(PerfilUsuario.ROLE_ADMIN);
		admin.getCredenciais().add(criarCredencial("admin", "123456"));
		Email emailAdmin = new Email();
		emailAdmin.setEndereco("admin@autobots.com");
		admin.getEmails().add(emailAdmin);
		empresa.getUsuarios().add(admin);

		// 2. Gerente
		Usuario gerente = new Usuario();
		gerente.setNome("Carlos Gerente Toyota");
		gerente.setNomeSocial("Carlos");
		gerente.getPerfis().add(PerfilUsuario.ROLE_GERENTE);
		gerente.getCredenciais().add(criarCredencial("gerente", "123456"));
		Email emailGerente = new Email();
		emailGerente.setEndereco("gerente@autobots.com");
		gerente.getEmails().add(emailGerente);
		empresa.getUsuarios().add(gerente);

		// 3. Vendedor (Funcionário)
		Usuario vendedor = new Usuario();
		vendedor.setNome("Pedro Alcântara de Bragança e Bourbon");
		vendedor.setNomeSocial("Dom Pedro Vendedor");
		vendedor.getPerfis().add(PerfilUsuario.ROLE_VENDEDOR);
		vendedor.getCredenciais().add(criarCredencial("vendedor", "123456"));

		Email emailVendedor = new Email();
		emailVendedor.setEndereco("vendedor@autobots.com");
		vendedor.getEmails().add(emailVendedor);

		Documento cpfVendedor = new Documento();
		cpfVendedor.setDataEmissao(new Date());
		cpfVendedor.setNumero("85647381922");
		cpfVendedor.setTipo(TipoDocumento.CPF);
		vendedor.getDocumentos().add(cpfVendedor);

		Endereco enderecoVendedor = new Endereco();
		enderecoVendedor.setEstado("São Paulo");
		enderecoVendedor.setCidade("São Paulo");
		enderecoVendedor.setBairro("Jardins");
		enderecoVendedor.setRua("Av. São Gabriel");
		enderecoVendedor.setNumero("10");
		enderecoVendedor.setCodigoPostal("01435-001");
		vendedor.setEndereco(enderecoVendedor);

		empresa.getUsuarios().add(vendedor);

		// 4. Segundo Vendedor (para validar isolamento entre vendedores)
		Usuario vendedor2 = new Usuario();
		vendedor2.setNome("Maria Vendedora");
		vendedor2.setNomeSocial("Maria");
		vendedor2.getPerfis().add(PerfilUsuario.ROLE_VENDEDOR);
		vendedor2.getCredenciais().add(criarCredencial("vendedor2", "123456"));
		empresa.getUsuarios().add(vendedor2);

		// 5. Cliente 1
		Usuario cliente = new Usuario();
		cliente.setNome("João Cliente Silva");
		cliente.setNomeSocial("João");
		cliente.getPerfis().add(PerfilUsuario.ROLE_CLIENTE);
		cliente.getCredenciais().add(criarCredencial("cliente", "123456"));

		Email emailCliente = new Email();
		emailCliente.setEndereco("cliente@autobots.com");
		cliente.getEmails().add(emailCliente);

		Documento cpfCliente = new Documento();
		cpfCliente.setDataEmissao(new Date());
		cpfCliente.setNumero("12584698533");
		cpfCliente.setTipo(TipoDocumento.CPF);
		cliente.getDocumentos().add(cpfCliente);

		Endereco enderecoCliente = new Endereco();
		enderecoCliente.setEstado("São Paulo");
		enderecoCliente.setCidade("São José dos Campos");
		enderecoCliente.setBairro("Centro");
		enderecoCliente.setRua("Av. Dr. Nelson D'Ávila");
		enderecoCliente.setNumero("200");
		enderecoCliente.setCodigoPostal("12245-070");
		cliente.setEndereco(enderecoCliente);

		Veiculo veiculo = new Veiculo();
		veiculo.setPlaca("ABC-0000");
		veiculo.setModelo("corolla-cross");
		veiculo.setTipo(TipoVeiculo.SUV);
		veiculo.setProprietario(cliente);
		cliente.getVeiculos().add(veiculo);

		empresa.getUsuarios().add(cliente);

		// 6. Cliente 2 (para validar isolamento entre clientes)
		Usuario cliente2 = new Usuario();
		cliente2.setNome("Ana Cliente Souza");
		cliente2.setNomeSocial("Ana");
		cliente2.getPerfis().add(PerfilUsuario.ROLE_CLIENTE);
		cliente2.getCredenciais().add(criarCredencial("cliente2", "123456"));
		empresa.getUsuarios().add(cliente2);

		// Mercadoria e Serviços
		Mercadoria rodaLigaLeve = new Mercadoria();
		rodaLigaLeve.setCadastro(new Date());
		rodaLigaLeve.setFabricao(new Date());
		rodaLigaLeve.setNome("Roda de liga leve modelo toyota etios");
		rodaLigaLeve.setValidade(new Date());
		rodaLigaLeve.setQuantidade(30);
		rodaLigaLeve.setValor(300.0);
		rodaLigaLeve.setDescricao("Roda de liga leve original de fábrica da Toyota");
		empresa.getMercadorias().add(rodaLigaLeve);

		Servico trocaRodas = new Servico();
		trocaRodas.setDescricao("Troca das rodas do carro por novas");
		trocaRodas.setNome("Troca de rodas");
		trocaRodas.setValor(50.0);
		empresa.getServicos().add(trocaRodas);

		Servico alinhamento = new Servico();
		alinhamento.setDescricao("Alinhamento das rodas do carro");
		alinhamento.setNome("Alinhamento de rodas");
		alinhamento.setValor(50.0);
		empresa.getServicos().add(alinhamento);

		// Venda 1 (vendedor -> cliente)
		Venda venda1 = new Venda();
		venda1.setCadastro(new Date());
		venda1.setIdentificacao("VND-0001");
		venda1.setCliente(cliente);
		venda1.setFuncionario(vendedor);
		venda1.setVeiculo(veiculo);
		venda1.getMercadorias().add(rodaLigaLeve);
		venda1.getServicos().add(trocaRodas);
		veiculo.getVendas().add(venda1);
		empresa.getVendas().add(venda1);

		// Venda 2 (vendedor2 -> cliente2)
		Venda venda2 = new Venda();
		venda2.setCadastro(new Date());
		venda2.setIdentificacao("VND-0002");
		venda2.setCliente(cliente2);
		venda2.setFuncionario(vendedor2);
		venda2.getServicos().add(alinhamento);
		empresa.getVendas().add(venda2);

		repositorioEmpresa.save(empresa);
	}

	private CredencialUsuarioSenha criarCredencial(String nomeUsuario, String senhaPura) {
		CredencialUsuarioSenha cred = new CredencialUsuarioSenha();
		cred.setInativo(false);
		cred.setNomeUsuario(nomeUsuario);
		cred.setSenha(codificador.encode(senhaPura));
		cred.setCriacao(new Date());
		cred.setUltimoAcesso(new Date());
		return cred;
	}
}
