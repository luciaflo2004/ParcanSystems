// ====== Lógica de la interfaz de Cargos ======
let cargosData = [];
let editingCargo = null;

// Aplica filtros (texto, estado) y renderiza la tabla
function renderCargos() {
    const texto = (document.getElementById('search-cargos').value || '').toLowerCase();
    const estadoF = document.getElementById('filtro-estado').value;

    const rows = cargosData.filter(c => {
        if (texto && !(c.descripcion || '').toLowerCase().includes(texto)) return false;
        const estado = c.eliminado ? 'inactivo' : 'activo';
        if (estadoF !== 'todos' && estado !== estadoF) return false;
        return true;
    }).map(c => ({
        Cod: c.codCargo,
        Descripcion: c.descripcion,
        Estado: c.eliminado ? 'Inactivo' : 'Activo'
    }));

    renderTable('cargos-list', ['Cod','Descripcion','Estado'], rows, (row) => {
        let html = `<button class="btn btn-outline btn-sm" onclick="editarCargo(${row.Cod})"><i class="fa-solid fa-pen"></i> Editar</button>`;
        if (row.Estado === 'Inactivo') html += `<button class="btn btn-success btn-sm" onclick="activarCargo(${row.Cod})"><i class="fa-solid fa-toggle-on"></i> Activar</button>`;
        else html += `<button class="btn btn-danger btn-sm" onclick="desactivarCargo(${row.Cod})"><i class="fa-solid fa-toggle-off"></i> Desactivar</button>`;
        return html;
    });
}

// Cargar la lista de cargos (global para refrescarla desde otras funciones)
async function loadCargos() {
    cargosData = await fetchAll('/api/cargos');
    renderCargos();
}

// Limpia los filtros de la tabla de cargos
function clearFiltrosCargos() {
    document.getElementById('search-cargos').value = '';
    document.getElementById('filtro-estado').value = 'todos';
    renderCargos();
}

function editarCargo(cod) {
    const c = cargosData.find(x => x.codCargo === cod);
    if (!c) return;
    editingCargo = cod;
    document.getElementById('descripcion').value = c.descripcion || '';
    document.getElementById('form-title').textContent = 'Editar Cargo';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Actualizar';
    document.getElementById('cargo-form').scrollIntoView({ behavior: 'smooth' });
}

function resetCargoForm() {
    editingCargo = null;
    document.getElementById('cargo-form').reset();
    document.getElementById('form-title').textContent = 'Registrar Cargo';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar';
}

async function desactivarCargo(cod) {
    if (!confirm('¿Está seguro que desea desactivar este cargo?')) return;
    const res = await fetch('/api/cargos/' + cod, { method: 'DELETE' });
    if (res.ok) { showAlert('Cargo desactivado', 'success'); loadCargos(); }
    else showAlert('Error al desactivar el cargo', 'error');
}

async function activarCargo(cod) {
    const res = await fetch('/api/cargos/' + cod + '/restore', { method: 'POST' });
    if (res.ok) { showAlert('Cargo activado', 'success'); loadCargos(); }
    else showAlert('Error al activar el cargo', 'error');
}

document.addEventListener('DOMContentLoaded', async function() {
    loadCargos();

    // Filtrar al escribir o cambiar seleccion
    document.getElementById('search-cargos').addEventListener('input', renderCargos);
    document.getElementById('filtro-estado').addEventListener('change', renderCargos);

    document.getElementById('cargo-form').addEventListener('submit', async function(e) {
        e.preventDefault();
        const fd = new FormData(this);
        const obj = {};
        fd.forEach((v, k) => obj[k] = v);
        const url = editingCargo ? ('/api/cargos/' + editingCargo) : '/api/cargos';
        const method = editingCargo ? 'PUT' : 'POST';
        const res = await fetch(url, { method: method, headers:{'Content-Type':'application/json'}, body: JSON.stringify(obj) });
        if (res.ok) {
            showAlert(editingCargo ? 'Cargo actualizado' : 'Cargo registrado', 'success');
            this.reset();
            editingCargo = null;
            document.getElementById('form-title').textContent = 'Registrar Cargo';
            document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar';
            loadCargos();
        } else showAlert('Error al guardar el cargo', 'error');
    });
});
