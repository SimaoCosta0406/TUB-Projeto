# 🔐 Guia de Configuração Auth0 - TUB Projeto

## 📋 Visão Geral

Este guia explica como configurar a autenticação OAuth2/OIDC com Auth0 para o projeto TUB.

**O que foi implementado:**
- ✅ Login com Auth0 (OAuth2/OIDC)
- ✅ Roles: ADMIN e USER
- ✅ Sistema de painéis mantido intacto (não afetado)
- ✅ Visitantes podem aceder sem login (apenas Home)
- ✅ Configuração de segurança permissiva (não quebra funcionalidades existentes)

---

## 🚀 PASSO 1: Criar Conta Auth0

1. Aceda a [https://auth0.com](https://auth0.com)
2. Crie uma conta gratuita
3. Crie um novo Tenant (ex: `tub-projeto`)
4. Região: **EU** (Europa)

---

## 🔧 PASSO 2: Configurar Application (SPA)

### 2.1 - Criar Application

1. No dashboard Auth0, vá a **Applications** → **Applications**
2. Clique em **Create Application**
3. Nome: `TUB-Projeto-SPA`
4. Tipo: **Single Page Web Applications**
5. Clique em **Create**

### 2.2 - Configurar URLs

Na aba **Settings** da aplicação:

```
Allowed Callback URLs:
http://localhost:8080/login.html

Allowed Logout URLs:
http://localhost:8080/index.html

Allowed Web Origins:
http://localhost:8080
```

**⚠️ IMPORTANTE:** Quando fizer deploy em produção, adicione também as URLs de produção aqui!

### 2.3 - Copiar Client ID

Na mesma página, copie o **Client ID** (vai precisar dele no próximo passo).

---

## 🔑 PASSO 3: Configurar API (Resource Server)

1. Vá a **Applications** → **APIs**
2. Clique em **Create API**
3. Preencha:
   - **Name:** `TUB-API`
   - **Identifier (Audience):** `https://api.tub.pt`
   - **Signing Algorithm:** RS256
4. Clique em **Create**

---

## 👥 PASSO 4: Criar Roles

1. Vá a **User Management** → **Roles**
2. Clique em **Create Role**

### Role 1: ADMIN
- **Name:** `ADMIN`
- **Description:** `Administrador com acesso total ao sistema`

### Role 2: USER
- **Name:** `USER`
- **Description:** `Utilizador comum com acesso ao mapa, rotas e paragens`

---

## 🎭 PASSO 5: Criar Action (Adicionar Roles ao Token)

1. Vá a **Actions** → **Flows** → **Login**
2. Clique no botão **+** (Custom)
3. Clique em **Build from scratch**
4. Nome: `Add Roles to Token`
5. Cole o seguinte código:

```javascript
exports.onExecutePostLogin = async (event, api) => {
  const namespace = 'https://api.tub.pt';
  
  if (event.authorization) {
    // Adicionar roles ao ID Token
    api.idToken.setCustomClaim(`${namespace}/roles`, event.authorization.roles);
    
    // Adicionar roles ao Access Token
    api.accessToken.setCustomClaim(`${namespace}/roles`, event.authorization.roles);
  }
};
```

6. Clique em **Deploy**
7. Volte ao **Login Flow**
8. **Arraste** a Action `Add Roles to Token` para o fluxo (entre Start e Complete)
9. Clique em **Apply**

---

## 👤 PASSO 6: Criar Utilizadores

### 6.1 - Criar Utilizador ADMIN

1. Vá a **User Management** → **Users**
2. Clique em **Create User**
3. Preencha:
   - **Email:** `admin@tub.pt`
   - **Password:** (escolha uma password segura)
   - **Connection:** Username-Password-Authentication
4. Clique em **Create**
5. Após criar, clique no utilizador
6. Vá à aba **Roles**
7. Clique em **Assign Roles**
8. Selecione **ADMIN**
9. Clique em **Assign**

### 6.2 - Criar Utilizador USER (para testes)

1. Repita o processo acima
2. Email: `user@tub.pt`
3. Atribua o role **USER**

---

## ⚙️ PASSO 7: Atualizar Código da Aplicação

### 7.1 - Atualizar `auth0-integration.js`

Abra o ficheiro `src/main/resources/static/auth0-integration.js` e substitua:

```javascript
domain: 'tub-projeto.eu.auth0.com',  // ⚠️ Substituir pelo seu domain
clientId: 'SEU_CLIENT_ID_AQUI',      // ⚠️ Substituir pelo Client ID copiado
```

**Como encontrar o Domain:**
- No dashboard Auth0, vá a **Applications** → **Applications**
- Clique na sua aplicação `TUB-Projeto-SPA`
- Na aba **Settings**, procure por **Domain** (ex: `tub-projeto.eu.auth0.com`)

---

## 🧪 PASSO 8: Testar a Aplicação

### 8.1 - Iniciar o Servidor

```bash
./mvnw spring-boot:run
```

### 8.2 - Testar Cenários

#### ✅ Cenário 1: Visitante sem login
1. Abra `http://localhost:8080`
2. Deve ver a **Home page**
3. Não deve conseguir aceder a mapa, rotas, etc.

#### ✅ Cenário 2: Login com ADMIN
1. Clique em **Login**
2. Clique em **🔐 Entrar com Auth0**
3. Faça login com `admin@tub.pt`
4. Deve ser redirecionado para o **mapa**
5. Deve ver **todos** os menus (Monitorização, Veículos, Rotas, etc.)

#### ✅ Cenário 3: Login com USER
1. Faça logout
2. Faça login com `user@tub.pt`
3. Deve ser redirecionado para o **mapa**
4. Deve ver apenas: Mapa, Rotas, Paragens (não vê Monitorização, Veículos)

#### ✅ Cenário 4: Sistema de Painéis (NÃO AFETADO)
1. Aceda a `http://localhost:8080/paineis-login.html`
2. Faça login com as credenciais tradicionais
3. Deve funcionar **exatamente como antes**

---

## 🔍 Troubleshooting

### Problema: "Erro ao autenticar"

**Solução:**
1. Verifique se o **Client ID** e **Domain** estão corretos em `auth0-integration.js`
2. Verifique se as **Callback URLs** estão configuradas corretamente no Auth0
3. Abra o **Console do navegador** (F12) e veja os erros

### Problema: "Roles não aparecem no token"

**Solução:**
1. Verifique se a **Action** foi criada e está **ativa** no Login Flow
2. Verifique se o utilizador tem o **role atribuído**
3. Faça logout e login novamente

### Problema: "Sistema de painéis não funciona"

**Solução:**
1. Verifique se o endpoint `/api/auth/login` está público no `SecurityConfig.java`
2. O sistema de painéis **não usa Auth0**, usa autenticação tradicional

---

## 📝 Notas Importantes

### ⚠️ Produção

Quando fizer deploy em produção:

1. **Atualize as URLs no Auth0:**
   - Allowed Callback URLs: `https://seu-dominio.com/login.html`
   - Allowed Logout URLs: `https://seu-dominio.com/index.html`
   - Allowed Web Origins: `https://seu-dominio.com`

2. **Atualize `auth0-integration.js`:**
   ```javascript
   redirect_uri: window.location.origin + '/login.html'
   ```
   (Já está dinâmico, não precisa alterar)

### 🔒 Segurança

- **Nunca** faça commit do Client Secret (não estamos a usar, mas se usar no futuro)
- Use **variáveis de ambiente** para configurações sensíveis em produção
- Ative **MFA** (Multi-Factor Authentication) no Auth0 para utilizadores ADMIN

### 📊 Monitorização

No dashboard Auth0, pode ver:
- **Logs** de autenticação
- **Utilizadores ativos**
- **Tentativas de login falhadas**

---

## ✅ Checklist Final

- [ ] Conta Auth0 criada
- [ ] Application (SPA) configurada
- [ ] API criada com audience `https://api.tub.pt`
- [ ] Roles ADMIN e USER criados
- [ ] Action "Add Roles to Token" criada e ativa
- [ ] Utilizador ADMIN criado e role atribuído
- [ ] Utilizador USER criado e role atribuído
- [ ] `auth0-integration.js` atualizado com Client ID e Domain
- [ ] Aplicação testada (visitante, ADMIN, USER, painéis)
- [ ] Compilação bem-sucedida (`./mvnw clean compile`)

---

## 🎓 Próximos Passos (Opcional)

### SAML 2.0 (Fase 2)
Se precisar de SAML 2.0 para integração enterprise:
1. Configure um **Enterprise Connection** no Auth0
2. Adicione metadata XML do Identity Provider
3. Configure certificados SSL

### SSO entre múltiplas aplicações
Se tiver outras aplicações:
1. Crie Applications separadas no Auth0
2. Use o mesmo Tenant
3. O SSO funcionará automaticamente

---

## 📞 Suporte

- **Documentação Auth0:** https://auth0.com/docs
- **Community Forum:** https://community.auth0.com
- **Status Page:** https://status.auth0.com

---

**Implementado por:** Cline AI Assistant  
**Data:** 30/05/2026  
**Versão:** 1.0 - Implementação Mínima e Funcional
