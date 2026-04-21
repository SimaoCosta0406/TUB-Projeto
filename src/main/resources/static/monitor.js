async function carregarDadosOcupacao() {
    try {
        const response = await fetch('/api/passengers/live-status');
        const dados = await response.json();
        
        const tabela = document.getElementById('tabela-ocupacao');
        tabela.innerHTML = ''; // Limpar antes de atualizar

        dados.forEach(veiculo => {
            // UC6 - Subfluxo 6.1: O sistema valida e calcula a ocupação [cite: 46, 47]
            const ocupacao = veiculo.entradas - veiculo.saidas;
            
            // Deteção de anomalias (Fluxo Alternativo 5.2) [cite: 38, 39]
            let status = ocupacao > 50 ? "Lotação Elevada" : "Normal";
            if(ocupacao < 0) status = "Erro: Dados Inconsistentes";

            tabela.innerHTML += `
                <tr>
                    <td>${veiculo.veiculoId}</td>
                    <td><b>${ocupacao}</b> passageiros</td>
                    <td>${status}</td>
                </tr>
            `;
        });
    } catch (error) {
        // Fluxo Alternativo 5.1: Falha na receção [cite: 31, 32]
        document.getElementById('alerta-sensor').style.display = 'block';
    }
}

// Função para submeter contagem de passageiros
document.getElementById('passenger-form').addEventListener('submit', async function(event) {
    event.preventDefault();
    
    const panelId = document.getElementById('panelId').value;
    const line = document.getElementById('line').value;
    const entryCount = document.getElementById('entryCount').value;
    const exitCount = document.getElementById('exitCount').value;
    
    const data = {
        panelId: parseInt(panelId),
        line: line,
        timestamp: new Date().toISOString(),
        entryCount: parseInt(entryCount),
        exitCount: parseInt(exitCount)
    };
    
    try {
        const response = await fetch('/api/passengers', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        
        if (response.ok) {
            alert('Contagem submetida com sucesso!');
            // Limpar formulário
            document.getElementById('passenger-form').reset();
            // Recarregar dados
            carregarDadosOcupacao();
        } else {
            alert('Erro ao submeter contagem.');
        }
    } catch (error) {
        alert('Erro de rede.');
    }
});

// Atualiza a cada 10 segundos conforme o Caso de Uso [cite: 11]
setInterval(carregarDadosOcupacao, 10000);