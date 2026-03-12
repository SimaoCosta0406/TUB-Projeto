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
document.querySelector('.btn1').addEventListener('click', function() {
    alert('Entraste na ilha do Epstein com sucesso');
});