async function carregarDadosOcupacao() {
    try {
        // Fetch panels
        const panelsResponse = await fetch('/api/panels');
        const panels = await panelsResponse.json();
        const panelMap = {};
        panels.forEach(panel => {
            panelMap[panel.id] = panel.location;
        });

        // Fetch all passenger counts
        const countsResponse = await fetch('/api/passengers/all');
        const counts = await countsResponse.json();

        // Group by panelId and get the latest
        const latestCounts = {};
        counts.forEach(count => {
            if (!latestCounts[count.panelId] || new Date(count.timestamp) > new Date(latestCounts[count.panelId].timestamp)) {
                latestCounts[count.panelId] = count;
            }
        });

        const tabela = document.getElementById('tabela-ocupacao');
        tabela.innerHTML = ''; // Limpar antes de atualizar

        Object.values(latestCounts).forEach(count => {
            const location = panelMap[count.panelId] || `Painel ${count.panelId}`;
            tabela.innerHTML += `
                <tr>
                    <td>${location} (ID: ${count.panelId})</td>
                    <td>${count.line}</td>
                    <td>${count.entryCount}</td>
                    <td>${count.exitCount}</td>
                    <td><b>${count.occupancy}</b></td>
                </tr>
            `;
        });

        // Hide alert if successful
        document.getElementById('alerta-sensor').style.display = 'none';
    } catch (error) {
        console.error('Erro ao carregar dados:', error);
        // Falha na receção
        document.getElementById('alerta-sensor').style.display = 'block';
    }
}

// Carregar dados iniciais
carregarDadosOcupacao();

// Atualiza a cada 10 segundos
setInterval(carregarDadosOcupacao, 10000);