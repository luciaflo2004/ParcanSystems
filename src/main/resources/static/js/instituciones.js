// ====== Lógica de la interfaz de Instituciones Eclesiásticas ======
let institucionesData = [];
let editingInstitucion = null;

// Aplica filtros (texto, estado) y renderiza la tabla
function renderInstituciones() {
    const texto = (document.getElementById('search-instituciones').value || '').toLowerCase();
    const estadoF = document.getElementById('filtro-estado').value;

    // Ordena las instituciones por ID ascendente
    const rows = ordenarPorId(institucionesData.filter(i => {
        const bloque = [
            i.nombre || '',
            i.diocese || '',
            i.ciudad || '',
            i.tipo || ''
        ].join(' ').toLowerCase();
        if (texto && !bloque.includes(texto)) return false;
        const estado = i.eliminado ? 'inactivo' : 'activo';
        if (estadoF !== 'todos' && estado !== estadoF) return false;
        return true;
    }), 'idInstitucionEclesiastica').map(i => ({
        ID: i.idInstitucionEclesiastica,
        Nombre: i.nombre,
        Tipo: i.tipo,
        Diocesis: i.diocese,
        Ciudad: i.ciudad,
        Estado: i.eliminado ? 'Inactivo' : 'Activo'
    }));

    renderTable('instituciones-list', ['ID','Nombre','Tipo','Diocesis','Ciudad','Estado'], rows, (row) => {
        let html = `<button class="btn btn-outline btn-sm" onclick="editarInstitucion(${row.ID})"><i class="fa-solid fa-pen"></i> Editar</button>`;
        if (row.Estado === 'Inactivo') html += `<button class="btn btn-success btn-sm" onclick="activarInstitucion(${row.ID})"><i class="fa-solid fa-toggle-on"></i> Activar</button>`;
        else html += `<button class="btn btn-danger btn-sm" onclick="desactivarInstitucion(${row.ID})"><i class="fa-solid fa-toggle-off"></i> Desactivar</button>`;
        return html;
    });
}

// Cargar la lista de instituciones (global para refrescarla desde otras funciones)
async function loadInstituciones() {
    institucionesData = await fetchAll('/api/instituciones');
    renderInstituciones();
}

// Limpia los filtros de la tabla de instituciones
function clearFiltrosInstituciones() {
    document.getElementById('search-instituciones').value = '';
    document.getElementById('filtro-estado').value = 'todos';
    renderInstituciones();
}

// Carga los datos de la institucion seleccionada en el formulario
function editarInstitucion(id) {
    const i = institucionesData.find(x => x.idInstitucionEclesiastica === id);
    if (!i) return;
    editingInstitucion = id;
    limpiarErroresFormulario(document.getElementById('institucion-form'));
    document.getElementById('diocese').value = i.diocese || '';
    document.getElementById('nombre').value = i.nombre || '';
    document.getElementById('tipo').value = i.tipo || '';
    document.getElementById('ciudad').value = i.ciudad || '';
    document.getElementById('form-title').textContent = 'Editar Institución';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Actualizar';
    document.getElementById('institucion-form').scrollIntoView({ behavior: 'smooth' });
}

// Limpia el formulario para registrar una institucion nueva
function resetInstitucionForm() {
    editingInstitucion = null;
    document.getElementById('institucion-form').reset();
    limpiarErroresFormulario(document.getElementById('institucion-form'));
    document.getElementById('form-title').textContent = 'Registrar Institución';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar';
}

async function desactivarInstitucion(id) {
    if (!confirm('¿Está seguro que desea desactivar esta institución?')) return;
    const res = await fetch('/api/instituciones/' + id, { method: 'DELETE' });
    if (res.ok) { showAlert('Institución desactivada', 'success'); loadInstituciones(); }
    else showAlert('Error al desactivar la institución', 'error');
}

async function activarInstitucion(id) {
    const res = await fetch('/api/instituciones/' + id + '/restore', { method: 'POST' });
    if (res.ok) { showAlert('Institución activada', 'success'); loadInstituciones(); }
    else showAlert('Error al activar la institución', 'error');
}

document.addEventListener('DOMContentLoaded', async function() {
    loadInstituciones();

    // Filtrar al escribir o cambiar seleccion
    document.getElementById('search-instituciones').addEventListener('input', renderInstituciones);
    document.getElementById('filtro-estado').addEventListener('change', renderInstituciones);

    // Envia los datos de la institucion al backend
    document.getElementById('institucion-form').addEventListener('submit', async function(e) {
        e.preventDefault();
        const form = this;
        limpiarErroresFormulario(form);
        const errores = erroresValidacionNativa(form);
        if (errores.length) {
            mostrarErroresFormulario(form, errores);
            return;
        }
        const fd = new FormData(this);
        const obj = {};
        fd.forEach((v, k) => obj[k] = v);
        Object.keys(obj).forEach(k => { if (obj[k] === '') obj[k] = null; });
        const url = editingInstitucion ? ('/api/instituciones/' + editingInstitucion) : '/api/instituciones';
        const method = editingInstitucion ? 'PUT' : 'POST';
        const res = await fetch(url, { method: method, headers:{'Content-Type':'application/json'}, body: JSON.stringify(obj) });
        if (res.ok) {
            showAlert(editingInstitucion ? 'Institución actualizada' : 'Institución registrada', 'success');
            limpiarErroresFormulario(form);
            // Si se llego desde Solicitudes para crear un solicitante, se regresa con el nuevo registro
            const params = new URLSearchParams(window.location.search);
            if (params.get('origen') === 'solicitudes') {
                const saved = await res.json();
                localStorage.setItem('spc_nuevo_solicitante', JSON.stringify({ tipo: 'INSTITUCION', id: saved.idInstitucionEclesiastica }));
                window.location.href = 'solicitudes.html';
                return;
            }
            this.reset();
            editingInstitucion = null;
            document.getElementById('form-title').textContent = 'Registrar Institución';
            document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar';
            loadInstituciones();
        } else {
            const err = await res.json().catch(() => null);
            const mensajeServidor = err && err.message ? err.message : 'Error al guardar la institución';
            if (/nombre/i.test(mensajeServidor)) {
                mostrarErroresFormulario(form, [{
                    campo: document.getElementById('nombre'),
                    mensaje: mensajeServidor,
                    validador: () => document.getElementById('nombre').value.trim() ? null : 'El nombre es obligatorio'
                }]);
            } else {
                showAlert(mensajeServidor, 'error');
            }
        }
    });
});
