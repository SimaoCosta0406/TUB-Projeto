# Testes de Simulação de Ocupação

## Comandos CURL para Simular Entradas e Saídas

### Pré-requisitos
- A aplicação deve estar em execução (geralmente em `http://localhost:8080`)
- Um painel deve existir no sistema (por exemplo, com ID: 1)

### 1. Simular Entradas (5 passageiros)
```bash
curl -X POST "http://localhost:8080/api/passengers/simulate/entry?panelId=1&count=5"
```

### 2. Simular Saídas (3 passageiros)
```bash
curl -X POST "http://localhost:8080/api/passengers/simulate/exit?panelId=1&count=3"
```

### 3. Simular Mais Entradas (10 passageiros)
```bash
curl -X POST "http://localhost:8080/api/passengers/simulate/entry?panelId=1&count=10"
```

### 4. Simular Múltiplas Saídas (4 passageiros)
```bash
curl -X POST "http://localhost:8080/api/passengers/simulate/exit?panelId=1&count=4"
```

### 5. Verificar Ocupação Atual (sem parâmetro count, assume 1)
```bash
curl -X POST "http://localhost:8080/api/passengers/simulate/entry?panelId=1"
```

## Fluxo de Teste Sugerido

1. **Simular um fluxo completo de passageiros:**
   ```bash
   # Entram 10 passageiros
   curl -X POST "http://localhost:8080/api/passengers/simulate/entry?panelId=1&count=10"
   
   # Saem 3 passageiros
   curl -X POST "http://localhost:8080/api/passengers/simulate/exit?panelId=1&count=3"
   
   # Entram mais 15 passageiros
   curl -X POST "http://localhost:8080/api/passengers/simulate/entry?panelId=1&count=15"
   ```

2. **Consultar a tabela de monitorização:**
   - Aceda a: `http://localhost:8080/monitorizacao.html`
   - Faça login como ADMIN
   - Verá a ocupação total e percentagem (baseado em 55 lugares)

## Exemplos com Múltiplos Painéis

Se tem múltiplos painéis (ID 1, 2, 3), pode testar:

```bash
# Painel 1: 20 entradas
curl -X POST "http://localhost:8080/api/passengers/simulate/entry?panelId=1&count=20"

# Painel 2: 15 entradas
curl -X POST "http://localhost:8080/api/passengers/simulate/entry?panelId=2&count=15"

# Painel 3: 30 entradas
curl -X POST "http://localhost:8080/api/passengers/simulate/entry?panelId=3&count=30"

# Painel 3: 10 saídas
curl -X POST "http://localhost:8080/api/passengers/simulate/exit?panelId=3&count=10"
```

## Cálculo de Ocupação

A ocupação é calculada como:
- **Ocupação Total** = Entradas - Saídas
- **Percentagem** = (Ocupação Total / 55) × 100%
- **Capacidade do Autocarro** = 55 lugares

### Exemplo:
- Se 20 pessoas entram e 5 saem:
  - Ocupação Total: 20 - 5 = **15 pessoas**
  - Percentagem: (15 / 55) × 100 = **27%**

## Cores de Alerta na Tabela

A cor da ocupação muda conforme a percentagem:
- 🟢 **Verde**: ≤ 60% (ocupação baixa)
- 🟡 **Amarelo**: 60% - 80% (ocupação média)
- 🔴 **Vermelho**: > 80% (ocupação alta)
