// ====== Lógica de la interfaz de Personas y Funcionarios ======

// Variables globales para la edición
let allPersonas = [];
let funcionarioCIs = new Set();
let editingCi = null;

// Aplica los filtros (texto, estado, rol) y devuelve las filas filtradas
function getPersonaRows() {
    const texto = (document.getElementById('search-personas').value || '').toLowerCase();
    const estadoF = document.getElementById('filtro-estado').value;
    const rolF = document.getElementById('filtro-rol').value;

    return allPersonas.filter(p => {
        // Filtro por texto: nombre, apellido, CI, teléfono
        const bloque = [
            p.nombrePersona || '', p.apellidoPersona || '',
            String(p.ciPersona), p.telefonoPersona || ''
        ].join(' ').toLowerCase();
        if (texto && !bloque.includes(texto)) return false;

        // Filtro por estado
        const estado = p.eliminado ? 'inactivo' : 'activo';
        if (estadoF !== 'todos' && estado !== estadoF) return false;

        // Filtro por rol
        const rol = funcionarioCIs.has(p.ciPersona) ? 'funcionario' : 'persona';
        if (rolF !== 'todos' && rol !== rolF) return false;

        return true;
    });
}

// Renderiza la tabla aplicando los filtros
function renderPersonas() {
    const rows = getPersonaRows().map(p => ({
        CI: p.ciPersona,
        Nombre: p.nombrePersona,
        Apellido: p.apellidoPersona,
        Telefono: p.telefonoPersona,
        Tipo: funcionarioCIs.has(p.ciPersona) ? 'Funcionario' : 'Persona',
        Estado: p.eliminado ? 'Inactivo' : 'Activo'
    }));

    renderTable('personas-list', ['CI','Nombre','Apellido','Telefono','Tipo','Estado'], rows, (row) => {
        const p = allPersonas.find(x => x.ciPersona === row.CI);
        let html = `<button class="btn btn-outline btn-sm" onclick="editarPersona(${row.CI})"><i class="fa-solid fa-pen"></i> Editar</button>`;
        if (p && p.eliminado) {
            html += `<button class="btn btn-success btn-sm" onclick="activarPersona(${row.CI})"><i class="fa-solid fa-toggle-on"></i> Activar</button>`;
        } else {
            html += `<button class="btn btn-danger btn-sm" onclick="desactivarPersona(${row.CI})"><i class="fa-solid fa-toggle-off"></i> Desactivar</button>`;
        }
        return html;
    });
}

// Cargar la lista de personas (global para poder refrescarla desde otras funciones)
async function loadPersonas() {
    allPersonas = await fetchAll('/api/personas');
    const funcionarios = await fetchAll('/api/funcionarios');
    funcionarioCIs = new Set(funcionarios.map(f => f.ciPersona));
    renderPersonas();
}

// Limpia los filtros de la tabla de personas
function clearFiltrosPersonas() {
    document.getElementById('search-personas').value = '';
    document.getElementById('filtro-estado').value = 'todos';
    document.getElementById('filtro-rol').value = 'todos';
    renderPersonas();
}

async function editarPersona(ci) {
    const p = allPersonas.find(x => x.ciPersona === ci);
    if (!p) return;
    editingCi = ci;

    // Cargar los datos de la persona en el formulario
    document.getElementById('ciPersona').value = p.ciPersona;
    document.getElementById('nombrePersona').value = p.nombrePersona || '';
    document.getElementById('apellidoPersona').value = p.apellidoPersona || '';
    document.getElementById('fechaNacPersona').value = p.fechaNacPersona || '';
    document.getElementById('direccionPersona').value = p.direccionPersona || '';
    document.getElementById('telefonoPersona').value = p.telefonoPersona || '';

    // Determinar si es funcionario
    const esFuncionario = funcionarioCIs.has(ci);
    document.getElementById('rol-select').value = esFuncionario ? 'FUNCIONARIO' : 'PERSONA';
    document.getElementById('cargo-group').classList.toggle('hidden', !esFuncionario);

    // Si es funcionario, cargar su cargo
    if (esFuncionario) {
        const fun = await fetchAll('/api/funcionarios/' + ci);
        if (fun && fun.cargo) {
            document.getElementById('cargo-select').value = fun.cargo.codCargo;
        }
    }

    // Cambiar título y botón del formulario
    document.getElementById('form-title').textContent = 'Editar Persona / Funcionario';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Actualizar';
    document.getElementById('persona-form').scrollIntoView({ behavior: 'smooth' });
}

async function desactivarPersona(ci) {
    if (!confirm('¿Está seguro que desea desactivar este registro?')) return;
    const res = await fetch('/api/personas/' + ci, { method: 'DELETE' });
    if (res.ok) { showAlert('Registro desactivado', 'success'); loadPersonas(); }
    else showAlert('Error al desactivar el registro', 'error');
}

async function activarPersona(ci) {
    const res = await fetch('/api/personas/' + ci + '/restore', { method: 'POST' });
    if (res.ok) { showAlert('Registro activado', 'success'); loadPersonas(); }
    else showAlert('Error al activar el registro', 'error');
}

// Restablece el formulario al modo de creación
function resetForm() {
    editingCi = null;
    document.getElementById('persona-form').reset();
    document.getElementById('cargo-group').classList.add('hidden');
    document.getElementById('form-title').textContent = 'Registrar Persona / Funcionario';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar';
}

document.addEventListener('DOMContentLoaded', async function() {
    // Cargar los cargos disponibles en el select
    const cargos = await fetchAll('/api/cargos');
    const cargoSelect = document.getElementById('cargo-select');
    cargos.forEach(c => {
        const o = document.createElement('option');
        o.value = c.codCargo;
        o.text = c.descripcion;
        cargoSelect.appendChild(o);
    });

    // Mostrar/ocultar el campo Cargo según el rol elegido
    const rolSelect = document.getElementById('rol-select');
    const cargoGroup = document.getElementById('cargo-group');
    rolSelect.addEventListener('change', function() {
        cargoGroup.classList.toggle('hidden', this.value !== 'FUNCIONARIO');
    });

    loadPersonas();

    // Guardar / actualizar el registro unificado
    document.getElementById('persona-form').addEventListener('submit', async function(e) {
        e.preventDefault();

        // Si el rol es FUNCIONARIO, se exige elegir un cargo
        const rol = document.getElementById('rol-select').value;
        const cargo = document.getElementById('cargo-select').value;
        if (rol === 'FUNCIONARIO' && !cargo) {
            showAlert('Debe seleccionar un cargo para el funcionario', 'error');
            return;
        }

        // Validar fecha de nacimiento (obligatoria y no anterior a 1930)
        const fecha = document.getElementById('fechaNacPersona').value;
        if (!fecha) {
            showAlert('La fecha de nacimiento es obligatoria', 'error');
            return;
        }
        if (new Date(fecha) < new Date('1930-01-01')) {
            showAlert('La fecha de nacimiento no puede ser anterior a 1930', 'error');
            return;
        }

        // Construir el objeto con datos personales + rol + cargo
        const fd = new FormData(this);
        const obj = {};
        fd.forEach((v, k) => obj[k] = v);
        Object.keys(obj).forEach(k => { if (obj[k] === '') obj[k] = null; });
        obj.cargo = cargo ? Number(cargo) : null;

        try {
            // Si se está editando se usa PUT, sino POST
            const url = editingCi ? ('/api/personas/registro/' + editingCi) : '/api/personas/registro';
            const method = editingCi ? 'PUT' : 'POST';
            const res = await fetch(url, { method: method, headers:{'Content-Type':'application/json'}, body: JSON.stringify(obj) });
            if (res.ok) {
                showAlert(editingCi ? 'Registro actualizado correctamente' : 'Registro guardado correctamente', 'success');
                this.reset();
                editingCi = null;
                cargoGroup.classList.add('hidden');
                document.getElementById('form-title').textContent = 'Registrar Persona / Funcionario';
                document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar';
                loadPersonas();
            } else {
                const err = await res.json().catch(() => null);
                showAlert(err && err.message ? err.message : 'Error al guardar el registro', 'error');
            }
        } catch (err) {
            showAlert('No se pudo conectar con el servidor', 'error');
        }
    });

    // Filtrar la tabla al escribir o cambiar seleccion
    document.getElementById('search-personas').addEventListener('input', renderPersonas);
    document.getElementById('filtro-estado').addEventListener('change', renderPersonas);
    document.getElementById('filtro-rol').addEventListener('change', renderPersonas);
});
