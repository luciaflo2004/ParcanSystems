// ====== Lógica de la interfaz de Solicitudes ======
let solicitudesData = [];
let editingSolicitud = null;

// Aplica filtros (texto, estado) y renderiza la tabla
function renderSolicitudes() {
    const texto = (document.getElementById('search-solicitudes').value || '').toLowerCase();
    const estadoF = document.getElementById('filtro-estado').value;

    const rows = solicitudesData.filter(s => {
        const bloque = [
            s.detalle || '',
            (s.feligres ? (s.feligres.nombrePersona || '') + ' ' + (s.feligres.apellidoPersona || '') : '')
        ].join(' ').toLowerCase();
        if (texto && !bloque.includes(texto)) return false;
        const estado = s.eliminado ? 'inactivo' : 'activo';
        if (estadoF !== 'todos' && estado !== estadoF) return false;
        return true;
    }).map(s => ({
        Cod: s.codSolicitud,
        Detalle: s.detalle,
        Fecha: s.fechaSolicitud,
        Estado: s.estadoSolicitud,
        Feligres: (s.feligres ? (s.feligres.nombrePersona || '') + ' ' + (s.feligres.apellidoPersona || '') : ''),
        Reg: s.eliminado ? 'Inactivo' : 'Activo'
    }));

    renderTable('solicitudes-list', ['Cod','Detalle','Fecha','Estado','Feligres','Reg'], rows, (row) => {
        let html = `<button class="btn btn-outline btn-sm" onclick="editarSolicitud(${row.Cod})"><i class="fa-solid fa-pen"></i> Editar</button>`;
        if (row.Reg === 'Inactivo') html += `<button class="btn btn-success btn-sm" onclick="activarSolicitud(${row.Cod})"><i class="fa-solid fa-toggle-on"></i> Activar</button>`;
        else html += `<button class="btn btn-danger btn-sm" onclick="desactivarSolicitud(${row.Cod})"><i class="fa-solid fa-toggle-off"></i> Desactivar</button>`;
        return html;
    });
}

// Cargar la lista de solicitudes (global para refrescarla desde otras funciones)
async function loadSolicitudes() {
    solicitudesData = await fetchAll('/api/solicitudes');
    renderSolicitudes();
}

// Limpia los filtros de la tabla de solicitudes
function clearFiltrosSolicitudes() {
    document.getElementById('search-solicitudes').value = '';
    document.getElementById('filtro-estado').value = 'todos';
    renderSolicitudes();
}

function editarSolicitud(cod) {
    const s = solicitudesData.find(x => x.codSolicitud === cod);
    if (!s) return;
    editingSolicitud = cod;
    document.getElementById('detalle').value = s.detalle || '';
    document.getElementById('fechaSolicitud').value = s.fechaSolicitud || '';
    document.getElementById('estadoSolicitud').value = s.estadoSolicitud || '';
    if (s.feligres) document.getElementById('feligres-select').value = s.feligres.ciPersona;
    document.getElementById('form-title').textContent = 'Editar Solicitud';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Actualizar';
    document.getElementById('solicitud-form').scrollIntoView({ behavior: 'smooth' });
}

function resetSolicitudForm() {
    editingSolicitud = null;
    document.getElementById('solicitud-form').reset();
    document.getElementById('form-title').textContent = 'Registrar Solicitud';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar';
}

async function desactivarSolicitud(cod) {
    if (!confirm('¿Está seguro que desea desactivar esta solicitud?')) return;
    const res = await fetch('/api/solicitudes/' + cod, { method: 'DELETE' });
    if (res.ok) { showAlert('Solicitud desactivada', 'success'); loadSolicitudes(); }
    else showAlert('Error al desactivar la solicitud', 'error');
}

async function activarSolicitud(cod) {
    const res = await fetch('/api/solicitudes/' + cod + '/restore', { method: 'POST' });
    if (res.ok) { showAlert('Solicitud activada', 'success'); loadSolicitudes(); }
    else showAlert('Error al activar la solicitud', 'error');
}

document.addEventListener('DOMContentLoaded', async function() {
    // Cargar feligreses en el select
    const feligreses = await fetchAll('/api/personas');
    const sel = document.getElementById('feligres-select');
    feligreses.forEach(p => {
        const o = document.createElement('option');
        o.value = p.ciPersona;
        o.text = (p.nombrePersona || '') + ' ' + (p.apellidoPersona || '');
        sel.appendChild(o);
    });

    loadSolicitudes();

    document.getElementById('solicitud-form').addEventListener('submit', async function(e) {
        e.preventDefault();
        const fd = new FormData(this);
        const obj = {};
        fd.forEach((v, k) => obj[k] = v);
        Object.keys(obj).forEach(k => { if (obj[k] === '') obj[k] = null; });
        const url = editingSolicitud ? ('/api/solicitudes/' + editingSolicitud) : '/api/solicitudes';
        const method = editingSolicitud ? 'PUT' : 'POST';
        const res = await fetch(url, { method: method, headers:{'Content-Type':'application/json'}, body: JSON.stringify(obj) });
        if (res.ok) {
            showAlert(editingSolicitud ? 'Solicitud actualizada' : 'Solicitud registrada', 'success');
            this.reset();
            editingSolicitud = null;
            document.getElementById('form-title').textContent = 'Registrar Solicitud';
            document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar';
    loadSolicitudes();

    // Filtrar al escribir o cambiar seleccion
    document.getElementById('search-solicitudes').addEventListener('input', renderSolicitudes);
    document.getElementById('filtro-estado').addEventListener('change', renderSolicitudes);
        } else showAlert('Error al guardar la solicitud', 'error');
    });
});