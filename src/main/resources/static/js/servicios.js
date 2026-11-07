// ====== Lógica de la interfaz de Servicios ======
let serviciosData = [];
let editingServicio = null;

// Devuelve el nombre del solicitante de una solicitud (feligrés o institución)
function nombreSolicitanteDeSolicitud(s) {
    if (s.feligres) return ((s.feligres.nombrePersona || '') + ' ' + (s.feligres.apellidoPersona || '')).trim();
    if (s.institucion) return s.institucion.nombre || '';
    return 'Sin solicitante';
}

// Formatea una solicitud como "Detalle – Solicitante" para mostrarla en el select y en la tabla
function textoSolicitud(s) {
    if (!s) return '-';
    return (s.detalle || '') + ' – ' + nombreSolicitanteDeSolicitud(s);
}

// Aplica filtros (texto, estado) y renderiza la tabla
function renderServicios() {
    const texto = (document.getElementById('search-servicios').value || '').toLowerCase();
    const estadoF = document.getElementById('filtro-estado').value;

    const rows = serviciosData.filter(s => {
        const bloque = [
            s.detalleServicio || '',
            (s.funcionario ? String(s.funcionario.ciPersona) : ''),
            (s.solicitud ? textoSolicitud(s.solicitud) : '')
        ].join(' ').toLowerCase();
        if (texto && !bloque.includes(texto)) return false;
        const estado = s.realizado ? 'realizado' : 'pendiente';
        if (estadoF !== 'todos' && estado !== estadoF) return false;
        return true;
    }).map(s => ({
        Cod: s.codServicio,
        Detalle: s.detalleServicio,
        Costo: s.costoServicio,
        Estado: s.realizado ? 'Realizado' : 'Pendiente',
        Fecha: s.fechaRealizacion || '-',
        Funcionario: (s.funcionario ? s.funcionario.ciPersona : '-'),
        Solicitud: (s.solicitud ? textoSolicitud(s.solicitud) : '-'),
        Reg: s.eliminado ? 'Inactivo' : 'Activo'
    }));

    renderTable('servicios-list', ['Cod','Detalle','Costo','Estado','Fecha','Funcionario','Solicitud','Reg'], rows, (row) => {
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

// Carga las solicitudes activas en el desplegable "Solicitud"
async function cargarSolicitudes() {
    const solicitudes = await fetchAll('/api/solicitudes');
    const sel = document.getElementById('solicitud-select');
    sel.innerHTML = '<option value="">Seleccione...</option>';
    solicitudes.filter(s => !s.eliminado).forEach(s => {
        const o = document.createElement('option');
        o.value = s.codSolicitud;
        o.text = textoSolicitud(s);
        sel.appendChild(o);
    });
}

// Agrega una opcion al select si aún no existe (para precargar solicitudes ya desactivadas al editar)
function asegurarOpcion(select, value, text) {
    if (value == null) return;
    if (!Array.from(select.options).some(o => o.value === String(value))) {
        const o = document.createElement('option');
        o.value = value;
        o.text = text;
        select.appendChild(o);
    }
}

function editarServicio(cod) {
    const s = serviciosData.find(x => x.codServicio === cod);
    if (!s) return;
    editingServicio = cod;
    document.getElementById('detalleServicio').value = s.detalleServicio || '';
    document.getElementById('costoServicio').value = s.costoServicio || '';
    document.getElementById('realizado').value = s.realizado ? 'true' : 'false';
    document.getElementById('fechaRealizacion').value = s.fechaRealizacion || '';
    if (s.funcionario) document.getElementById('func-select').value = s.funcionario.ciPersona;
    // Precargar la solicitud asociada al servicio
    if (s.solicitud) {
        const sel = document.getElementById('solicitud-select');
        asegurarOpcion(sel, s.solicitud.codSolicitud, textoSolicitud(s.solicitud));
        sel.value = s.solicitud.codSolicitud;
    }
    document.getElementById('form-title').textContent = 'Editar Servicio';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Actualizar';
    document.getElementById('servicio-form').scrollIntoView({ behavior: 'smooth' });
}

function resetServicioForm() {
    editingServicio = null;
    document.getElementById('servicio-form').reset();
    document.getElementById('solicitud-select').value = '';
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
    // Cargar funcionarios en el select mostrando nombre + cargo
    const funcionarios = await fetchAll('/api/funcionarios');
    const sel = document.getElementById('func-select');
    funcionarios.forEach(f => {
        const o = document.createElement('option');
        o.value = f.ciPersona;
        const nombre = f.persona ? (f.persona.nombrePersona || '') + ' ' + (f.persona.apellidoPersona || '') : f.ciPersona;
        const cargo = f.cargo ? f.cargo.descripcion : '';
        o.text = nombre + (cargo ? ' - ' + cargo : '');
        sel.appendChild(o);
    });

    // Cargar las solicitudes en el desplegable "Solicitud"
    await cargarSolicitudes();

    loadServicios();

    document.getElementById('servicio-form').addEventListener('submit', async function(e) {
        e.preventDefault();

        // La solicitud es obligatoria para registrar el servicio
        const solSel = document.getElementById('solicitud-select');
        if (!solSel.value) { showAlert('Debe seleccionar una solicitud', 'error'); return; }

        const funcSel = document.getElementById('func-select');
        const obj = {
            detalleServicio: document.getElementById('detalleServicio').value || null,
            costoServicio: document.getElementById('costoServicio').value ? Number(document.getElementById('costoServicio').value) : null,
            realizado: document.getElementById('realizado').value === 'true',
            fechaRealizacion: document.getElementById('fechaRealizacion').value || null,
            funcionario: funcSel.value ? { ciPersona: Number(funcSel.value) } : null,
            solicitud: { codSolicitud: Number(solSel.value) }
        };

        const url = editingServicio ? ('/api/servicios/' + editingServicio) : '/api/servicios';
        const method = editingServicio ? 'PUT' : 'POST';
        const res = await fetch(url, { method: method, headers:{'Content-Type':'application/json'}, body: JSON.stringify(obj) });
        if (res.ok) {
            showAlert(editingServicio ? 'Servicio actualizado' : 'Servicio registrado', 'success');
            resetServicioForm();
            loadServicios();
        } else {
            const err = await res.json().catch(() => null);
            showAlert(err && err.message ? err.message : 'Error al guardar el servicio', 'error');
        }
    });

    // Filtrar al escribir o cambiar seleccion
    document.getElementById('search-servicios').addEventListener('input', renderServicios);
    document.getElementById('filtro-estado').addEventListener('change', renderServicios);
});
