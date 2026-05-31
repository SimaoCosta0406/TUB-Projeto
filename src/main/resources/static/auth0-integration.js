///////////////////////// GESTÃO DE ACESSOS E LOGIN COM AUTH0 ///////////////////////////

let auth0Client = null;

// Inicializar Auth0
async function initAuth0() {
    auth0Client = await auth0.createAuth0Client({
        domain: 'tub-projeto.eu.auth0.com',
        clientId: 'JxESMdB7WlvIaNyzdKKpewpTYtix0iAM',
        authorizationParams: {
            redirect_uri: window.location.origin + '/login.html',
            audience: 'https://api.tub.pt'
        }
    });

    // Verificar se voltou do Auth0 (callback)
    const query = window.location.search;
    if (query.includes('code=') && query.includes('state=')) {
        try {
            await auth0Client.handleRedirectCallback();
            window.history.replaceState({}, document.title, '/login.html');
            await processarLogin();
        } catch (erro) {
            console.error('Erro no callback do Auth0:', erro);
            const erroMsg = document.getElementById('erro-msg');
            if (erroMsg) erroMsg.style.display = 'block';
        }
    }

    // Verificar se já está autenticado
    const isAuth = await auth0Client.isAuthenticated();
    if (isAuth) {
        await processarLogin();
    }
}

// Processar login após autenticação bem-sucedida
async function processarLogin() {
    try {
        const user = await auth0Client.getUser();
        const token = await auth0Client.getTokenSilently();
        
        console.log('Utilizador autenticado:', user);
        
        // Extrair role do token
        const roles = user['https://api.tub.pt/roles'] || [];

        // Determinar role (prioridade: ADMIN > SUPERVISOR > WORKER > USER)
        let role = 'USER'; // default
            if (roles.includes('ADMIN')) {
                role = 'ADMIN';
            } else if (roles.includes('SUPERVISOR')) {
                role = 'SUPERVISOR';
            } else if (roles.includes('WORKER')) {
                role = 'WORKER';
}
        
        // Criar objeto utilizador compatível com sistema existente
        const utilizador = {
            nome: user.name || user.email,
            email: user.email,
            role: role
        };
        
        // Guardar no localStorage (compatível com script.js existente)
        localStorage.setItem('utilizadorAtivo', JSON.stringify(utilizador));
        localStorage.setItem('auth0_token', token);
        
        console.log('Utilizador guardado:', utilizador);
        
        // Redirecionar conforme role
        if (role === 'ADMIN') {
            window.location.href = 'mapa.html'; // ADMIN vê tudo (incluindo menu ALARMES)
        } else if (role === 'SUPERVISOR') {
            window.location.href = 'supervisor.html'; // SUPERVISOR só vê alarmes
        } else if (role === 'WORKER') {
            window.location.href = 'worker.html'; // WORKER envia alarmes
        } else {
            window.location.href = 'mapa.html'; // USER vê mapa
        }

    } catch (erro) {
        console.error('Erro ao processar login:', erro);
        const erroMsg = document.getElementById('erro-msg');
        if (erroMsg) erroMsg.style.display = 'block';
    }
}

// Função de login com Auth0
async function loginComAuth0() {
    try {
        await auth0Client.loginWithRedirect({
            authorizationParams: {
                prompt: 'login'
            }
        });
    } catch (erro) {
        console.error('Erro ao iniciar login:', erro);
        const erroMsg = document.getElementById('erro-msg');
        if (erroMsg) erroMsg.style.display = 'block';
    }
}


// Função de logout atualizada para Auth0
async function fazerLogoutAuth0() {
    localStorage.removeItem('utilizadorAtivo');
    localStorage.removeItem('auth0_token');
    
    if (auth0Client) {
        await auth0Client.logout({
            logoutParams: {
                returnTo: window.location.origin + '/index.html'
            }
        });
    } else {
        window.location.href = 'index.html';
    }
}

// Inicializar quando carregar a página de login
document.addEventListener('DOMContentLoaded', function() {
    const btnLogin = document.getElementById('btn-login-auth0');
    if (btnLogin) {
        initAuth0().then(() => {
            btnLogin.addEventListener('click', loginComAuth0);
        }).catch(erro => {
            console.error('Erro ao inicializar Auth0:', erro);
        });
    }
});

console.log('Auth0 Integration carregado com sucesso!');
