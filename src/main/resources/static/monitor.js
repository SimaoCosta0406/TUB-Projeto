async function carregarDadosOcupacao() {
    try {
        const response = await fetch('/api/panels');
        const panels = await response.json();

        const tabela = document.getElementById('tabela-ocupacao');
        tabela.innerHTML = ''; // Limpar antes de atualizar

        panels.forEach(panel => {
            // Gerar ocupação aleatória entre 0 e 60
            const ocupacao = Math.floor(Math.random() * 61);

            // Deteção de anomalias
            let status = ocupacao > 50 ? "Lotação Elevada" : "Normal";
            if (ocupacao < 0) status = "Erro: Dados Inconsistentes";

            tabela.innerHTML += `
                <tr>
                    <td>${panel.location} (ID: ${panel.id})</td>
                    <td><b>${ocupacao}</b> passageiros</td>
                    <td>${status}</td>
                </tr>
            `;
        });
    } catch (error) {
        // Falha na receção
        document.getElementById('alerta-sensor').style.display = 'block';
    }
}

// Carregar dados iniciais
carregarDadosOcupacao();

// Atualiza a cada 10 segundos
setInterval(carregarDadosOcupacao, 10000);