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
    // 1. Verificar acessos em todas as páginas
    verificarAcessos();

    // 2. Correr o mapa apenas se estivermos na página do mapa
    const mapContainer = document.getElementById("map");
    if (mapContainer) {
        const map = L.map("map").setView([41.5454, -8.4265], 13);

        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            attribution: "&copy; OpenStreetMap contributors"
        }).addTo(map);

        const panels = [
            { id: 1, location: "Avenida Central", lat: 41.5501, lon: -8.4213 },
            { id: 2, location: "Universidade do Minho", lat: 41.5607, lon: -8.3975 },
            { id: 3, location: "Braga Parque", lat: 41.5582, lon: -8.4048 },
            { id: 4, location: "Estação de Braga", lat: 41.5471, lon: -8.4343 },
            { id: 5, location: "Hospital de Braga", lat: 41.5618, lon: -8.3996 },
            { id: 6, location: "Bom Jesus", lat: 41.5547, lon: -8.3772 },
        ];

        panels.forEach(panel => {
            L.marker([panel.lat, panel.lon])
                .addTo(map)
                .bindPopup(panel.location);
        });
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