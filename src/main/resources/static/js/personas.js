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

// Renderiza la tabla aplicando los filtros y el orden seleccionado
function renderPersonas() {
    const direccion = document.getElementById('orden-personas').value;
    // Orden alfabetico por nombre; el apellido y el ID se usan como desempate
    const rows = ordenarAlfabeticamente(
        getPersonaRows(),
        p => p.nombrePersona,
        p => p.apellidoPersona,
        p => p.ciPersona,
        direccion
    ).map(p => ({
        CI: p.ciPersona,
        Nombre: p.nombrePersona,
        Apellido: p.apellidoPersona,
        Telefono: p.telefonoPersona,
        Tipo: funcionarioCIs.has(p.ciPersona) ? 'Funcionario' : 'Persona',
        Estado: p.eliminado ? 'Inactivo' : 'Activo',
        _letra: letraInicial(p.nombrePersona)
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
    }, { indiceAlfabetico: true });
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
    // Al limpiar se vuelve al orden predeterminado de A a la Z
    document.getElementById('orden-personas').value = '';
    renderPersonas();
}

// Devuelve el mensaje de error de un campo de Persona, o null si es válido
function mensajeErrorPersonaCampo(campo) {
    if (!campo) return null;
    const valor = campo.value;
    const rol = document.getElementById('rol-select').value;

    if (campo.id === 'ciPersona') {
        if (!/^[0-9]{6,}$/.test(valor)) return 'La cédula debe tener mínimo 6 números y no letras';
        if (valor.length > 8) return 'La cédula debe contener como máximo 8 números';
        if (tieneCaracteresConsecutivos(valor, 4)) return 'No se permiten más de cuatro números iguales consecutivos';
    }

    if (campo.id === 'nombrePersona') {
        if (!/^[A-Za-zÀ-ÿ ]+$/.test(valor)) return 'El nombre solo debe contener letras y espacios';
        if (valor.replace(/[^A-Za-zÀ-ÿ]/g, '').length < 2) return 'El nombre debe tener mínimo 2 letras';
        if (valor.length > 30) return 'El nombre no puede superar los 30 caracteres';
        if (tieneEspaciosConsecutivos(valor)) return 'No se permiten más de dos espacios consecutivos';
        if (tieneCaracteresConsecutivos(valor, 2)) return 'No se permiten más de dos letras iguales consecutivas. Ingrese un nombre válido';
    }

    if (campo.id === 'apellidoPersona') {
        if (!/^[A-Za-zÀ-ÿ ]+$/.test(valor)) return 'El apellido solo debe contener letras y espacios';
        if (valor.replace(/[^A-Za-zÀ-ÿ]/g, '').length < 2) return 'El apellido debe tener mínimo 2 letras';
        if (valor.length > 30) return 'El apellido no puede superar los 30 caracteres';
        if (tieneEspaciosConsecutivos(valor)) return 'No se permiten más de dos espacios consecutivos';
        if (tieneCaracteresConsecutivos(valor, 2)) return 'No se permiten más de dos letras iguales consecutivas. Ingrese un apellido válido';
    }

    if (campo.id === 'fechaNacPersona') {
        if (!valor) return 'La fecha de nacimiento es obligatoria';
        if (valor > hoyLocal()) return 'La fecha de nacimiento no es válida. No puede ser posterior a la fecha actual. Verifique e intente nuevamente';
        if (valor < '1930-01-01') return 'La fecha de nacimiento no puede ser anterior a 1930';
        if (rol === 'FUNCIONARIO' && valor > fechaMaximaFuncionario()) {
            return 'El funcionario debe tener al menos 18 años cumplidos. Verifique la fecha de nacimiento';
        }
    }

    if (campo.id === 'telefonoPersona') {
        if (valor && !/^[0-9]+$/.test(valor)) return 'El teléfono debe contener únicamente números';
        if (valor && valor.length > 10) return 'El teléfono no puede superar los 10 dígitos';
        if (valor && tieneCaracteresConsecutivos(valor, 4)) return 'No se permiten más de cuatro números iguales consecutivos';
    }

    if (campo.id === 'direccionPersona') {
        if (valor && valor.length > 50) return 'La dirección no puede superar los 50 caracteres';
        if (valor && tieneEspaciosConsecutivos(valor)) return 'No se permiten más de dos espacios consecutivos';
        if (valor && tieneCaracteresConsecutivos(valor, 2)) return 'No se permiten más de dos caracteres iguales consecutivos';
    }

    if (campo.id === 'cargo-select' && rol === 'FUNCIONARIO' && !valor) {
        return 'Debe seleccionar un cargo para el funcionario';
    }

    return null;
}

// Busca a qué campo corresponde un mensaje devuelto por el backend
function campoPersonaPorMensaje(mensaje) {
    const texto = (mensaje || '').toLowerCase();
    if (texto.includes('cédula') || texto.includes('cedula')) return document.getElementById('ciPersona');
    if (texto.includes('apellido')) return document.getElementById('apellidoPersona');
    if (texto.includes('nombre')) return document.getElementById('nombrePersona');
    if (texto.includes('fecha')) return document.getElementById('fechaNacPersona');
    if (texto.includes('teléfono') || texto.includes('telefono')) return document.getElementById('telefonoPersona');
    if (texto.includes('dirección') || texto.includes('direccion')) return document.getElementById('direccionPersona');
    if (texto.includes('cargo')) return document.getElementById('cargo-select');
    return null;
}


// Carga los datos de la persona seleccionada en el formulario
async function editarPersona(ci) {
    const p = allPersonas.find(x => x.ciPersona === ci);
    if (!p) return;
    editingCi = ci;
    limpiarErroresFormulario(document.getElementById('persona-form'));

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
    // Ajustar la fecha máxima según la condición real del registro
    document.getElementById('fechaNacPersona').max =
        esFuncionario ? fechaMaximaFuncionario() : hoyLocal();

    // Si es funcionario, cargar su cargo
    if (esFuncionario) {
        const fun = await fetchAll('/api/funcionarios/' + ci);
        const cargoSelect = document.getElementById('cargo-select');
        // Se quita una opcion historica de una edicion anterior
        limpiarOpcionesHistoricas(cargoSelect);
        if (fun && fun.cargo) {
            // Se agrega el cargo aunque este inactivo para mostrar el dato historico
            agregarOpcionSiFalta(cargoSelect, fun.cargo.codCargo, fun.cargo.descripcion, true);
            cargoSelect.value = fun.cargo.codCargo;
        }
    }

    // Cambiar título y botón del formulario
    document.getElementById('form-title').textContent = 'Editar Persona / Funcionario';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Actualizar';
    document.getElementById('persona-form').scrollIntoView({ behavior: 'smooth' });
}

// Desactiva la persona seleccionada
async function desactivarPersona(ci) {
    if (!confirm('¿Está seguro que desea desactivar este registro?')) return;
    const res = await fetch('/api/personas/' + ci, { method: 'DELETE' });
    if (res.ok) { showAlert('Registro desactivado', 'success'); loadPersonas(); }
    else showAlert('Error al desactivar el registro', 'error');
}

// Reactiva la persona y su funcionario si existe
async function activarPersona(ci) {
    const res = await fetch('/api/personas/' + ci + '/restore', { method: 'POST' });
    if (res.ok) { showAlert('Registro activado', 'success'); loadPersonas(); }
    else showAlert('Error al activar el registro', 'error');
}

// Restablece el formulario al modo de creación
function resetForm() {
    editingCi = null;
    document.getElementById('persona-form').reset();
    limpiarErroresFormulario(document.getElementById('persona-form'));
    // Al registrar un funcionario nuevo no se muestran cargos historicos inactivos
    limpiarOpcionesHistoricas(document.getElementById('cargo-select'));
    document.getElementById('cargo-group').classList.add('hidden');
    document.getElementById('fechaNacPersona').max = hoyLocal();
    document.getElementById('form-title').textContent = 'Registrar Persona / Funcionario';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar';
}

document.addEventListener('DOMContentLoaded', async function() {
    // Carga solo los cargos activos en el select
    const cargos = await fetchActivos('/api/cargos/activos');
    const cargoSelect = document.getElementById('cargo-select');
    cargos.forEach(c => {
        const o = document.createElement('option');
        o.value = c.codCargo;
        o.text = c.descripcion;
        cargoSelect.appendChild(o);
    });

    // Mostrar/ocultar el campo Cargo según el rol elegido y ajustar la fecha máxima
    const rolSelect = document.getElementById('rol-select');
    const cargoGroup = document.getElementById('cargo-group');
    const fechaInput = document.getElementById('fechaNacPersona');
    function actualizarMaxFecha() {
        // Feligrés: la fecha máxima es hoy. Funcionario: hoy menos 18 años.
        fechaInput.max = (rolSelect.value === 'FUNCIONARIO') ? fechaMaximaFuncionario() : hoyLocal();
    }
    rolSelect.addEventListener('change', function() {
        cargoGroup.classList.toggle('hidden', this.value !== 'FUNCIONARIO');
        actualizarMaxFecha();
    });
    actualizarMaxFecha();

    // Si se vino desde Usuarios para crear un funcionario, se preselecciona el rol Funcionario
    const paramsUrl = new URLSearchParams(window.location.search);
    if (paramsUrl.get('origen') === 'usuarios') {
        rolSelect.value = 'FUNCIONARIO';
        cargoGroup.classList.remove('hidden');
    }

    loadPersonas();

    // Guardar / actualizar el registro unificado
    // Valida y envia los datos de la persona o funcionario
    document.getElementById('persona-form').addEventListener('submit', async function(e) {
        e.preventDefault();

        const form = this;
        limpiarErroresFormulario(form);

        // Se revisan los mismos campos y reglas que ya tenía el formulario
        const campos = [
            'ciPersona', 'nombrePersona', 'apellidoPersona',
            'fechaNacPersona', 'telefonoPersona', 'direccionPersona', 'cargo-select'
        ].map(id => document.getElementById(id));

        const errores = erroresValidacionNativa(form);
        campos.forEach(campo => {
            const existente = errores.find(item => item.campo === campo);
            if (existente) {
                // Si ya hay un error nativo, se usa la regla personalizada para actualizarlo
                existente.validador = () => mensajeErrorPersonaCampo(campo);
                return;
            }
            const mensaje = mensajeErrorPersonaCampo(campo);
            if (mensaje) {
                errores.push({
                    campo,
                    mensaje,
                    validador: () => mensajeErrorPersonaCampo(campo)
                });
            }
        });

        if (errores.length) {
            mostrarErroresFormulario(form, errores);
            return;
        }

        // Construir el objeto con datos personales + rol + cargo
        const cargo = document.getElementById('cargo-select').value;
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
                // Si se llego desde Solicitudes para crear un solicitante, se regresa con el nuevo registro
                const params = new URLSearchParams(window.location.search);
                if (params.get('origen') === 'solicitudes') {
                    const saved = await res.json();
                    localStorage.setItem('spc_nuevo_solicitante', JSON.stringify({ tipo: 'FELIGRES', id: saved.ciPersona }));
                    window.location.href = 'solicitudes.html';
                    return;
                }
                // Si se llego desde Usuarios para crear un funcionario, se regresa con el nuevo registro
                if (params.get('origen') === 'usuarios') {
                    const saved = await res.json();
                    localStorage.setItem('spc_nuevo_funcionario', JSON.stringify({ id: saved.ciPersona }));
                    window.location.href = 'usuarios.html';
                    return;
                }
                resetForm();
                loadPersonas();
            } else {
                const err = await res.json().catch(() => null);
                const mensajeServidor = err && err.message ? err.message : 'Error al guardar el registro';
                const campo = campoPersonaPorMensaje(mensajeServidor);
                if (campo) {
                    mostrarErroresFormulario(form, [{
                        campo,
                        mensaje: mensajeServidor,
                        validador: () => mensajeErrorPersonaCampo(campo)
                    }]);
                } else {
                    showAlert(mensajeServidor, 'error');
                }
            }
        } catch (err) {
            showAlert('No se pudo conectar con el servidor', 'error');
        }
    });

    // Filtrar la tabla al escribir o cambiar seleccion
    document.getElementById('search-personas').addEventListener('input', renderPersonas);
    document.getElementById('filtro-estado').addEventListener('change', renderPersonas);
    document.getElementById('filtro-rol').addEventListener('change', renderPersonas);
    document.getElementById('orden-personas').addEventListener('change', renderPersonas);
});
