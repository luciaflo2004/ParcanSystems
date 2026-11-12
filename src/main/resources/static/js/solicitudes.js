// ====== Lógica de la interfaz de Solicitudes ======
let solicitudesData = [];
let editingSolicitud = null;

// Listas de solicitantes y registro pendiente de auto-seleccion al volver de otro modulo
let personasSolicitantes = [];
let institucionesSolicitantes = [];
let nuevoSolicitantePendiente = null;

// Devuelve el nombre del solicitante (feligrés o institución) para mostrarlo en la tabla
function nombreSolicitante(s) {
    if (s.feligres) return (s.feligres.nombrePersona || '') + ' ' + (s.feligres.apellidoPersona || '');
    if (s.institucion) return s.institucion.nombre || '';
    return '';
}

// Aplica filtros (texto, estado) y renderiza la tabla
function renderSolicitudes() {
    const texto = (document.getElementById('search-solicitudes').value || '').toLowerCase();
    const estadoF = document.getElementById('filtro-estado').value;

    // Ordena las solicitudes por codigo ascendente
    const rows = ordenarPorId(solicitudesData.filter(s => {
        const bloque = [s.detalle || '', nombreSolicitante(s)].join(' ').toLowerCase();
        if (texto && !bloque.includes(texto)) return false;
        const estado = s.aprobada ? 'aprobada' : 'pendiente';
        if (estadoF !== 'todos' && estado !== estadoF) return false;
        return true;
    }), 'codSolicitud').map(s => ({
        Cod: s.codSolicitud,
        Detalle: s.detalle,
        Fecha: s.fechaSolicitud,
        Estado: s.aprobada ? 'Aprobada' : 'Pendiente',
        Solicitante: nombreSolicitante(s),
        Reg: s.eliminado ? 'Inactivo' : 'Activo'
    }));

    renderTable('solicitudes-list', ['Cod','Detalle','Fecha','Estado','Solicitante','Reg'], rows, (row) => {
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

// Carga los solicitantes activos (personas e instituciones) en sus desplegables
async function cargarSolicitantes() {
    // Solo se ofrecen personas e instituciones activas para solicitudes nuevas
    personasSolicitantes = await fetchActivos('/api/personas/activas');
    institucionesSolicitantes = await fetchActivos('/api/instituciones/activas');

    const selF = document.getElementById('feligres-select');
    selF.innerHTML = '<option value="">Seleccione...</option>';
    personasSolicitantes.forEach(p => {
        const o = document.createElement('option');
        o.value = p.ciPersona;
        o.text = (p.nombrePersona || '') + ' ' + (p.apellidoPersona || '');
        selF.appendChild(o);
    });

    const selI = document.getElementById('institucion-select');
    selI.innerHTML = '<option value="">Seleccione...</option>';
    institucionesSolicitantes.forEach(i => {
        const o = document.createElement('option');
        o.value = i.idInstitucionEclesiastica;
        o.text = i.nombre || '';
        selI.appendChild(o);
    });
}

// Muestra u oculta el desplegable de solicitante según el tipo elegido
function mostrarGrupoSolicitante() {
    const tipo = document.getElementById('tipo-solicitante').value;
    document.getElementById('feligres-group').classList.toggle('hidden', tipo !== 'FELIGRES');
    document.getElementById('institucion-group').classList.toggle('hidden', tipo !== 'INSTITUCION');
}

// Carga los datos de la solicitud seleccionada en el formulario
function editarSolicitud(cod) {
    const s = solicitudesData.find(x => x.codSolicitud === cod);
    if (!s) return;
    editingSolicitud = cod;
    limpiarErroresFormulario(document.getElementById('solicitud-form'));
    document.getElementById('detalle').value = s.detalle || '';
    document.getElementById('fechaSolicitud').value = s.fechaSolicitud || '';
    document.getElementById('aprobada').value = s.aprobada ? 'true' : 'false';

    // Se quitan opciones historicas de una edicion anterior
    const selF = document.getElementById('feligres-select');
    const selI = document.getElementById('institucion-select');
    limpiarOpcionesHistoricas(selF);
    limpiarOpcionesHistoricas(selI);

    // Precargan el tipo de solicitante y el registro correspondiente
    const tipoSelect = document.getElementById('tipo-solicitante');
    if (s.institucion) {
        tipoSelect.value = 'INSTITUCION';
        mostrarGrupoSolicitante();
        agregarOpcionSiFalta(selI, s.institucion.idInstitucionEclesiastica,
            s.institucion.nombre || '', true);
        selI.value = s.institucion.idInstitucionEclesiastica;
    } else if (s.feligres) {
        tipoSelect.value = 'FELIGRES';
        mostrarGrupoSolicitante();
        agregarOpcionSiFalta(selF, s.feligres.ciPersona,
            (s.feligres.nombrePersona || '') + ' ' + (s.feligres.apellidoPersona || ''), true);
        selF.value = s.feligres.ciPersona;
    } else {
        tipoSelect.value = '';
        mostrarGrupoSolicitante();
    }

    document.getElementById('form-title').textContent = 'Editar Solicitud';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Actualizar';
    document.getElementById('solicitud-form').scrollIntoView({ behavior: 'smooth' });
}

// Limpia el formulario para registrar una solicitud nueva
function resetSolicitudForm() {
    editingSolicitud = null;
    document.getElementById('solicitud-form').reset();
    limpiarErroresFormulario(document.getElementById('solicitud-form'));
    // Al registrar una solicitud nueva no se muestran solicitantes historicos inactivos
    limpiarOpcionesHistoricas(document.getElementById('feligres-select'));
    limpiarOpcionesHistoricas(document.getElementById('institucion-select'));
    document.getElementById('tipo-solicitante').value = '';
    document.getElementById('feligres-select').value = '';
    document.getElementById('institucion-select').value = '';
    mostrarGrupoSolicitante();
    // Se elimina el borrador guardado al limpiar o guardar
    localStorage.removeItem('spc_solicitud_draft');
    document.getElementById('form-title').textContent = 'Registrar Solicitud';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar';
}

// Guarda los datos ya introducidos para no perderlos al ir a crear un solicitante
function guardarDraftSolicitud() {
    const draft = {
        editing: editingSolicitud,
        detalle: document.getElementById('detalle').value || '',
        fechaSolicitud: document.getElementById('fechaSolicitud').value || '',
        aprobada: document.getElementById('aprobada').value || 'false',
        tipoSolicitante: document.getElementById('tipo-solicitante').value || ''
    };
    localStorage.setItem('spc_solicitud_draft', JSON.stringify(draft));
}

// Restaura los datos introducidos (si existen) al volver a Solicitudes
function restaurarDraftSolicitud() {
    const raw = localStorage.getItem('spc_solicitud_draft');
    if (!raw) return;
    try {
        const draft = JSON.parse(raw);
        editingSolicitud = draft.editing || null;
        document.getElementById('detalle').value = draft.detalle || '';
        document.getElementById('fechaSolicitud').value = draft.fechaSolicitud || '';
        document.getElementById('aprobada').value = draft.aprobada || 'false';
        document.getElementById('tipo-solicitante').value = draft.tipoSolicitante || '';
        mostrarGrupoSolicitante();
        if (editingSolicitud) {
            document.getElementById('form-title').textContent = 'Editar Solicitud';
            document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Actualizar';
        }
    } catch (e) { /* datos de borrador inválidos: se ignoran */ }
}

// Redirige al formulario de Personas conservando el borrador de la solicitud
function crearNuevaPersona() {
    guardarDraftSolicitud();
    window.location.href = 'personas.html?origen=solicitudes';
}

// Redirige al formulario de Instituciones conservando el borrador de la solicitud
function crearNuevaInstitucion() {
    guardarDraftSolicitud();
    window.location.href = 'instituciones.html?origen=solicitudes';
}

// Devuelve el mensaje de error de un campo de Solicitud, o null si es válido
function mensajeErrorSolicitudCampo(campo) {
    if (!campo) return null;
    const tipo = document.getElementById('tipo-solicitante').value;
    const valor = campo.value;

    if (campo.id === 'tipo-solicitante' && !valor) {
        return 'Debe seleccionar un tipo de solicitante';
    }
    if (campo.id === 'feligres-select' && tipo === 'FELIGRES' && !valor) {
        return 'Debe seleccionar un feligrés válido';
    }
    if (campo.id === 'institucion-select' && tipo === 'INSTITUCION' && !valor) {
        return 'Debe seleccionar una institución válida';
    }
    if (campo.id === 'detalle') {
        if (!valor || !valor.trim()) return 'El detalle es obligatorio';
        if (valor.length > 50) return 'El detalle no puede superar los 50 caracteres';
        if (tieneEspaciosConsecutivos(valor)) return 'No se permiten más de dos espacios consecutivos';
        if (tieneCaracteresConsecutivos(valor, 2)) return 'No se permiten más de dos caracteres iguales consecutivos';
    }
    if (campo.id === 'fechaSolicitud') {
        if (!valor) return 'Fecha inválida. Debe ingresar una fecha de solicitud.';
        if (!esFechaISOValida(valor)) return 'Fecha inválida. Ingrese una fecha de solicitud válida.';
        const hoy = hoyLocal();
        const fechaMinima = fechaHaceMeses(6);
        if (valor < fechaMinima) {
            return 'Fecha inválida. Ingrese otra fecha. La fecha de solicitud no puede ser anterior a los últimos seis meses.';
        }
        if (valor > hoy) {
            return 'Fecha inválida. Ingrese otra fecha. La fecha de solicitud no puede ser posterior a la fecha actual.';
        }
    }
    return null;
}

// Busca a qué campo corresponde un mensaje devuelto por el backend
function campoSolicitudPorMensaje(mensaje) {
    const texto = (mensaje || '').toLowerCase();
    if (texto.includes('feligrés') || texto.includes('feligres')) return document.getElementById('feligres-select');
    if (texto.includes('institución') || texto.includes('institucion')) return document.getElementById('institucion-select');
    if (texto.includes('detalle')) return document.getElementById('detalle');
    if (texto.includes('fecha')) return document.getElementById('fechaSolicitud');
    if (texto.includes('solicitante')) return document.getElementById('tipo-solicitante');
    return null;
}

// Desactiva la solicitud seleccionada
async function desactivarSolicitud(cod) {
    if (!confirm('¿Está seguro que desea desactivar esta solicitud?')) return;
    const res = await fetch('/api/solicitudes/' + cod, { method: 'DELETE' });
    if (res.ok) { showAlert('Solicitud desactivada', 'success'); loadSolicitudes(); }
    else showAlert('Error al desactivar la solicitud', 'error');
}

// Reactiva una solicitud desactivada
async function activarSolicitud(cod) {
    const res = await fetch('/api/solicitudes/' + cod + '/restore', { method: 'POST' });
    if (res.ok) { showAlert('Solicitud activada', 'success'); loadSolicitudes(); }
    else showAlert('Error al activar la solicitud', 'error');
}

document.addEventListener('DOMContentLoaded', async function() {
    // Restaurar los datos del borrador (si se viene de crear un solicitante)
    restaurarDraftSolicitud();

    // Recuperar el solicitante recién creado para auto-seleccionarlo al volver
    const nuevoRaw = localStorage.getItem('spc_nuevo_solicitante');
    if (nuevoRaw) {
        localStorage.removeItem('spc_nuevo_solicitante');
        try { nuevoSolicitantePendiente = JSON.parse(nuevoRaw); } catch (e) { /* se ignora */ }
    }

    await cargarSolicitantes();

    // Buscadores por nombre dentro de los desplegables de solicitantes
    crearBuscadorSelect('feligres-select', 'feligres-busqueda');
    crearBuscadorSelect('institucion-select', 'institucion-busqueda');

    // Aplicar la auto-selección del nuevo solicitante, si corresponde
    if (nuevoSolicitantePendiente) {
        const tipoSelect = document.getElementById('tipo-solicitante');
        tipoSelect.value = nuevoSolicitantePendiente.tipo;
        mostrarGrupoSolicitante();
        if (nuevoSolicitantePendiente.tipo === 'FELIGRES') {
            document.getElementById('feligres-select').value = nuevoSolicitantePendiente.id;
        } else if (nuevoSolicitantePendiente.tipo === 'INSTITUCION') {
            document.getElementById('institucion-select').value = nuevoSolicitantePendiente.id;
        }
        showAlert('Solicitante creado y seleccionado', 'success');
        nuevoSolicitantePendiente = null;
    }

    // Configura los limites de la fecha de solicitud
    const fechaSolicitudInput = document.getElementById('fechaSolicitud');
    fechaSolicitudInput.required = true;
    fechaSolicitudInput.min = fechaHaceMeses(6);
    fechaSolicitudInput.max = hoyLocal();

    loadSolicitudes();

    // Al cambiar el tipo se limpia la selección anterior y se muestra el desplegable correcto
    document.getElementById('tipo-solicitante').addEventListener('change', function() {
        document.getElementById('feligres-select').value = '';
        document.getElementById('institucion-select').value = '';
        limpiarErrorCampo(document.getElementById('feligres-select'));
        limpiarErrorCampo(document.getElementById('institucion-select'));
        mostrarGrupoSolicitante();
    });

    // Valida y envia los datos de la solicitud
    document.getElementById('solicitud-form').addEventListener('submit', async function(e) {
        e.preventDefault();

        const form = this;
        limpiarErroresFormulario(form);

        // Se revisan las mismas reglas que ya tenía el formulario
        const campos = ['tipo-solicitante', 'feligres-select', 'institucion-select', 'detalle', 'fechaSolicitud']
            .map(id => document.getElementById(id));
        const errores = erroresValidacionNativa(form);
        campos.forEach(campo => {
            const existente = errores.find(item => item.campo === campo);
            const mensaje = mensajeErrorSolicitudCampo(campo);
            if (existente) {
                existente.validador = () => mensajeErrorSolicitudCampo(campo);
                if (campo.id === 'fechaSolicitud' && mensaje) {
                    existente.mensaje = mensaje;
                }
                return;
            }
            if (mensaje) {
                errores.push({
                    campo,
                    mensaje,
                    validador: () => mensajeErrorSolicitudCampo(campo)
                });
            }
        });
        if (errores.length) {
            mostrarErroresFormulario(form, errores);
            return;
        }

        const tipo = document.getElementById('tipo-solicitante').value;
        let feligres = null;
        let institucion = null;
        if (tipo === 'FELIGRES') {
            feligres = { ciPersona: Number(document.getElementById('feligres-select').value) };
        } else {
            institucion = { idInstitucionEclesiastica: Number(document.getElementById('institucion-select').value) };
        }

        // Validar el detalle
        const detalle = document.getElementById('detalle').value;

        // Se arma el objeto con la relación del solicitante elegido (solo una de las dos)
        const obj = {
            detalle: detalle || null,
            fechaSolicitud: document.getElementById('fechaSolicitud').value || null,
            aprobada: document.getElementById('aprobada').value === 'true',
            feligres: feligres,
            institucion: institucion
        };

        const url = editingSolicitud ? ('/api/solicitudes/' + editingSolicitud) : '/api/solicitudes';
        const method = editingSolicitud ? 'PUT' : 'POST';
        const res = await fetch(url, { method: method, headers:{'Content-Type':'application/json'}, body: JSON.stringify(obj) });
        if (res.ok) {
            showAlert(editingSolicitud ? 'Solicitud actualizada' : 'Solicitud registrada', 'success');
            resetSolicitudForm();
            loadSolicitudes();
        } else {
            const err = await res.json().catch(() => null);
            const mensajeServidor = err && err.message ? err.message : 'Error al guardar la solicitud';
            const campo = campoSolicitudPorMensaje(mensajeServidor);
            if (campo) {
                mostrarErroresFormulario(form, [{
                    campo,
                    mensaje: mensajeServidor,
                    validador: () => mensajeErrorSolicitudCampo(campo)
                }]);
            } else {
                showAlert(mensajeServidor, 'error');
            }
        }
    });

    // Filtrar al escribir o cambiar seleccion
    document.getElementById('search-solicitudes').addEventListener('input', renderSolicitudes);
    document.getElementById('filtro-estado').addEventListener('change', renderSolicitudes);
});
