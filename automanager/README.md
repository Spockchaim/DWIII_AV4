# 🔐 AutoBots — Microsserviço com Spring Security e JWT (`automanager` — ATVIV)

[![Java](https://img.shields.io/badge/Java-17%20%2F%2021-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.6.4-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Security-JWT%20Bearer-6DB33F?logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![Build & Tests](https://img.shields.io/badge/Tests-11%20Passed-brightgreen)](https://github.com/)
[![Swagger](https://img.shields.io/badge/OpenAPI%203.0-Swagger%20UI-85EA2D?logo=swagger&logoColor=black)](http://localhost:8080/swagger-ui.html)
[![RMM](https://img.shields.io/badge/RMM-Level%203%20(HATEOAS)-blue)](https://martinfowler.com/articles/richardsonMaturityModel.html)

> **Atividade Prática ATVIV — Atualização de Segurança**  
> **Instituição:** Faculdade de Tecnologia do Estado de São Paulo (FATEC) — São José dos Campos  
> **Curso:** Análise e Desenvolvimento de Sistemas (3º Semestre)  
> **Disciplina:** Desenvolvimento Web III  
> **Docente:** Prof. Dr. Eng. Gerson Penha  

---

## 📖 1. Sobre o Projeto

Esta etapa implementa a **"Atualização de Segurança"** do microsserviço **`automanager`** (AutoBots), integrando **Spring Security + JSON Web Token (JWT)** sobre todo o ecossistema completo da ATVIII (Empresas, Usuários, Credenciais Polimórficas, Veículos, Mercadorias, Serviços, Vendas e HATEOAS Nível 3 RMM).

---

## 🔐 2. Autenticação via JWT (`POST /login`)

1. Envie uma requisição `POST http://localhost:8080/login` com o corpo JSON:
   ```json
   {
     "nomeUsuario": "admin",
     "senha": "123456"
   }
   ```
2. A API retorna o token JWT no cabeçalho de resposta **`Authorization`**:
   ```
   Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
   ```
3. Envie esse cabeçalho em todas as requisições protegidas (ou clique no botão **Authorize** no Swagger UI).

### 👥 Usuários Pré-cadastrados (Carga Inicial com BCrypt)

| Login (`nomeUsuario`) | Senha | Perfil (`PerfilUsuario`) |
| :--- | :--- | :--- |
| `admin` | `123456` | `ROLE_ADMIN` |
| `gerente` | `123456` | `ROLE_GERENTE` |
| `vendedor` | `123456` | `ROLE_VENDEDOR` |
| `vendedor2` | `123456` | `ROLE_VENDEDOR` |
| `cliente` | `123456` | `ROLE_CLIENTE` |
| `cliente2` | `123456` | `ROLE_CLIENTE` |

---

## 🛡️ 3. Matriz de Autorizações (Tabela 1 — Padrão Toyota/Volkswagen)

| Perfil | Usuários | Serviços e Mercadorias | Vendas |
| :--- | :--- | :--- | :--- |
| **`ROLE_ADMIN`** | CRUD total (incluindo outros `ADMIN`) | CRUD total | CRUD total |
| **`ROLE_GERENTE`** | CRUD sobre `GERENTE`, `VENDEDOR` e `CLIENTE` *(bloqueado para `ADMIN`)* | CRUD total | CRUD total |
| **`ROLE_VENDEDOR`** | CRUD apenas sobre `CLIENTE` | Apenas **Leitura** (`GET`) | **Criar** vendas feitas por si mesmo e **Ler** apenas suas próprias vendas |
| **`ROLE_CLIENTE`** | Apenas **Leitura** do **próprio cadastro** (`/usuario/me` e `/usuario/{id}`) | Sem acesso (`403`) | Apenas **Leitura** das vendas em que é o consumidor |

---

## 🚀 4. Execução e Testes

### Rodar a suíte de testes automatizados (Segurança + JWT + HATEOAS):
```bash
cd automanager
./mvnw test
```

### Executar a aplicação:
```bash
cd automanager
./mvnw spring-boot:run
```

* **Swagger UI (com botão Authorize JWT):** `http://localhost:8080/swagger-ui.html`
* **Console H2:** `http://localhost:8080/h2-console` (`jdbc:h2:mem:automanagerdb` | Usuário: `sa` | Senha em branco)
