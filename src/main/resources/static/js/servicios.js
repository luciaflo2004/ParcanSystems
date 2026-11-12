// ====== Lógica de la interfaz de Servicios ======
let serviciosData = [];
let editingServicio = null;
// Guarda la fecha de cada solicitud para validar la fecha de realización
let fechasSolicitud = {};

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

// Formatea un funcionario como "Nombre Apellido – Cargo" para mostrarlo en el select y en la tabla
function textoFuncionario(f) {
    const nombre = f.persona ? ((f.persona.nombrePersona || '') + ' ' + (f.persona.apellidoPersona || '')).trim() : (f.ciPersona || '');
    const cargo = f.cargo ? f.cargo.descripcion : '';
    return nombre + (cargo ? ' – ' + cargo : '');
}

// Aplica filtros (texto, estado) y renderiza la tabla
function renderServicios() {
    const texto = (document.getElementById('search-servicios').value || '').toLowerCase();
    const estadoF = document.getElementById('filtro-estado').value;

    // Ordena los servicios por codigo ascendente
    const rows = ordenarPorId(serviciosData.filter(s => {
        const bloque = [
            s.detalleServicio || '',
            (s.funcionario ? textoFuncionario(s.funcionario) : ''),
            (s.solicitud ? textoSolicitud(s.solicitud) : '')
        ].join(' ').toLowerCase();
        if (texto && !bloque.includes(texto)) return false;
        const estado = s.realizado ? 'realizado' : 'pendiente';
        if (estadoF !== 'todos' && estado !== estadoF) return false;
        return true;
    }), 'codServicio').map(s => ({
        Cod: s.codServicio,
        Detalle: s.detalleServicio,
        Costo: (s.costoServicio != null ? formatoMoneda(s.costoServicio) : '-'),
        Estado: s.realizado ? 'Realizado' : 'Pendiente',
        Fecha: s.fechaRealizacion || '-',
        Funcionario: (s.funcionario ? textoFuncionario(s.funcionario) : '-'),
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
    // Solo se muestran solicitudes activas para registrar servicios nuevos
    const solicitudes = await fetchActivos('/api/solicitudes/activas');
    const sel = document.getElementById('solicitud-select');
    sel.innerHTML = '<option value="">Seleccione...</option>';
    fechasSolicitud = {};
    solicitudes.forEach(s => {
        fechasSolicitud[s.codSolicitud] = s.fechaSolicitud;
        const o = document.createElement('option');
        o.value = s.codSolicitud;
        o.text = textoSolicitud(s);
        sel.appendChild(o);
    });
}

// Ajusta los limites de la fecha de realización según la solicitud seleccionada
function actualizarLimitesFechaRealizacion(mostrarError) {
    const solicitudSelect = document.getElementById('solicitud-select');
    const fechaInput = document.getElementById('fechaRealizacion');
    const fechaSolicitud = fechasSolicitud[solicitudSelect.value] || '';

    fechaInput.required = true;
    fechaInput.min = fechaSolicitud;
    fechaInput.max = hoyLocal();

    // Si el usuario ya escribio una fecha, se revisa al cambiar la solicitud
    if (mostrarError && fechaInput.value) {
        const mensaje = mensajeErrorServicioCampo(fechaInput);
        if (mensaje) {
            mostrarErrorCampo(fechaInput, mensaje, () => mensajeErrorServicioCampo(fechaInput));
        } else {
            limpiarErrorCampo(fechaInput);
        }
    }
}


// Carga los datos del servicio seleccionado en el formulario
function editarServicio(cod) {
    const s = serviciosData.find(x => x.codServicio === cod);
    if (!s) return;
    editingServicio = cod;
    limpiarErroresFormulario(document.getElementById('servicio-form'));
    document.getElementById('detalleServicio').value = s.detalleServicio || '';
    document.getElementById('costoServicio').value =
        (s.costoServicio != null ? formatoMoneda(s.costoServicio) : '');
    document.getElementById('realizado').value = s.realizado ? 'true' : 'false';
    document.getElementById('fechaRealizacion').value = s.fechaRealizacion || '';
    // Se quitan opciones historicas de una edicion anterior
    const selF = document.getElementById('func-select');
    const selS = document.getElementById('solicitud-select');
    limpiarOpcionesHistoricas(selF);
    limpiarOpcionesHistoricas(selS);
    // Precargar el funcionario asociado al servicio (aunque este inactivo)
    if (s.funcionario) {
        agregarOpcionSiFalta(selF, s.funcionario.ciPersona, textoFuncionario(s.funcionario), true);
        selF.value = s.funcionario.ciPersona;
    }
    // Precargar la solicitud asociada al servicio (aunque este inactiva)
    if (s.solicitud) {
        fechasSolicitud[s.solicitud.codSolicitud] = s.solicitud.fechaSolicitud;
        agregarOpcionSiFalta(selS, s.solicitud.codSolicitud, textoSolicitud(s.solicitud), true);
        selS.value = s.solicitud.codSolicitud;
    }
    // Ajustar los limites de la fecha según la solicitud cargada
    actualizarLimitesFechaRealizacion();
    document.getElementById('form-title').textContent = 'Editar Servicio';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Actualizar';
    document.getElementById('servicio-form').scrollIntoView({ behavior: 'smooth' });
}

// Limpia el formulario para registrar un servicio nuevo
function resetServicioForm() {
    editingServicio = null;
    document.getElementById('servicio-form').reset();
    limpiarErroresFormulario(document.getElementById('servicio-form'));
    // Al registrar un servicio nuevo no se muestran registros historicos inactivos
    limpiarOpcionesHistoricas(document.getElementById('func-select'));
    limpiarOpcionesHistoricas(document.getElementById('solicitud-select'));
    document.getElementById('solicitud-select').value = '';
    document.getElementById('func-select').value = '';
    actualizarLimitesFechaRealizacion(false);
    document.getElementById('form-title').textContent = 'Registrar Servicio';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar';
}

// Devuelve el mensaje de error de un campo de Servicio, o null si es válido
function mensajeErrorServicioCampo(campo) {
    if (!campo) return null;
    const valor = campo.value;

    if (campo.id === 'solicitud-select' && !valor) {
        return 'Debe seleccionar una solicitud';
    }
    if (campo.id === 'func-select' && !valor) {
        return 'Debe seleccionar un funcionario';
    }
    if (campo.id === 'detalleServicio') {
        if (!valor || !valor.trim()) return 'El detalle del servicio es obligatorio';
        if (valor.length > 50) return 'El detalle no puede superar los 50 caracteres';
        if (tieneEspaciosConsecutivos(valor)) return 'No se permiten más de dos espacios consecutivos';
        if (tieneCaracteresConsecutivos(valor, 2)) return 'No se permiten más de dos caracteres iguales consecutivos';
    }
    if (campo.id === 'costoServicio') {
        const numero = parsearMoneda(valor);
        if (valor.trim() !== '' && (isNaN(numero) || numero < 0)) {
            return 'El costo debe ser un número válido y no negativo';
        }
    }
    if (campo.id === 'fechaRealizacion') {
        const solicitudSelect = document.getElementById('solicitud-select');
        if (!solicitudSelect.value) {
            return 'No se puede validar la fecha de realización. Seleccione primero una solicitud válida.';
        }
        if (!valor) return 'Fecha inválida. Debe ingresar una fecha de realización.';
        if (!esFechaISOValida(valor)) return 'Fecha inválida. Ingrese una fecha de realización válida.';

        const fechaSolicitud = fechasSolicitud[solicitudSelect.value];
        if (!fechaSolicitud) {
            return 'No se puede validar la fecha de realización. Seleccione primero una solicitud válida.';
        }
        if (valor < fechaSolicitud) {
            return 'Fecha inválida. Ingrese otra fecha. La fecha de realización no puede ser anterior a la fecha de solicitud.';
        }
        if (valor > hoyLocal()) {
            return 'Fecha inválida. Ingrese otra fecha. La fecha de realización no puede ser posterior a la fecha actual.';
        }
    }
    return null;
}

// Busca a qué campo corresponde un mensaje devuelto por el backend
function campoServicioPorMensaje(mensaje) {
    const texto = (mensaje || '').toLowerCase();
    if (texto.includes('fecha')) return document.getElementById('fechaRealizacion');
    if (texto.includes('solicitud')) return document.getElementById('solicitud-select');
    if (texto.includes('funcionario')) return document.getElementById('func-select');
    if (texto.includes('detalle')) return document.getElementById('detalleServicio');
    if (texto.includes('costo')) return document.getElementById('costoServicio');
    return null;
}

// Desactiva el servicio seleccionado
async function desactivarServicio(cod) {
    if (!confirm('¿Está seguro que desea desactivar este servicio?')) return;
    const res = await fetch('/api/servicios/' + cod, { method: 'DELETE' });
    if (res.ok) { showAlert('Servicio desactivado', 'success'); loadServicios(); }
    else showAlert('Error al desactivar el servicio', 'error');
}

// Reactiva un servicio desactivado
async function activarServicio(cod) {
    const res = await fetch('/api/servicios/' + cod + '/restore', { method: 'POST' });
    if (res.ok) { showAlert('Servicio activado', 'success'); loadServicios(); }
    else showAlert('Error al activar el servicio', 'error');
}

document.addEventListener('DOMContentLoaded', async function() {
    // Carga solo funcionarios activos (con su persona activa) y muestra nombre + cargo
    const funcionarios = (await fetchActivos('/api/funcionarios/activos'))
        .filter(funcionarioActivo);
    const sel = document.getElementById('func-select');
    sel.innerHTML = '<option value="">Seleccione...</option>';
    funcionarios.forEach(f => {
        const o = document.createElement('option');
        o.value = f.ciPersona;
        o.text = textoFuncionario(f);
        sel.appendChild(o);
    });

    // Cargar las solicitudes en el desplegable "Solicitud"
    await cargarSolicitudes();

    // Ajustar los limites iniciales de la fecha de realización
    actualizarLimitesFechaRealizacion(false);
    document.getElementById('solicitud-select').addEventListener('change', function() {
        actualizarLimitesFechaRealizacion(true);
    });

    // Buscador por detalle de solicitud y nombre del solicitante
    crearBuscadorSelect('solicitud-select', 'solicitud-busqueda');

    // Formato monetario del costo: se muestra con separadores y "Gs." al salir del campo
    const costoInput = document.getElementById('costoServicio');
    costoInput.addEventListener('focus', function() {
        const n = parsearMoneda(this.value);
        if (!isNaN(n)) this.value = String(n);
    });
    costoInput.addEventListener('blur', function() {
        const n = parsearMoneda(this.value);
        if (this.value.trim() !== '' && !isNaN(n)) this.value = formatoMoneda(n);
    });

    loadServicios();

    // Valida y envia los datos del servicio
    document.getElementById('servicio-form').addEventListener('submit', async function(e) {
        e.preventDefault();

        const form = this;
        limpiarErroresFormulario(form);

        // Se revisan las mismas reglas que ya tenía el formulario
        const campos = ['solicitud-select', 'func-select', 'detalleServicio', 'costoServicio', 'fechaRealizacion']
            .map(id => document.getElementById(id));
        const errores = erroresValidacionNativa(form);
        campos.forEach(campo => {
            const existente = errores.find(item => item.campo === campo);
            const mensaje = mensajeErrorServicioCampo(campo);
            if (existente) {
                existente.validador = () => mensajeErrorServicioCampo(campo);
                if (campo.id === 'fechaRealizacion' && mensaje) {
                    existente.mensaje = mensaje;
                }
                return;
            }
            if (mensaje) {
                errores.push({
                    campo,
                    mensaje,
                    validador: () => mensajeErrorServicioCampo(campo)
                });
            }
        });
        if (errores.length) {
            mostrarErroresFormulario(form, errores);
            return;
        }

        const solSel = document.getElementById('solicitud-select');
        const funcSel = document.getElementById('func-select');
        const detalleServicio = document.getElementById('detalleServicio').value;
        const costoTexto = document.getElementById('costoServicio').value;
        const costoNumero = parsearMoneda(costoTexto);

        const obj = {
            detalleServicio: detalleServicio || null,
            costoServicio: costoTexto.trim() !== '' ? costoNumero : null,
            realizado: document.getElementById('realizado').value === 'true',
            fechaRealizacion: document.getElementById('fechaRealizacion').value || null,
            funcionario: { ciPersona: Number(funcSel.value) },
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
            const mensajeServidor = err && err.message ? err.message : 'Error al guardar el servicio';
            const campo = campoServicioPorMensaje(mensajeServidor);
            if (campo) {
                mostrarErroresFormulario(form, [{
                    campo,
                    mensaje: mensajeServidor,
                    validador: () => mensajeErrorServicioCampo(campo)
                }]);
            } else {
                showAlert(mensajeServidor, 'error');
            }
        }
    });

    // Filtrar al escribir o cambiar seleccion
    document.getElementById('search-servicios').addEventListener('input', renderServicios);
    document.getElementById('filtro-estado').addEventListener('change', renderServicios);
});
