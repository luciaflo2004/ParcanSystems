// ====== Lógica de la interfaz de Usuarios ======
let usuariosData = [];
let editingUsuario = null;

// Aplica filtros (texto, estado) y renderiza la tabla
function renderUsuarios() {
    const texto = (document.getElementById('search-usuarios').value || '').toLowerCase();
    const estadoF = document.getElementById('filtro-estado').value;

    const rows = usuariosData.filter(u => {
        const bloque = [
            u.usuario || '',
            String(u.idContraseña),
            (u.funcionario ? String(u.funcionario.ciPersona) : '')
        ].join(' ').toLowerCase();
        if (texto && !bloque.includes(texto)) return false;
        const estado = u.eliminado ? 'inactivo' : 'activo';
        if (estadoF !== 'todos' && estado !== estadoF) return false;
        return true;
    }).map(u => ({
        ID: u.idContraseña,
        Usuario: u.usuario,
        Funcionario: (u.funcionario ? u.funcionario.ciPersona : '-'),
        Estado: u.eliminado ? 'Inactivo' : 'Activo'
    }));

    renderTable('usuarios-list', ['ID','Usuario','Funcionario','Estado'], rows, (row) => {
        let html = `<button class="btn btn-outline btn-sm" onclick="editarUsuario(${row.ID})"><i class="fa-solid fa-pen"></i> Editar</button>`;
        if (row.Estado === 'Inactivo') html += `<button class="btn btn-success btn-sm" onclick="activarUsuario(${row.ID})"><i class="fa-solid fa-toggle-on"></i> Activar</button>`;
        else html += `<button class="btn btn-danger btn-sm" onclick="desactivarUsuario(${row.ID})"><i class="fa-solid fa-toggle-off"></i> Desactivar</button>`;
        return html;
    });
}

// Cargar la lista de usuarios (global para refrescarla desde otras funciones)
async function loadUsuarios() {
    usuariosData = await fetchAll('/api/usuarios');
    renderUsuarios();
}

// Limpia los filtros de la tabla de usuarios
function clearFiltrosUsuarios() {
    document.getElementById('search-usuarios').value = '';
    document.getElementById('filtro-estado').value = 'todos';
    renderUsuarios();
}

function editarUsuario(id) {
    const u = usuariosData.find(x => x.idContraseña === id);
    if (!u) return;
    editingUsuario = id;
    document.getElementById('usuario').value = u.usuario || '';
    document.getElementById('contrasena').value = u.contrasena || '';
    document.getElementById('funcionario-ci').value = (u.funcionario ? u.funcionario.ciPersona : '');
    document.getElementById('form-title').textContent = 'Editar Usuario';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Actualizar';
    document.getElementById('usuario-form').scrollIntoView({ behavior: 'smooth' });
}

function resetUsuarioForm() {
    editingUsuario = null;
    document.getElementById('usuario-form').reset();
    document.getElementById('form-title').textContent = 'Registrar Usuario';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar';
}

async function desactivarUsuario(id) {
    if (!confirm('¿Está seguro que desea desactivar este usuario?')) return;
    const res = await fetch('/api/usuarios/' + id, { method: 'DELETE' });
    if (res.ok) { showAlert('Usuario desactivado', 'success'); loadUsuarios(); }
    else showAlert('Error al desactivar el usuario', 'error');
}

async function activarUsuario(id) {
    const res = await fetch('/api/usuarios/' + id + '/restore', { method: 'POST' });
    if (res.ok) { showAlert('Usuario activado', 'success'); loadUsuarios(); }
    else showAlert('Error al activar el usuario', 'error');
}

document.addEventListener('DOMContentLoaded', async function() {
    loadUsuarios();

    // Filtrar al escribir o cambiar seleccion
    document.getElementById('search-usuarios').addEventListener('input', renderUsuarios);
    document.getElementById('filtro-estado').addEventListener('change', renderUsuarios);

    document.getElementById('usuario-form').addEventListener('submit', async function(e) {
        e.preventDefault();
        const fd = new FormData(this);
        const obj = {};
        fd.forEach((v, k) => obj[k] = v);
        const url = editingUsuario ? ('/api/usuarios/' + editingUsuario) : '/api/usuarios';
        const method = editingUsuario ? 'PUT' : 'POST';
        const res = await fetch(url, { method: method, headers:{'Content-Type':'application/json'}, body: JSON.stringify(obj) });
        if (res.ok) {
            showAlert(editingUsuario ? 'Usuario actualizado' : 'Usuario registrado', 'success');
            this.reset();
            editingUsuario = null;
            document.getElementById('form-title').textContent = 'Registrar Usuario';
            document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar';
            loadUsuarios();
        } else showAlert('Error al guardar el usuario', 'error');
    });
});
