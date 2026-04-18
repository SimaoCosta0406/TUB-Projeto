// Smooth scroll 
document.querySelectorAll('a[href^="#"], a[href^="index.html#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        // Extrair apenas o ID da secção (ex: tira o "index.html" e deixa só "#about")
        const targetId = this.getAttribute('href').split('#')[1];
        const target = document.getElementById(targetId);
        
        if (target) {
            e.preventDefault();
            target.scrollIntoView({
                behavior: 'smooth'
            });
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
                        const marcador = L.marker([stop.latitude, stop.longitude])
                            .addTo(map)
                            .bindPopup(stop.name);

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

            if (!termo) {
                alert("Escreve o nome de uma paragem.");
                return;
            }

            const stopEncontrado = stopsGuardados.find(stop =>
                stop.name &&
                stop.latitude != null &&
                stop.longitude != null &&
                stop.name.toLowerCase().includes(termo)
            );

            if (stopEncontrado) {
                map.setView([stopEncontrado.latitude, stopEncontrado.longitude], 16);

                const popup = L.popup()
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

        if (botaoPesquisa) {
            botaoPesquisa.addEventListener("click", pesquisarParagem);
        }

        if (inputPesquisa) {
            inputPesquisa.addEventListener("keydown", function (event) {
                if (event.key === "Enter") {
                    pesquisarParagem();
                }
            });
        }
    }
});
/////////////////////////////////////////////FIM MAPA DE braga//////////////////////////////////////////

///////////////////////// GESTÃO DE ACESSOS E LOGIN COM JSON ///////////////////////////

async function validarLogin() {
    const emailInput = document.getElementById('emailInput');
    if (!emailInput) return; // Garante que estamos na página de login
    
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

        //Redireciona para a página principal após o login
        window.location.href = 'index.html'; 
        
    } catch (erro) {
        console.error("Erro ao ler o ficheiro JSON:", erro);
        alert("Erro no login! Confirma se os ficheiros de teste estão na pasta.");
    }
}

function fazerLogout() {
    localStorage.removeItem('utilizadorAtivo');
    //Redireciona para a página principal ao sair
    window.location.href = 'index.html';
}

function verificarAcessos() {
    const utilizadorGuardado = localStorage.getItem('utilizadorAtivo');
    
    const navLogin = document.getElementById('nav-login');
    const navLogout = document.getElementById('nav-logout');
    const formLogin = document.getElementById('login-form');
    const msgSucesso = document.getElementById('login-success');
    const boasVindas = document.getElementById('mensagem-boasvindas');
    const seccaoContagem = document.getElementById('contagem');

    if (utilizadorGuardado) {
        const utilizador = JSON.parse(utilizadorGuardado);
        
        if(navLogin) navLogin.style.display = 'none';
        if(navLogout) navLogout.style.display = 'inline-block';
        if(formLogin) formLogin.style.display = 'none';
        if(msgSucesso) msgSucesso.style.display = 'block';
        if(boasVindas) boasVindas.innerText = `Olá, ${utilizador.nome}!`;

        if (utilizador.role === 'ADMIN') {
            if(seccaoContagem) seccaoContagem.style.display = 'block'; 
        } else {
            if(seccaoContagem) seccaoContagem.style.display = 'none';  
        }
        
    } else {
        if(navLogin) navLogin.style.display = 'inline-block';
        if(navLogout) navLogout.style.display = 'none';
        if(formLogin) formLogin.style.display = 'block';
        if(msgSucesso) msgSucesso.style.display = 'none';
        if(seccaoContagem) seccaoContagem.style.display = 'none'; 
        
        const emailInput = document.getElementById('emailInput');
        if(emailInput) emailInput.value = '';
        const erroMsg = document.getElementById('erro-msg');
        if(erroMsg) erroMsg.style.display = 'none';
    }
}