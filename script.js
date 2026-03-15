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