#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
PASS=0
FAIL=0

GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_section() {
    echo -e "\n${BLUE}=================================================================${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}=================================================================${NC}"
}

assert_status() {
    local expected="$1"
    local actual="$2"
    local desc="$3"
    if [ "$actual" -eq "$expected" ]; then
        echo -e "  [${GREEN}PASS${NC}] (HTTP $actual) $desc"
        PASS=$((PASS + 1))
    else
        echo -e "  [${RED}FAIL${NC}] (Esperado HTTP $expected, obteve HTTP $actual) $desc"
        FAIL=$((FAIL + 1))
    fi
}

assert_contains() {
    local body="$1"
    local needle="$2"
    local desc="$3"
    if echo "$body" | grep -q "$needle"; then
        echo -e "  [${GREEN}PASS${NC}] $desc"
        PASS=$((PASS + 1))
    else
        echo -e "  [${RED}FAIL${NC}] $desc (não encontrou '$needle')"
        FAIL=$((FAIL + 1))
    fi
}

assert_not_contains() {
    local body="$1"
    local needle="$2"
    local desc="$3"
    if ! echo "$body" | grep -q "$needle"; then
        echo -e "  [${GREEN}PASS${NC}] $desc"
        PASS=$((PASS + 1))
    else
        echo -e "  [${RED}FAIL${NC}] $desc (encontrou '$needle' indevidamente!)"
        FAIL=$((FAIL + 1))
    fi
}

# Helper: faz requisição HTTP e retorna "BODY\nSTATUS"
http_req() {
    local method="$1"
    local path="$2"
    local token="${3:-}"
    local data="${4:-}"
    local args=(-s -w "\n%{http_code}" -X "$method" "${BASE_URL}${path}")
    if [ -n "$token" ]; then
        args+=(-H "Authorization: ${token}")
    fi
    if [ -n "$data" ]; then
        args+=(-H "Content-Type: application/json" -d "$data")
    fi
    curl "${args[@]}"
}

get_code() {
    echo "$1" | tail -n 1
}

get_body() {
    echo "$1" | sed '$d'
}

json_id() {
    python3 -c 'import sys, json; print(json.load(sys.stdin)["id"])' <<< "$1"
}

login_jwt() {
    local user="$1"
    local pass="$2"
    local headers
    headers=$(curl -s -D - -o /dev/null -X POST "${BASE_URL}/login" \
        -H "Content-Type: application/json" \
        -d "{\"nomeUsuario\":\"${user}\",\"senha\":\"${pass}\"}")
    echo "$headers" | grep -i '^Authorization:' | sed 's/^[Aa]uthorization:[[:space:]]*//' | tr -d '\r\n'
}

# =====================================================================
log_section "1. ROTAS PÚBLICAS, SWAGGER E BLOQUEIO SEM TOKEN"
# =====================================================================
RES=$(http_req GET "/v3/api-docs")
assert_status 200 "$(get_code "$RES")" "GET /v3/api-docs acessível publicamente"
assert_contains "$(get_body "$RES")" "bearerAuth" "Swagger OpenAPI expõe esquema bearerAuth (JWT)"

RES=$(http_req GET "/swagger-ui/index.html")
assert_status 200 "$(get_code "$RES")" "GET /swagger-ui/index.html disponível"

RES=$(http_req GET "/usuario/usuarios")
assert_status 403 "$(get_code "$RES")" "GET /usuario/usuarios sem token retorna 403"

RES=$(http_req GET "/venda/vendas" "Bearer token.invalido.adulterado")
assert_status 403 "$(get_code "$RES")" "GET /venda/vendas com JWT inválido retorna 403"

RES=$(http_req POST "/login" "" '{"nomeUsuario":"admin","senha":"senha_errada"}')
assert_status 401 "$(get_code "$RES")" "POST /login com senha incorreta retorna 401"

# =====================================================================
log_section "2. AUTENTICAÇÃO JWT DOS 4 PERFIS (POST /login)"
# =====================================================================
TOKEN_ADMIN=$(login_jwt "admin" "123456")
assert_contains "$TOKEN_ADMIN" "Bearer " "Login ADMIN retornou JWT Bearer"

TOKEN_GERENTE=$(login_jwt "gerente" "123456")
assert_contains "$TOKEN_GERENTE" "Bearer " "Login GERENTE retornou JWT Bearer"

TOKEN_VENDEDOR=$(login_jwt "vendedor" "123456")
assert_contains "$TOKEN_VENDEDOR" "Bearer " "Login VENDEDOR retornou JWT Bearer"

TOKEN_CLIENTE=$(login_jwt "cliente" "123456")
assert_contains "$TOKEN_CLIENTE" "Bearer " "Login CLIENTE retornou JWT Bearer"

# Descobrir IDs reais dos usuários e vendas da carga inicial
ME_ADMIN=$(get_body "$(http_req GET "/usuario/me" "$TOKEN_ADMIN")")
ID_ADMIN=$(json_id "$ME_ADMIN")
assert_not_contains "$ME_ADMIN" '"senha"' "JSON de usuário NÃO vaza campo 'senha' (WRITE_ONLY)"
assert_contains "$ME_ADMIN" '"_links"' "Resposta individual inclui HATEOAS (_links)"

ID_GERENTE=$(json_id "$(get_body "$(http_req GET "/usuario/me" "$TOKEN_GERENTE")")")
ID_VENDEDOR=$(json_id "$(get_body "$(http_req GET "/usuario/me" "$TOKEN_VENDEDOR")")")
ID_CLIENTE=$(json_id "$(get_body "$(http_req GET "/usuario/me" "$TOKEN_CLIENTE")")")

TOKEN_VENDEDOR2=$(login_jwt "vendedor2" "123456")
ID_VENDEDOR2=$(json_id "$(get_body "$(http_req GET "/usuario/me" "$TOKEN_VENDEDOR2")")")

TOKEN_CLIENTE2=$(login_jwt "cliente2" "123456")
ID_CLIENTE2=$(json_id "$(get_body "$(http_req GET "/usuario/me" "$TOKEN_CLIENTE2")")")

VENDAS_ALL=$(get_body "$(http_req GET "/venda/vendas" "$TOKEN_ADMIN")")
ID_VENDA1=$(python3 -c 'import sys,json; print([v["id"] for v in json.load(sys.stdin) if v["identificacao"]=="VND-0001"][0])' <<< "$VENDAS_ALL")
ID_VENDA2=$(python3 -c 'import sys,json; print([v["id"] for v in json.load(sys.stdin) if v["identificacao"]=="VND-0002"][0])' <<< "$VENDAS_ALL")

echo -e "  ${YELLOW}IDs detectados -> admin:$ID_ADMIN gerente:$ID_GERENTE vendedor:$ID_VENDEDOR vendedor2:$ID_VENDEDOR2 cliente:$ID_CLIENTE cliente2:$ID_CLIENTE2 venda1:$ID_VENDA1 venda2:$ID_VENDA2${NC}"

# =====================================================================
log_section "3. PERFIL ADMINISTRADOR (CRUD TOTAL + OUTROS ADMINS)"
# =====================================================================
RES=$(http_req GET "/empresa/1" "$TOKEN_ADMIN")
assert_status 200 "$(get_code "$RES")" "ADMIN lê empresa por ID"
assert_contains "$(get_body "$RES")" '"_links"' "Empresa possui links HATEOAS"

RES=$(http_req POST "/usuario/cadastro" "$TOKEN_ADMIN" '{"nome":"Admin E2E","perfis":["ROLE_ADMIN"],"credenciais":[{"tipo":"usuarioSenha","nomeUsuario":"admin_e2e","senha":"secret123"}]}')
assert_status 201 "$(get_code "$RES")" "ADMIN cria novo usuário ROLE_ADMIN"
ID_ADMIN_E2E=$(json_id "$(get_body "$RES")")

TOKEN_ADMIN_E2E=$(login_jwt "admin_e2e" "secret123")
assert_contains "$TOKEN_ADMIN_E2E" "Bearer " "Novo ADMIN criado via API consegue logar com senha BCrypt!"

RES=$(http_req PUT "/usuario/atualizar" "$TOKEN_ADMIN" "{\"id\":${ID_ADMIN_E2E},\"nomeSocial\":\"Admin Atualizado\"}")
assert_status 200 "$(get_code "$RES")" "ADMIN atualiza outro ADMIN"

RES=$(http_req DELETE "/usuario/excluir/${ID_ADMIN_E2E}" "$TOKEN_ADMIN")
assert_status 200 "$(get_code "$RES")" "ADMIN remove outro ADMIN"

RES=$(http_req POST "/empresa/cadastro" "$TOKEN_ADMIN" '{"razaoSocial":"VW Service Ltda","nomeFantasia":"VW Oficina"}')
assert_status 201 "$(get_code "$RES")" "ADMIN cadastra nova Empresa"
ID_EMP_E2E=$(json_id "$(get_body "$RES")")
RES=$(http_req DELETE "/empresa/excluir/${ID_EMP_E2E}" "$TOKEN_ADMIN")
assert_status 200 "$(get_code "$RES")" "ADMIN exclui Empresa"

# =====================================================================
log_section "4. PERFIL GERENTE (CRUD GERENTE/VENDEDOR/CLIENTE + SERVIÇOS/MERCADORIAS/VENDAS; BLOQUEADO PARA ADMIN)"
# =====================================================================
RES=$(http_req GET "/usuario/${ID_ADMIN}" "$TOKEN_GERENTE")
assert_status 403 "$(get_code "$RES")" "GERENTE bloqueado ao ler ADMIN (403)"

RES=$(http_req POST "/usuario/cadastro" "$TOKEN_GERENTE" '{"nome":"Admin Ilegal","perfis":["ROLE_ADMIN"]}')
assert_status 403 "$(get_code "$RES")" "GERENTE bloqueado ao criar ADMIN (403)"

RES=$(http_req PUT "/usuario/atualizar" "$TOKEN_GERENTE" "{\"id\":${ID_ADMIN},\"nome\":\"Hack\"}")
assert_status 403 "$(get_code "$RES")" "GERENTE bloqueado ao atualizar ADMIN (403)"

RES=$(http_req PUT "/usuario/atualizar" "$TOKEN_GERENTE" "{\"id\":${ID_VENDEDOR},\"perfis\":[\"ROLE_ADMIN\"]}")
assert_status 403 "$(get_code "$RES")" "GERENTE bloqueado ao escalar perfil para ROLE_ADMIN (403)"

RES=$(http_req DELETE "/usuario/excluir/${ID_ADMIN}" "$TOKEN_GERENTE")
assert_status 403 "$(get_code "$RES")" "GERENTE bloqueado ao excluir ADMIN (403)"

RES=$(http_req GET "/usuario/usuarios" "$TOKEN_GERENTE")
assert_status 200 "$(get_code "$RES")" "GERENTE lista usuários permitidos"
assert_not_contains "$(get_body "$RES")" "Administrador AutoBots" "Listagem do GERENTE filtra e oculta ADMINs"

# CRUD Gerente sobre Gerente/Vendedor/Cliente
for ROLE in ROLE_GERENTE ROLE_VENDEDOR ROLE_CLIENTE; do
    RES=$(http_req POST "/usuario/cadastro" "$TOKEN_GERENTE" "{\"nome\":\"Teste ${ROLE}\",\"perfis\":[\"${ROLE}\"]}")
    assert_status 201 "$(get_code "$RES")" "GERENTE cria usuário ${ROLE}"
    TMP_ID=$(json_id "$(get_body "$RES")")
    RES=$(http_req GET "/usuario/${TMP_ID}" "$TOKEN_GERENTE")
    assert_status 200 "$(get_code "$RES")" "GERENTE lê usuário ${ROLE}"
    RES=$(http_req PUT "/usuario/atualizar" "$TOKEN_GERENTE" "{\"id\":${TMP_ID},\"nomeSocial\":\"Editado\"}")
    assert_status 200 "$(get_code "$RES")" "GERENTE atualiza usuário ${ROLE}"
    RES=$(http_req DELETE "/usuario/excluir/${TMP_ID}" "$TOKEN_GERENTE")
    assert_status 200 "$(get_code "$RES")" "GERENTE exclui usuário ${ROLE}"
done

# CRUD Gerente sobre Serviço
RES=$(http_req POST "/servico/cadastro" "$TOKEN_GERENTE" '{"nome":"Revisão 10k","valor":450.0,"descricao":"Revisão completa"}')
assert_status 201 "$(get_code "$RES")" "GERENTE cria Serviço"
ID_SRV=$(json_id "$(get_body "$RES")")
RES=$(http_req GET "/servico/${ID_SRV}" "$TOKEN_GERENTE")
assert_status 200 "$(get_code "$RES")" "GERENTE lê Serviço"
RES=$(http_req PUT "/servico/atualizar" "$TOKEN_GERENTE" "{\"id\":${ID_SRV},\"valor\":490.0}")
assert_status 200 "$(get_code "$RES")" "GERENTE atualiza Serviço"
RES=$(http_req DELETE "/servico/excluir/${ID_SRV}" "$TOKEN_GERENTE")
assert_status 200 "$(get_code "$RES")" "GERENTE exclui Serviço"

# CRUD Gerente sobre Mercadoria
RES=$(http_req POST "/mercadoria/cadastro" "$TOKEN_GERENTE" '{"nome":"Filtro de óleo","validade":"2028-01-01","fabricao":"2026-01-01","quantidade":50,"valor":45.0,"descricao":"Original Toyota"}')
assert_status 201 "$(get_code "$RES")" "GERENTE cria Mercadoria"
ID_MERC=$(json_id "$(get_body "$RES")")
RES=$(http_req GET "/mercadoria/${ID_MERC}" "$TOKEN_GERENTE")
assert_status 200 "$(get_code "$RES")" "GERENTE lê Mercadoria"
RES=$(http_req PUT "/mercadoria/atualizar" "$TOKEN_GERENTE" "{\"id\":${ID_MERC},\"quantidade\":40}")
assert_status 200 "$(get_code "$RES")" "GERENTE atualiza Mercadoria"
RES=$(http_req DELETE "/mercadoria/excluir/${ID_MERC}" "$TOKEN_GERENTE")
assert_status 200 "$(get_code "$RES")" "GERENTE exclui Mercadoria"

# CRUD Gerente sobre Venda (referenciando cliente, funcionario, servico e mercadoria existentes!)
RES=$(http_req POST "/venda/cadastro" "$TOKEN_GERENTE" "{\"identificacao\":\"VND-GERENTE-E2E\",\"cliente\":{\"id\":${ID_CLIENTE}},\"funcionario\":{\"id\":${ID_VENDEDOR}},\"servicos\":[{\"id\":1}],\"mercadorias\":[{\"id\":1}]}")
assert_status 201 "$(get_code "$RES")" "GERENTE cria Venda com referências existentes"
ID_VND_G=$(json_id "$(get_body "$RES")")
RES=$(http_req GET "/venda/${ID_VND_G}" "$TOKEN_GERENTE")
assert_status 200 "$(get_code "$RES")" "GERENTE lê Venda"
RES=$(http_req PUT "/venda/atualizar" "$TOKEN_GERENTE" "{\"id\":${ID_VND_G},\"identificacao\":\"VND-GERENTE-E2E-UPD\"}")
assert_status 200 "$(get_code "$RES")" "GERENTE atualiza Venda"
RES=$(http_req DELETE "/venda/excluir/${ID_VND_G}" "$TOKEN_GERENTE")
assert_status 200 "$(get_code "$RES")" "GERENTE exclui Venda"

# =====================================================================
log_section "5. PERFIL VENDEDOR (CRUD CLIENTES; LÊ SERVIÇOS/MERCADORIAS; CRIA/LÊ PRÓPRIAS VENDAS)"
# =====================================================================
# CRUD de Cliente
RES=$(http_req POST "/usuario/cadastro" "$TOKEN_VENDEDOR" '{"nome":"Cliente Novo Vendedor","perfis":["ROLE_CLIENTE"]}')
assert_status 201 "$(get_code "$RES")" "VENDEDOR cria CLIENTE"
ID_CLI_V=$(json_id "$(get_body "$RES")")
RES=$(http_req GET "/usuario/${ID_CLI_V}" "$TOKEN_VENDEDOR")
assert_status 200 "$(get_code "$RES")" "VENDEDOR lê CLIENTE"
RES=$(http_req PUT "/usuario/atualizar" "$TOKEN_VENDEDOR" "{\"id\":${ID_CLI_V},\"nomeSocial\":\"VIP\"}")
assert_status 200 "$(get_code "$RES")" "VENDEDOR atualiza CLIENTE"
RES=$(http_req DELETE "/usuario/excluir/${ID_CLI_V}" "$TOKEN_VENDEDOR")
assert_status 200 "$(get_code "$RES")" "VENDEDOR exclui CLIENTE"

# Bloqueios de usuário para Vendedor
RES=$(http_req GET "/usuario/${ID_GERENTE}" "$TOKEN_VENDEDOR")
assert_status 403 "$(get_code "$RES")" "VENDEDOR bloqueado ao ler GERENTE (403)"
RES=$(http_req GET "/usuario/${ID_VENDEDOR2}" "$TOKEN_VENDEDOR")
assert_status 403 "$(get_code "$RES")" "VENDEDOR bloqueado ao ler outro VENDEDOR (403)"
RES=$(http_req POST "/usuario/cadastro" "$TOKEN_VENDEDOR" '{"nome":"Outro Vendedor","perfis":["ROLE_VENDEDOR"]}')
assert_status 403 "$(get_code "$RES")" "VENDEDOR bloqueado ao criar VENDEDOR (403)"

# Serviços e Mercadorias: somente leitura
RES=$(http_req GET "/servico/servicos" "$TOKEN_VENDEDOR")
assert_status 200 "$(get_code "$RES")" "VENDEDOR lê lista de serviços"
RES=$(http_req GET "/mercadoria/mercadorias" "$TOKEN_VENDEDOR")
assert_status 200 "$(get_code "$RES")" "VENDEDOR lê lista de mercadorias"
RES=$(http_req POST "/servico/cadastro" "$TOKEN_VENDEDOR" '{"nome":"Proibido","valor":10.0}')
assert_status 403 "$(get_code "$RES")" "VENDEDOR bloqueado ao criar serviço (403)"
RES=$(http_req PUT "/mercadoria/atualizar" "$TOKEN_VENDEDOR" '{"id":1,"valor":1.0}')
assert_status 403 "$(get_code "$RES")" "VENDEDOR bloqueado ao atualizar mercadoria (403)"
RES=$(http_req DELETE "/mercadoria/excluir/1" "$TOKEN_VENDEDOR")
assert_status 403 "$(get_code "$RES")" "VENDEDOR bloqueado ao excluir mercadoria (403)"

# Vendas: criar própria e ler apenas próprias
RES=$(http_req POST "/venda/cadastro" "$TOKEN_VENDEDOR" "{\"identificacao\":\"VND-VEND-E2E\",\"cliente\":{\"id\":${ID_CLIENTE}}}")
assert_status 201 "$(get_code "$RES")" "VENDEDOR cria venda feita por si mesmo"
ID_VND_V=$(json_id "$(get_body "$RES")")

RES=$(http_req POST "/venda/cadastro" "$TOKEN_VENDEDOR" "{\"identificacao\":\"VND-FRAUDE-E2E\",\"funcionario\":{\"id\":${ID_VENDEDOR2}},\"cliente\":{\"id\":${ID_CLIENTE}}}")
assert_status 403 "$(get_code "$RES")" "VENDEDOR bloqueado ao criar venda em nome de outro vendedor (403)"

RES=$(http_req GET "/venda/vendas" "$TOKEN_VENDEDOR")
assert_status 200 "$(get_code "$RES")" "VENDEDOR lista suas próprias vendas"
assert_contains "$(get_body "$RES")" "VND-0001" "Listagem do VENDEDOR inclui sua venda VND-0001"
assert_not_contains "$(get_body "$RES")" "VND-0002" "Listagem do VENDEDOR oculta venda VND-0002 de outro vendedor"

RES=$(http_req GET "/venda/${ID_VENDA2}" "$TOKEN_VENDEDOR")
assert_status 403 "$(get_code "$RES")" "VENDEDOR bloqueado ao ler venda de outro vendedor (403)"

RES=$(http_req PUT "/venda/atualizar" "$TOKEN_VENDEDOR" "{\"id\":${ID_VND_V},\"identificacao\":\"HACK\"}")
assert_status 403 "$(get_code "$RES")" "VENDEDOR bloqueado ao atualizar venda (403)"

RES=$(http_req DELETE "/venda/excluir/${ID_VND_V}" "$TOKEN_VENDEDOR")
assert_status 403 "$(get_code "$RES")" "VENDEDOR bloqueado ao excluir venda (403)"

# Limpar venda temporária com ADMIN
http_req DELETE "/venda/excluir/${ID_VND_V}" "$TOKEN_ADMIN" >/dev/null

# =====================================================================
log_section "6. PERFIL CLIENTE (LÊ APENAS PRÓPRIO CADASTRO E PRÓPRIAS VENDAS)"
# =====================================================================
RES=$(http_req GET "/usuario/me" "$TOKEN_CLIENTE")
assert_status 200 "$(get_code "$RES")" "CLIENTE lê próprio cadastro via /usuario/me"
assert_contains "$(get_body "$RES")" "João Cliente Silva" "Dados retornados pertencem ao cliente autenticado"

RES=$(http_req GET "/usuario/${ID_CLIENTE}" "$TOKEN_CLIENTE")
assert_status 200 "$(get_code "$RES")" "CLIENTE lê próprio cadastro via /usuario/{id}"

RES=$(http_req GET "/usuario/usuarios" "$TOKEN_CLIENTE")
assert_status 200 "$(get_code "$RES")" "CLIENTE em /usuario/usuarios recebe apenas a si mesmo"
COUNT_CLI=$(python3 -c 'import sys,json; print(len(json.load(sys.stdin)))' <<< "$(get_body "$RES")")
if [ "$COUNT_CLI" -eq 1 ]; then
    echo -e "  [${GREEN}PASS${NC}] Lista de /usuario/usuarios para CLIENTE tem exatamente 1 registro (apenas ele mesmo)"
    PASS=$((PASS + 1))
else
    echo -e "  [${RED}FAIL${NC}] Esperado 1 usuário na lista do CLIENTE, obteve $COUNT_CLI"
    FAIL=$((FAIL + 1))
fi

RES=$(http_req GET "/usuario/${ID_CLIENTE2}" "$TOKEN_CLIENTE")
assert_status 403 "$(get_code "$RES")" "CLIENTE bloqueado ao ler outro cliente (403)"

RES=$(http_req POST "/usuario/cadastro" "$TOKEN_CLIENTE" '{"nome":"Hack","perfis":["ROLE_CLIENTE"]}')
assert_status 403 "$(get_code "$RES")" "CLIENTE bloqueado ao cadastrar usuário (403)"

RES=$(http_req PUT "/usuario/atualizar" "$TOKEN_CLIENTE" "{\"id\":${ID_CLIENTE},\"nome\":\"Hack\"}")
assert_status 403 "$(get_code "$RES")" "CLIENTE bloqueado ao alterar cadastro (403)"

RES=$(http_req DELETE "/usuario/excluir/${ID_CLIENTE}" "$TOKEN_CLIENTE")
assert_status 403 "$(get_code "$RES")" "CLIENTE bloqueado ao excluir cadastro (403)"

RES=$(http_req GET "/venda/${ID_VENDA1}" "$TOKEN_CLIENTE")
assert_status 200 "$(get_code "$RES")" "CLIENTE lê venda da qual foi consumidor (VND-0001)"
assert_contains "$(get_body "$RES")" '"_links"' "Venda retornada ao CLIENTE inclui HATEOAS"

RES=$(http_req GET "/venda/vendas" "$TOKEN_CLIENTE")
assert_status 200 "$(get_code "$RES")" "CLIENTE lista suas próprias compras"
assert_contains "$(get_body "$RES")" "VND-0001" "Listagem do CLIENTE inclui sua compra VND-0001"
assert_not_contains "$(get_body "$RES")" "VND-0002" "Listagem do CLIENTE oculta compra VND-0002 de outro cliente"

RES=$(http_req GET "/venda/${ID_VENDA2}" "$TOKEN_CLIENTE")
assert_status 403 "$(get_code "$RES")" "CLIENTE bloqueado ao ler venda de outro cliente (403)"

RES=$(http_req POST "/venda/cadastro" "$TOKEN_CLIENTE" '{"identificacao":"HACK"}')
assert_status 403 "$(get_code "$RES")" "CLIENTE bloqueado ao criar venda (403)"

RES=$(http_req GET "/servico/servicos" "$TOKEN_CLIENTE")
assert_status 403 "$(get_code "$RES")" "CLIENTE bloqueado ao ler serviços (403)"

RES=$(http_req GET "/mercadoria/mercadorias" "$TOKEN_CLIENTE")
assert_status 403 "$(get_code "$RES")" "CLIENTE bloqueado ao ler mercadorias (403)"

RES=$(http_req GET "/credencial/credenciais" "$TOKEN_CLIENTE")
assert_status 403 "$(get_code "$RES")" "CLIENTE bloqueado ao ler credenciais (403)"

# =====================================================================
echo -e "\n${BLUE}=================================================================${NC}"
echo -e "  RESULTADO FINAL E2E: ${GREEN}${PASS} PASSARAM${NC} | ${RED}${FAIL} FALHARAM${NC}"
echo -e "${BLUE}=================================================================${NC}"

if [ "$FAIL" -gt 0 ]; then
    exit 1
fi
