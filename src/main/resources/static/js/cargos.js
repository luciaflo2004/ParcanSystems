// ====== Lógica de la interfaz de Cargos ======
let cargosData = [];
let editingCargo = null;

// Aplica filtros (texto, estado) y renderiza la tabla
function renderCargos() {
    const texto = (document.getElementById('search-cargos').value || '').toLowerCase();
    const estadoF = document.getElementById('filtro-estado').value;

    // Ordena los registros por ID ascendente antes de mostrarlos
    const rows = ordenarPorId(cargosData.filter(c => {
        if (texto && !(c.descripcion || '').toLowerCase().includes(texto)) return false;
        const estado = c.eliminado ? 'inactivo' : 'activo';
        if (estadoF !== 'todos' && estado !== estadoF) return false;
        return true;
    }), 'codCargo').map(c => ({
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

// Devuelve el mensaje de error del formulario de cargos, o null si esta correcto
function mensajeErrorCargo() {
    const descripcion = document.getElementById('descripcion').value;
    if (!descripcion.trim()) return 'La descripción del cargo es obligatoria';
    if (!esSoloLetras(descripcion)) return 'La descripción del cargo solo permite letras y espacios';
    if (descripcion.length > 100) return 'La descripción no puede superar los 100 caracteres';
    if (tieneEspaciosConsecutivos(descripcion)) return 'No se permiten más de dos espacios consecutivos';
    if (tieneCaracteresConsecutivos(descripcion, 2)) return 'No se permiten más de dos letras iguales consecutivas';
    return null;
}

// Carga los datos del cargo seleccionado en el formulario
function editarCargo(cod) {
    const c = cargosData.find(x => x.codCargo === cod);
    if (!c) return;
    editingCargo = cod;
    document.getElementById('descripcion').value = c.descripcion || '';
    limpiarErroresFormulario(document.getElementById('cargo-form'));
    document.getElementById('form-title').textContent = 'Editar Cargo';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Actualizar';
    document.getElementById('cargo-form').scrollIntoView({ behavior: 'smooth' });
}

// Limpia el formulario para registrar un cargo nuevo
function resetCargoForm() {
    editingCargo = null;
    document.getElementById('cargo-form').reset();
    limpiarErroresFormulario(document.getElementById('cargo-form'));
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

    // Valida los datos del cargo antes de guardar
    document.getElementById('cargo-form').addEventListener('submit', async function(e) {
        e.preventDefault();
        const form = this;
        const descripcionInput = document.getElementById('descripcion');
        limpiarErroresFormulario(form);

        const errores = erroresValidacionNativa(form);
        const errorDescripcion = errores.find(item => item.campo === descripcionInput);
        if (errorDescripcion) {
            // La regla personalizada actualiza el mensaje mientras se corrige
            errorDescripcion.validador = mensajeErrorCargo;
        } else {
            const mensaje = mensajeErrorCargo();
            if (mensaje) {
                errores.push({
                    campo: descripcionInput,
                    mensaje,
                    validador: mensajeErrorCargo
                });
            }
        }
        if (errores.length) {
            mostrarErroresFormulario(form, errores);
            return;
        }

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
            limpiarErroresFormulario(form);
            document.getElementById('form-title').textContent = 'Registrar Cargo';
            document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar';
            loadCargos();
        } else {
            const err = await res.json().catch(() => null);
            const mensajeServidor = err && err.message ? err.message : 'Error al guardar el cargo';
            if (/descripci[oó]n/i.test(mensajeServidor)) {
                mostrarErroresFormulario(form, [{
                    campo: descripcionInput,
                    mensaje: mensajeServidor,
                    validador: mensajeErrorCargo
                }]);
            } else {
                showAlert(mensajeServidor, 'error');
            }
        }
    });
});
