// ====== Lógica de la interfaz de Servicios ======
let serviciosData = [];
let editingServicio = null;

// Aplica filtros (texto, estado) y renderiza la tabla
function renderServicios() {
    const texto = (document.getElementById('search-servicios').value || '').toLowerCase();
    const estadoF = document.getElementById('filtro-estado').value;

    const rows = serviciosData.filter(s => {
        const bloque = [
            s.detalleServicio || '',
            (s.funcionario ? String(s.funcionario.ciPersona) : '')
        ].join(' ').toLowerCase();
        if (texto && !bloque.includes(texto)) return false;
        const estado = s.eliminado ? 'inactivo' : 'activo';
        if (estadoF !== 'todos' && estado !== estadoF) return false;
        return true;
    }).map(s => ({
        Cod: s.codServicio,
        Detalle: s.detalleServicio,
        Costo: s.costoServicio,
        Estado: s.estadoServicio,
        Funcionario: (s.funcionario ? s.funcionario.ciPersona : '-'),
        Reg: s.eliminado ? 'Inactivo' : 'Activo'
    }));

    renderTable('servicios-list', ['Cod','Detalle','Costo','Estado','Funcionario','Reg'], rows, (row) => {
        let html = `<button class="btn btn-outline btn-sm" onclick="editarServicio(${row.Cod})"><i class="fa-solid fa-pen"></i> Editar</button>`;
        if (row.Reg === 'Inactivo') html += `<button class="btn btn-success btn-sm" onclick="activarServicio(${row.Cod})"><i class="fa-solid fa-toggle-on"></i> Activar</button>`;
        else html += `<button class="btn btn-danger btn-sm" onclick="desactivarServicio(${row.Cod})"><i class="fa-solid fa-toggle-off"></i> Desactivar</button>`;
        return html;
    });
}

// Cargar la lista de servicios (global para refrescarla desde otras funciones)
async function loadServicios() {
    serviciosData = await fetchAll('/api/servicios');
    renderServicios();
}

// Limpia los filtros de la tabla de servicios
function clearFiltrosServicios() {
    document.getElementById('search-servicios').value = '';
    document.getElementById('filtro-estado').value = 'todos';
    renderServicios();
}

function editarServicio(cod) {
    const s = serviciosData.find(x => x.codServicio === cod);
    if (!s) return;
    editingServicio = cod;
    document.getElementById('detalleServicio').value = s.detalleServicio || '';
    document.getElementById('costoServicio').value = s.costoServicio || '';
    document.getElementById('estadoServicio').value = s.estadoServicio || '';
    if (s.funcionario) document.getElementById('func-select').value = s.funcionario.ciPersona;
    document.getElementById('form-title').textContent = 'Editar Servicio';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Actualizar';
    document.getElementById('servicio-form').scrollIntoView({ behavior: 'smooth' });
}

function resetServicioForm() {
    editingServicio = null;
    document.getElementById('servicio-form').reset();
    document.getElementById('form-title').textContent = 'Registrar Servicio';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar';
}

async function desactivarServicio(cod) {
    if (!confirm('¿Está seguro que desea desactivar este servicio?')) return;
    const res = await fetch('/api/servicios/' + cod, { method: 'DELETE' });
    if (res.ok) { showAlert('Servicio desactivado', 'success'); loadServicios(); }
    else showAlert('Error al desactivar el servicio', 'error');
}

async function activarServicio(cod) {
    const res = await fetch('/api/servicios/' + cod + '/restore', { method: 'POST' });
    if (res.ok) { showAlert('Servicio activado', 'success'); loadServicios(); }
    else showAlert('Error al activar el servicio', 'error');
}

document.addEventListener('DOMContentLoaded', async function() {
    // Cargar funcionarios en el select
    const funcionarios = await fetchAll('/api/funcionarios');
    const sel = document.getElementById('func-select');
    funcionarios.forEach(f => {
        const o = document.createElement('option');
        o.value = f.ciPersona;
        o.text = f.ciPersona;
        sel.appendChild(o);
    });

    loadServicios();

    document.getElementById('servicio-form').addEventListener('submit', async function(e) {
        e.preventDefault();
        const fd = new FormData(this);
        const obj = {};
        fd.forEach((v, k) => obj[k] = v);
        Object.keys(obj).forEach(k => { if (obj[k] === '') obj[k] = null; });
        const url = editingServicio ? ('/api/servicios/' + editingServicio) : '/api/servicios';
        const method = editingServicio ? 'PUT' : 'POST';
        const res = await fetch(url, { method: method, headers:{'Content-Type':'application/json'}, body: JSON.stringify(obj) });
        if (res.ok) {
            showAlert(editingServicio ? 'Servicio actualizado' : 'Servicio registrado', 'success');
            this.reset();
            editingServicio = null;
            document.getElementById('form-title').textContent = 'Registrar Servicio';
            document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar';
    loadServicios();

    // Filtrar al escribir o cambiar seleccion
    document.getElementById('search-servicios').addEventListener('input', renderServicios);
    document.getElementById('filtro-estado').addEventListener('change', renderServicios);
        } else showAlert('Error al guardar el servicio', 'error');
    });
});
