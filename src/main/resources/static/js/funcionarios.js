// ====== Lógica de la interfaz de Funcionarios ======
let funcionariosData = [];

// Aplica filtros (texto, estado) y renderiza la tabla
function renderFuncionarios() {
    const texto = (document.getElementById('search-funcionarios').value || '').toLowerCase();
    const estadoF = document.getElementById('filtro-estado').value;

    const rows = funcionariosData.filter(f => {
        const bloque = [
            String(f.ciPersona),
            (f.persona ? (f.persona.nombrePersona || '') + ' ' + (f.persona.apellidoPersona || '') : ''),
            (f.cargo ? f.cargo.descripcion : '')
        ].join(' ').toLowerCase();
        if (texto && !bloque.includes(texto)) return false;
        const estado = f.eliminado ? 'inactivo' : 'activo';
        if (estadoF !== 'todos' && estado !== estadoF) return false;
        return true;
    }).map(f => ({
        ciPersona: f.ciPersona,
        Persona: (f.persona ? (f.persona.nombrePersona || '') + ' ' + (f.persona.apellidoPersona || '') : ''),
        Cargo: (f.cargo ? f.cargo.descripcion : '-'),
        Estado: f.eliminado ? 'Inactivo' : 'Activo'
    }));

    renderTable('funcionarios-list', ['ciPersona','Persona','Cargo','Estado'], rows, (row) => {
        let html = `<a class="btn btn-outline btn-sm" href="personas.html"><i class="fa-solid fa-pen"></i> Editar</a>`;
        if (row.Estado === 'Inactivo') {
            html += `<button class="btn btn-success btn-sm" onclick="activarFuncionario(${row.ciPersona})"><i class="fa-solid fa-toggle-on"></i> Activar</button>`;
        } else {
            html += `<button class="btn btn-danger btn-sm" onclick="desactivarFuncionario(${row.ciPersona})"><i class="fa-solid fa-toggle-off"></i> Desactivar</button>`;
        }
        return html;
    });
}

// Cargar la lista de funcionarios (global para refrescarla desde otras funciones)
async function loadFuncionarios() {
    funcionariosData = await fetchAll('/api/funcionarios');
    renderFuncionarios();
}

// Limpia los filtros de la tabla de funcionarios
function clearFiltrosFuncionarios() {
    document.getElementById('search-funcionarios').value = '';
    document.getElementById('filtro-estado').value = 'todos';
    renderFuncionarios();
}

// Acciones de la lista
async function desactivarFuncionario(ci) {
    if (!confirm('¿Está seguro que desea desactivar este funcionario?')) return;
    const res = await fetch('/api/funcionarios/' + ci, { method: 'DELETE' });
    if (res.ok) { showAlert('Funcionario desactivado', 'success'); loadFuncionarios(); }
    else showAlert('Error al desactivar el funcionario', 'error');
}

async function activarFuncionario(ci) {
    const res = await fetch('/api/funcionarios/' + ci + '/restore', { method: 'POST' });
    if (res.ok) { showAlert('Funcionario activado', 'success'); loadFuncionarios(); }
    else showAlert('Error al activar el funcionario', 'error');
}

document.addEventListener('DOMContentLoaded', async function() {
    loadFuncionarios();
    // Filtrar al escribir o cambiar seleccion
    document.getElementById('search-funcionarios').addEventListener('input', renderFuncionarios);
    document.getElementById('filtro-estado').addEventListener('change', renderFuncionarios);
});

