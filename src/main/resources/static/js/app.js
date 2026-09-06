// ====== Funciones compartidas de la aplicacion ======

// URL base de la API (vacia porque es relativa)
const API = '';

// HTML del menu lateral que se inyecta en todas las paginas
const sidebarHTML = `<nav class="sidebar-nav">
<div class="sidebar-brand"><i class="fa-solid fa-place-of-worship"></i><div><h2>SystemParCan</h2><p>Parroquia de Katuete</p></div></div>
<ul class="nav-list">
<li><a href="index.html"><i class="fa-solid fa-house"></i> Inicio</a></li>
<li><a href="personas.html"><i class="fa-solid fa-users"></i> Personas</a></li>
<li><a href="funcionarios.html"><i class="fa-solid fa-user-tie"></i> Funcionarios</a></li>
<li><a href="cargos.html"><i class="fa-solid fa-briefcase"></i> Cargos</a></li>
<li><a href="usuarios.html"><i class="fa-solid fa-user-shield"></i> Usuarios</a></li>
<li><a href="roles.html"><i class="fa-solid fa-key"></i> Roles</a></li>
<li class="nav-section">Solicitudes y Servicios</li>
<li><a href="solicitudes.html"><i class="fa-solid fa-file-pen"></i> Solicitudes</a></li>
<li><a href="servicios.html"><i class="fa-solid fa-bell-concierge"></i> Servicios</a></li>
<li class="nav-section">Configuracion</li>
<li><a href="instituciones.html"><i class="fa-solid fa-church"></i> Instituciones Eclesiasticas</a></li>
</ul></nav>`;

document.addEventListener('DOMContentLoaded', function() {
    if (!document.querySelector('.sidebar-nav')) {
        document.body.insertAdjacentHTML('afterbegin', sidebarHTML);
    }
    highlightActive();
});

function highlightActive() {
    const path = window.location.pathname.split('/').pop();
    document.querySelectorAll('.nav-list a').forEach(a => {
        a.classList.remove('active');
        if (a.getAttribute('href') === path) a.classList.add('active');
    });
}

async function fetchAll(url) {
    try {
        const res = await fetch(API + url);
        return await res.json();
    } catch(e) {
        console.error('Error fetching:', e);
        return [];
    }
}

function showAlert(msg, type) {
    const div = document.createElement('div');
    div.className = type === 'success' ? 'alert-success' : 'alert-error';
    div.textContent = msg;
    const content = document.querySelector('.content');
    content.insertBefore(div, content.firstChild);
    setTimeout(() => div.remove(), 3000);
}

function bindSearch(inputId, containerId) {
    const input = document.getElementById(inputId);
    if (!input) return;
    input.addEventListener('input', function () {
        const q = this.value.trim().toLowerCase();
        const rows = document.querySelectorAll('#' + containerId + ' tbody tr');
        rows.forEach(r => {
            r.style.display = (!q || r.textContent.toLowerCase().includes(q)) ? '' : 'none';
        });
    });
}

function clearSearch(inputId, containerId) {
    const input = document.getElementById(inputId);
    if (input) input.value = '';
    document.querySelectorAll('#' + containerId + ' tbody tr').forEach(r => r.style.display = '');
}

function renderTable(containerId, headers, rows, actions) {
    const container = document.getElementById(containerId);
    if (!rows || rows.length === 0) {
        container.innerHTML = '<div class="no-data"><span>&#128196;</span><br>No hay datos registrados</div>';
        return;
    }
    let html = '<table><thead><tr>';
    headers.forEach(h => html += '<th>' + h + '</th>');
    if (actions) html += '<th class="actions-col">Acciones</th>';
    html += '</tr></thead><tbody>';
    rows.forEach(row => {
        html += '<tr>';
        headers.forEach(h => {
            const val = row[h];
            html += '<td>' + (val != null ? val : '-') + '</td>';
        });
        if (actions) html += '<td class="actions-col">' + actions(row) + '</td>';
        html += '</tr>';
    });
    html += '</tbody></table>';
    container.innerHTML = html;
}
