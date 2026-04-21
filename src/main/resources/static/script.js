// Smooth scroll (mantido para compatibilidade, mas navegação agora usa mostrarSecao)
document.querySelectorAll('a[href^="#"], a[href^="index.html#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        const targetId = this.getAttribute('href').split('#')[1];
        const target = document.getElementById(targetId);
        if (target) {
            e.preventDefault();
            target.scrollIntoView({ behavior: 'smooth' });
        }
    });
});

console.log('Website TUB Projeto carregado com sucesso!');

// Botão "Começar"
const btnComecar = document.querySelector('.btn3');
if (btnComecar) {
    btnComecar.addEventListener('click', function() {
        alert('Conseguiste');
    });
}

///////////////////////////////MAPA DE BRAGA///////////////////////////
document.addEventListener("DOMContentLoaded", function () {
    verificarAcessos();

    const mapContainer = document.getElementById("map");
    if (mapContainer) {
        const map = L.map("map").setView([41.5454, -8.4265], 13);

        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            attribution: "&copy; OpenStreetMap contributors"
        }).addTo(map);

        let marcadores = [];
        let stopsGuardados = [];

        function limparMarcadores() {
            marcadores.forEach(marcador => map.removeLayer(marcador));
            marcadores = [];
        }

        async function carregarStops() {
            try {
                const resposta = await fetch("/api/stops");
                const stops = await resposta.json();

                stopsGuardados = stops;
                limparMarcadores();

                stops.forEach(stop => {
                    if (stop.latitude != null && stop.longitude != null) {
                        const utilizadorGuardado = localStorage.getItem('utilizadorAtivo');
                        let popupContent = `<strong>${stop.name}</strong>`;

                        if (utilizadorGuardado) {
                            const utilizador = JSON.parse(utilizadorGuardado);
                            if (utilizador.role === 'ADMIN') {
                                popupContent += `
                                    <br>Latitude: ${stop.latitude}
                                    <br>Longitude: ${stop.longitude}
                                    <br><br>
                                    <button onclick="preencherFormularioParagem(${stop.id}, '${stop.name.replace(/'/g, "\\'")}', ${stop.latitude}, ${stop.longitude})">
                                        Editar
                                    </button>
                                    <button onclick="apagarParagem(${stop.id})">
                                        Apagar
                                    </button>
                                `;
                            }
                        }

                        const marcador = L.marker([stop.latitude, stop.longitude])
                            .addTo(map)
                            .bindPopup(popupContent);
                        marcadores.push(marcador);
                    }
                });
            } catch (erro) {
                console.error("Erro ao carregar stops do mapa:", erro);
            }
        }

        function pesquisarParagem() {
            const input = document.getElementById("searchStop");
            if (!input) return;
            const termo = input.value.trim().toLowerCase();
            if (!termo) { alert("Escreve o nome de uma paragem."); return; }

            const stopEncontrado = stopsGuardados.find(stop =>
                stop.name &&
                stop.latitude != null &&
                stop.longitude != null &&
                stop.name.toLowerCase().includes(termo)
            );

            if (stopEncontrado) {
                map.setView([stopEncontrado.latitude, stopEncontrado.longitude], 16);
                L.popup()
                    .setLatLng([stopEncontrado.latitude, stopEncontrado.longitude])
                    .setContent(`<strong>${stopEncontrado.name}</strong>`)
                    .openOn(map);
            } else {
                alert("Paragem não encontrada.");
            }
        }

        carregarStops();
        setInterval(carregarStops, 5000);

        const botaoPesquisa = document.getElementById("searchBtn");
        const inputPesquisa = document.getElementById("searchStop");
        if (botaoPesquisa) botaoPesquisa.addEventListener("click", pesquisarParagem);
        if (inputPesquisa) inputPesquisa.addEventListener("keydown", function (e) {
            if (e.key === "Enter") pesquisarParagem();
        });
    }
});
/////////////////////////////////////////////FIM MAPA DE BRAGA//////////////////////////////////////////

///////////////////////// GESTÃO DE ACESSOS E LOGIN ///////////////////////////

async function validarLogin() {
    const emailInput = document.getElementById('emailInput');
    if (!emailInput) return;

    const email = emailInput.value;
    let ficheiroParaLer = "";

    if (email === 'admin@tub.pt') {
        ficheiroParaLer = 'teste_admin.json';
    } else if (email === 'user@tub.pt') {
        ficheiroParaLer = 'teste_utilizador.json';
    } else {
        document.getElementById('erro-msg').style.display = 'block';
        return;
    }

    try {
        const resposta = await fetch(ficheiroParaLer);
        const dadosUtilizador = await resposta.json();
        localStorage.setItem('utilizadorAtivo', JSON.stringify(dadosUtilizador));
        document.getElementById('erro-msg').style.display = 'none';
        window.location.href = 'mapa.html';
    } catch (erro) {
        console.error("Erro ao ler o ficheiro JSON:", erro);
        alert("Erro no login! Confirma se os ficheiros de teste estão na pasta.");
    }
}

function fazerLogout() {
    localStorage.removeItem('utilizadorAtivo');
    window.location.href = 'index.html';
}

function verificarAcessos() {
    const utilizadorGuardado = localStorage.getItem('utilizadorAtivo');
    const navLogin   = document.getElementById('nav-login');
    const navLogout  = document.getElementById('nav-logout');
    const navAlarmes = document.getElementById('nav-alarmes');
    const navMonitorizacao = document.getElementById('nav-monitorizacao');

    if (utilizadorGuardado) {
        if (navLogin)  navLogin.style.display  = 'none';
        if (navLogout) navLogout.style.display = 'inline-block';

        const utilizador = JSON.parse(utilizadorGuardado);
        if (utilizador.role === 'ADMIN') {
            if (navAlarmes) navAlarmes.style.display = 'inline-block';
            if (navMonitorizacao) navMonitorizacao.style.display = 'inline-block';
        }
    } else {
        if (navLogin)  navLogin.style.display  = 'inline-block';
        if (navLogout) navLogout.style.display = 'none';
        if (navAlarmes) navAlarmes.style.display = 'none';
        if (navMonitorizacao) navMonitorizacao.style.display = 'none';
    }
}

///////////////////// Funções admin — paragens /////////////////////
function preencherFormularioParagem(id, name, latitude, longitude) {
    document.getElementById("stopId").value        = id;
    document.getElementById("stopName").value      = name;
    document.getElementById("stopLatitude").value  = latitude;
    document.getElementById("stopLongitude").value = longitude;
}

function limparFormularioParagem() {
    document.getElementById("stopId").value        = "";
    document.getElementById("stopName").value      = "";
    document.getElementById("stopLatitude").value  = "";
    document.getElementById("stopLongitude").value = "";
}

async function adicionarOuEditarParagem() {
    const id        = document.getElementById("stopId").value;
    const name      = document.getElementById("stopName").value.trim();
    const latitude  = parseFloat(document.getElementById("stopLatitude").value);
    const longitude = parseFloat(document.getElementById("stopLongitude").value);

    if (!name || isNaN(latitude) || isNaN(longitude)) {
        alert("Preenche nome, latitude e longitude.");
        return;
    }

    const payload = { name, latitude, longitude };
    const url    = id ? `/api/stops/${id}` : "/api/stops";
    const method = id ? "PUT" : "POST";

    try {
        const resposta = await fetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if (resposta.ok) {
            alert(id ? "Paragem atualizada com sucesso." : "Paragem adicionada com sucesso.");
            limparFormularioParagem();
            location.reload();
        } else {
            alert("Erro ao guardar a paragem.");
        }
    } catch (erro) {
        console.error("Erro ao guardar paragem:", erro);
        alert("Erro de ligação ao servidor.");
    }
}

async function apagarParagem(id) {
    if (!confirm("Tens a certeza que queres apagar esta paragem?")) return;

    try {
        const resposta = await fetch(`/api/stops/${id}`, { method: "DELETE" });
        if (resposta.ok) {
            alert("Paragem apagada com sucesso.");
            location.reload();
        } else {
            alert("Erro ao apagar paragem.");
        }
    } catch (erro) {
        console.error("Erro ao apagar paragem:", erro);
        alert("Erro de ligação ao servidor.");
    }
}

///////////////////// Navegação por secções (index.html) /////////////////////

// Secções que existem no index.html e podem ser mostradas/escondidas
const SECCOES_INDEX = ['home', 'about', 'contact', 'alarmes'];

function mostrarSecao(idAlvo) {
    // Esconde todas as secções geridas
    SECCOES_INDEX.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.style.display = 'none';
    });

    // Mostra a secção pedida
    const seccaoAtiva = document.getElementById(idAlvo);
    if (seccaoAtiva) {
        // 'home' é uma section.hero, as outras são sections normais
        seccaoAtiva.style.display = 'block';
        window.scrollTo({ top: 0, behavior: 'smooth' });

        if (idAlvo === 'alarmes') {
            carregarDadosAlarmes();
        }
    }
}

// Inicialização: garante que home, about e contact estão visíveis ao carregar
document.addEventListener("DOMContentLoaded", function () {
    // Só faz a lógica de secções se estivermos no index.html
    const eIndexPage = !!document.getElementById('home');
    if (!eIndexPage) return;

    // Por defeito mostra as três secções de scroll normais
    // e esconde alarmes (já tem display:none no HTML)
    ['home', 'about', 'contact'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.style.display = 'block';
    });
});

///////////////////// Alarmes /////////////////////

async function carregarDadosAlarmes() {
    try {
        const resStats = await fetch('/api/alerts/stats');
        const stats = await resStats.json();
        document.getElementById('count-high').innerText   = stats.HIGH   || 0;
        document.getElementById('count-medium').innerText = stats.MEDIUM || 0;
        document.getElementById('count-low').innerText    = stats.LOW    || 0;

        const resAlerts = await fetch('/api/alerts');
        const alerts = await resAlerts.json();
        const body = document.getElementById('alerts-body');
        body.innerHTML = '';

        alerts.forEach(alert => {
            body.innerHTML += `
                <tr>
                    <td>${alert.id}</td>
                    <td>${alert.type}</td>
                    <td class="severity-${alert.severity.toLowerCase()}">${alert.severity}</td>
                    <td>${alert.status}</td>
                    <td>
                        <button onclick="resolverAlerta(${alert.id})" class="btn-acao">✅</button>
                        <button onclick="marcarInconsistente(${alert.id})" class="btn-acao">⚠️</button>
                    </td>
                </tr>
            `;
        });
    } catch (err) {
        console.error("Erro ao carregar alarmes:", err);
    }
}

async function resolverAlerta(id) {
    const user = JSON.parse(localStorage.getItem('utilizadorAtivo'));
    await fetch(`/api/alerts/${id}/resolve?username=${user.nome}`, { method: 'POST' });
    carregarDadosAlarmes();
}

async function marcarInconsistente(id) {
    const user = JSON.parse(localStorage.getItem('utilizadorAtivo'));
    await fetch(`/api/alerts/${id}/inconsistent?username=${user.nome}`, { method: 'POST' });
    carregarDadosAlarmes();
}