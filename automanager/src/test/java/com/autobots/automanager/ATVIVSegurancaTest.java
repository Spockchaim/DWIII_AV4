package com.autobots.automanager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@SpringBootTest
@AutoConfigureMockMvc
class ATVIVSegurancaTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UsuarioRepositorio usuarioRepositorio;

	@Autowired
	private VendaRepositorio vendaRepositorio;

	private String autenticar(String nomeUsuario, String senha) throws Exception {
		String body = "{\"nomeUsuario\":\"" + nomeUsuario + "\",\"senha\":\"" + senha + "\"}";
		MvcResult result = mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isOk())
				.andReturn();
		String token = result.getResponse().getHeader("Authorization");
		assertNotNull(token, "Header Authorization deve ser retornado no login");
		assertTrue(token.startsWith("Bearer "), "Token deve iniciar com 'Bearer '");
		return token;
	}

	private Usuario buscarPorLogin(String nomeUsuario) {
		for (Usuario u : usuarioRepositorio.findAll()) {
			for (Credencial c : u.getCredenciais()) {
				if (c instanceof CredencialUsuarioSenha
						&& nomeUsuario.equals(((CredencialUsuarioSenha) c).getNomeUsuario())) {
					return u;
				}
			}
		}
		return null;
	}

	private Venda buscarVendaPorIdentificacao(String identificacao) {
		for (Venda v : vendaRepositorio.findAll()) {
			if (identificacao.equals(v.getIdentificacao())) {
				return v;
			}
		}
		return null;
	}

	// ==================== AUTENTICAÇÃO ====================

	@Test
	@DisplayName("1. Requisição sem token JWT deve ser bloqueada (403 Forbidden)")
	void testSemTokenBloqueado() throws Exception {
		mockMvc.perform(get("/usuario/usuarios"))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/venda/vendas"))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("2. Login com credenciais inválidas deve retornar 401 Unauthorized")
	void testLoginInvalido() throws Exception {
		mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nomeUsuario\":\"admin\",\"senha\":\"errada\"}"))
				.andExpect(status().isUnauthorized());
	}

	// ==================== ADMINISTRADOR ====================

	@Test
	@DisplayName("3. ADMIN tem acesso total: pode criar outro ADMIN, ler empresas e serviços com HATEOAS")
	void testAdminAcessoTotal() throws Exception {
		String token = autenticar("admin", "123456");

		mockMvc.perform(get("/empresa/1").header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$._links.self.href").exists());

		String novoAdmin = "{"
				+ "\"nome\":\"Segundo Admin\","
				+ "\"perfis\":[\"ROLE_ADMIN\"],"
				+ "\"credenciais\":[{\"tipo\":\"usuarioSenha\",\"nomeUsuario\":\"admin_temp\",\"senha\":\"123456\"}]"
				+ "}";
		mockMvc.perform(post("/usuario/cadastro")
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(novoAdmin))
				.andExpect(status().isCreated());

		Usuario criado = buscarPorLogin("admin_temp");
		assertNotNull(criado);

		mockMvc.perform(delete("/usuario/excluir/" + criado.getId())
				.header("Authorization", token))
				.andExpect(status().isOk());
	}

	// ==================== GERENTE ====================

	@Test
	@DisplayName("4. GERENTE pode fazer CRUD de serviços, mercadorias e vendas")
	void testGerenteCrudServicosMercadoriasVendas() throws Exception {
		String token = autenticar("gerente", "123456");

		mockMvc.perform(post("/servico/cadastro")
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Troca de óleo\",\"valor\":120.0,\"descricao\":\"Óleo sintético\"}"))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/mercadoria/mercadorias").header("Authorization", token))
				.andExpect(status().isOk());

		mockMvc.perform(get("/venda/vendas").header("Authorization", token))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("5. GERENTE pode criar VENDEDOR/CLIENTE mas NÃO pode criar/ler/excluir ADMIN")
	void testGerenteRestricaoAdmin() throws Exception {
		String token = autenticar("gerente", "123456");
		Usuario admin = buscarPorLogin("admin");

		// Bloqueado para ler ADMIN
		mockMvc.perform(get("/usuario/" + admin.getId()).header("Authorization", token))
				.andExpect(status().isForbidden());

		// Bloqueado para excluir ADMIN
		mockMvc.perform(delete("/usuario/excluir/" + admin.getId()).header("Authorization", token))
				.andExpect(status().isForbidden());

		// Bloqueado para criar ADMIN
		mockMvc.perform(post("/usuario/cadastro")
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Admin Ilegal\",\"perfis\":[\"ROLE_ADMIN\"]}"))
				.andExpect(status().isForbidden());

		// Permitido criar VENDEDOR
		mockMvc.perform(post("/usuario/cadastro")
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Vendedor Novo\",\"perfis\":[\"ROLE_VENDEDOR\"]}"))
				.andExpect(status().isCreated());
	}

	// ==================== VENDEDOR ====================

	@Test
	@DisplayName("6. VENDEDOR pode ler serviços/mercadorias, mas NÃO pode cadastrar ou excluir")
	void testVendedorSomenteLeituraServicosEMercadorias() throws Exception {
		String token = autenticar("vendedor", "123456");

		mockMvc.perform(get("/servico/servicos").header("Authorization", token))
				.andExpect(status().isOk());
		mockMvc.perform(get("/mercadoria/mercadorias").header("Authorization", token))
				.andExpect(status().isOk());

		mockMvc.perform(post("/servico/cadastro")
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Serviço Proibido\",\"valor\":99.0}"))
				.andExpect(status().isForbidden());

		mockMvc.perform(delete("/mercadoria/excluir/1").header("Authorization", token))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("7. VENDEDOR pode fazer CRUD de CLIENTE, mas NÃO de GERENTE/VENDEDOR/ADMIN")
	void testVendedorRestricaoUsuarios() throws Exception {
		String token = autenticar("vendedor", "123456");
		Usuario gerente = buscarPorLogin("gerente");

		mockMvc.perform(get("/usuario/" + gerente.getId()).header("Authorization", token))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/usuario/cadastro")
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Gerente Proibido\",\"perfis\":[\"ROLE_GERENTE\"]}"))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/usuario/cadastro")
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Cliente do Vendedor\",\"perfis\":[\"ROLE_CLIENTE\"]}"))
				.andExpect(status().isCreated());
	}

	@Test
	@DisplayName("8. VENDEDOR pode criar e ler apenas suas próprias vendas")
	void testVendedorVendasProprias() throws Exception {
		String token = autenticar("vendedor", "123456");
		Usuario cliente = buscarPorLogin("cliente");
		Usuario vendedor2 = buscarPorLogin("vendedor2");
		Venda venda1 = buscarVendaPorIdentificacao("VND-0001");
		Venda venda2 = buscarVendaPorIdentificacao("VND-0002");

		// Pode ler sua própria venda (VND-0001)
		mockMvc.perform(get("/venda/" + venda1.getId()).header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$._links.self.href").exists());

		// Bloqueado para ler venda de outro vendedor (VND-0002)
		mockMvc.perform(get("/venda/" + venda2.getId()).header("Authorization", token))
				.andExpect(status().isForbidden());

		// Bloqueado para criar venda em nome de outro vendedor
		mockMvc.perform(post("/venda/cadastro")
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"identificacao\":\"VND-FRAUDE\",\"funcionario\":{\"id\":" + vendedor2.getId()
						+ "},\"cliente\":{\"id\":" + cliente.getId() + "}}"))
				.andExpect(status().isForbidden());

		// Permitido criar venda própria
		mockMvc.perform(post("/venda/cadastro")
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"identificacao\":\"VND-VENDEDOR-OK\",\"cliente\":{\"id\":" + cliente.getId() + "}}"))
				.andExpect(status().isCreated());
	}

	// ==================== CLIENTE ====================

	@Test
	@DisplayName("9. CLIENTE pode ler apenas o próprio cadastro e suas próprias vendas")
	void testClienteApenasPropriosDados() throws Exception {
		String token = autenticar("cliente", "123456");
		Usuario cliente = buscarPorLogin("cliente");
		Usuario cliente2 = buscarPorLogin("cliente2");
		Venda venda1 = buscarVendaPorIdentificacao("VND-0001");
		Venda venda2 = buscarVendaPorIdentificacao("VND-0002");

		// Pode ler o próprio cadastro (/usuario/me e /usuario/{id})
		mockMvc.perform(get("/usuario/me").header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("João Cliente Silva"))
				.andExpect(jsonPath("$._links.self.href").exists());

		mockMvc.perform(get("/usuario/" + cliente.getId()).header("Authorization", token))
				.andExpect(status().isOk());

		// Bloqueado para ler cadastro de outro cliente
		mockMvc.perform(get("/usuario/" + cliente2.getId()).header("Authorization", token))
				.andExpect(status().isForbidden());

		// Bloqueado para atualizar ou criar usuários
		mockMvc.perform(put("/usuario/atualizar")
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"id\":" + cliente.getId() + ",\"nome\":\"Hackeado\"}"))
				.andExpect(status().isForbidden());

		// Pode ler venda da qual foi consumidor (VND-0001)
		mockMvc.perform(get("/venda/" + venda1.getId()).header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.identificacao").value("VND-0001"));

		// Bloqueado para ler venda de outro cliente (VND-0002)
		mockMvc.perform(get("/venda/" + venda2.getId()).header("Authorization", token))
				.andExpect(status().isForbidden());

		// Bloqueado para ler serviços/mercadorias
		mockMvc.perform(get("/servico/servicos").header("Authorization", token))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/mercadoria/mercadorias").header("Authorization", token))
				.andExpect(status().isForbidden());
	}

	// ==================== SWAGGER ====================

	@Test
	@DisplayName("10. Documentação OpenAPI/Swagger deve estar acessível publicamente com esquema JWT")
	void testSwaggerPublico() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("AutoBots — API AutoManager (ATVIV)"))
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
	}
}
