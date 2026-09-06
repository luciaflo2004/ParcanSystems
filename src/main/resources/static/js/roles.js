// ====== Lógica de la interfaz de Roles ======
let rolesData = [];
let editingRol = null;

// Aplica filtros (texto, estado) y renderiza la tabla
function renderRoles() {
    const texto = (document.getElementById('search-roles').value || '').toLowerCase();
    const estadoF = document.getElementById('filtro-estado').value;

    const rows = rolesData.filter(r => {
        if (texto && !(r.descripcionRoll || '').toLowerCase().includes(texto)) return false;
        const estado = r.eliminado ? 'inactivo' : 'activo';
        if (estadoF !== 'todos' && estado !== estadoF) return false;
        return true;
    }).map(r => ({
        ID: r.idRoles,
        Descripcion: r.descripcionRoll,
        Estado: r.eliminado ? 'Inactivo' : 'Activo'
    }));

    renderTable('roles-list', ['ID','Descripcion','Estado'], rows, (row) => {
        let html = `<button class="btn btn-outline btn-sm" onclick="editarRol(${row.ID})"><i class="fa-solid fa-pen"></i> Editar</button>`;
        if (row.Estado === 'Inactivo') html += `<button class="btn btn-success btn-sm" onclick="activarRol(${row.ID})"><i class="fa-solid fa-toggle-on"></i> Activar</button>`;
        else html += `<button class="btn btn-danger btn-sm" onclick="desactivarRol(${row.ID})"><i class="fa-solid fa-toggle-off"></i> Desactivar</button>`;
        return html;
    });
}

// Cargar la lista de roles (global para refrescarla desde otras funciones)
async function loadRoles() {
    rolesData = await fetchAll('/api/roles');
    renderRoles();
}

// Limpia los filtros de la tabla de roles
function clearFiltrosRoles() {
    document.getElementById('search-roles').value = '';
    document.getElementById('filtro-estado').value = 'todos';
    renderRoles();
}

function editarRol(id) {
    const r = rolesData.find(x => x.idRoles === id);
    if (!r) return;
    editingRol = id;
    document.getElementById('descripcionRoll').value = r.descripcionRoll || '';
    document.getElementById('form-title').textContent = 'Editar Rol';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Actualizar';
    document.getElementById('rol-form').scrollIntoView({ behavior: 'smooth' });
}

function resetRolForm() {
    editingRol = null;
    document.getElementById('rol-form').reset();
    document.getElementById('form-title').textContent = 'Registrar Rol';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar';
}

async function desactivarRol(id) {
    if (!confirm('¿Está seguro que desea desactivar este rol?')) return;
    const res = await fetch('/api/roles/' + id, { method: 'DELETE' });
    if (res.ok) { showAlert('Rol desactivado', 'success'); loadRoles(); }
    else showAlert('Error al desactivar el rol', 'error');
}

async function activarRol(id) {
    const res = await fetch('/api/roles/' + id + '/restore', { method: 'POST' });
    if (res.ok) { showAlert('Rol activado', 'success'); loadRoles(); }
    else showAlert('Error al activar el rol', 'error');
}

document.addEventListener('DOMContentLoaded', async function() {
    loadRoles();

    // Filtrar al escribir o cambiar seleccion
    document.getElementById('search-roles').addEventListener('input', renderRoles);
    document.getElementById('filtro-estado').addEventListener('change', renderRoles);

    document.getElementById('rol-form').addEventListener('submit', async function(e) {
        e.preventDefault();
        const fd = new FormData(this);
        const obj = {};
        fd.forEach((v, k) => obj[k] = v);
        const url = editingRol ? ('/api/roles/' + editingRol) : '/api/roles';
        const method = editingRol ? 'PUT' : 'POST';
        const res = await fetch(url, { method: method, headers:{'Content-Type':'application/json'}, body: JSON.stringify(obj) });
        if (res.ok) {
            showAlert(editingRol ? 'Rol actualizado' : 'Rol registrado', 'success');
            this.reset();
            editingRol = null;
            document.getElementById('form-title').textContent = 'Registrar Rol';
            document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar';
            loadRoles();
        } else showAlert('Error al guardar el rol', 'error');
    });
});
