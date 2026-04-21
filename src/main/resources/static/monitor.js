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

        // Group by panelId and calculate total occupancy
        const occupancyByPanel = {};
        counts.forEach(count => {
            if (!occupancyByPanel[count.panelId]) {
                occupancyByPanel[count.panelId] = {
                    location: panelMap[count.panelId] || `Painel ${count.panelId}`,
                    line: count.line,
                    totalOccupancy: 0
                };
            }
            occupancyByPanel[count.panelId].totalOccupancy += count.entryCount - count.exitCount;
        });

        const tabela = document.getElementById('tabela-ocupacao');
        tabela.innerHTML = ''; // Limpar antes de atualizar

        Object.values(occupancyByPanel).forEach(panel => {
            tabela.innerHTML += `
                <tr>
                    <td>${panel.location} (ID: ${panel.panelId})</td>
                    <td>${panel.line}</td>
                    <td>-</td>
                    <td>-</td>
                    <td><b>${panel.totalOccupancy}</b></td>
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

// Atualiza a cada 2 segundos
setInterval(carregarDadosOcupacao, 2000);