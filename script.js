// Smooth scroll para os links de navegação
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth'
            });
        }
    });
});

// Log quando o página carrega
console.log('Website TUB Projeto carregado com sucesso!');

// Botão "Começar"
document.querySelector('.btn3')?.addEventListener('click', function() {
    alert('Conseguiste');
});

///////////////////////////////MAPA DE BRAGA///////////////////////////
document.addEventListener("DOMContentLoaded", function () {

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

});
/////////////////////////////////////////////FIM MAPA DE braga//////////////////////////////////////////

///////////////////////// GESTÂO DE ACESSOS E LOGIN COM JSON///////////////////////////

// Corre também no início para verificar se já há alguém logado
document.addEventListener("DOMContentLoaded", function () {
    verificarAcessos();
});

// Lê os ficheiros JSON
async function validarLogin() {
    const email = document.getElementById('emailInput').value;
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

        verificarAcessos();
        window.location.hash = '#home'; 
        
    } catch (erro) {
        console.error("Erro ao ler o ficheiro JSON:", erro);
        alert("Erro no login! Confirma se os ficheiros 'teste_admin.json' e 'teste_utilizador.json' estão na pasta.");
    }
}

function fazerLogout() {
    localStorage.removeItem('utilizadorAtivo');
    verificarAcessos();
    window.location.hash = '#home';
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
        
        // Esconder e mostrar elementos se tiver logado
        if(navLogin) navLogin.style.display = 'none';
        if(navLogout) navLogout.style.display = 'inline-block';
        if(formLogin) formLogin.style.display = 'none';
        if(msgSucesso) msgSucesso.style.display = 'block';
        if(boasVindas) boasVindas.innerText = `Olá, ${utilizador.nome}!`;

        // PERFIS (Admin vê contagem, User não)
        if (utilizador.role === 'ADMIN') {
            if(seccaoContagem) seccaoContagem.style.display = 'block'; 
        } else {
            if(seccaoContagem) seccaoContagem.style.display = 'none';  
        }
        
    } else {
        // Ninguém logado
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