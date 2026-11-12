// ====== Lógica de la interfaz de Usuarios ======
let usuariosData = [];
let funcionariosData = [];
let cargosData = [];
let editingUsuario = null;
let nuevoFuncionarioPendiente = null;

// Devuelve únicamente el nombre y apellido del funcionario
function nombreFuncionario(f) {
    if (!f) return '';
    if (f.persona) {
        return ((f.persona.nombrePersona || '') + ' ' + (f.persona.apellidoPersona || '')).trim();
    }
    return String(f.ciPersona || '');
}

// Cargo asignado al usuario; si no tiene, se usa el cargo de su funcionario
function cargoDeUsuario(u) {
    if (u && u.cargo) return u.cargo;
    if (u && u.funcionario && u.funcionario.cargo) return u.funcionario.cargo;
    return null;
}

// Cargos activos del funcionario: su propio cargo y los ya asignados a sus usuarios
function cargosDeFuncionario(f) {
    if (!f) return [];
    const cargos = new Map();
    const agregar = (c) => {
        // Solo se ofrecen cargos activos para nuevas asignaciones
        if (c && c.codCargo != null && !c.eliminado && !cargos.has(c.codCargo)) {
            cargos.set(c.codCargo, c);
        }
    };
    agregar(f.cargo);
    usuariosData.forEach(u => {
        if (u.funcionario && Number(u.funcionario.ciPersona) === Number(f.ciPersona)) agregar(u.cargo);
    });
    return Array.from(cargos.values());
}

// Rellena el desplegable Cargo; selecciona el indicado o el único disponible
function llenarSelectCargos(cargos, codSeleccionado, cargoHistorico) {
    const sel = document.getElementById('cargo-select');
    const lista = Array.isArray(cargos) ? cargos : [];
    sel.innerHTML = '<option value="">Seleccione un cargo...</option>';
    lista.forEach(c => {
        const o = document.createElement('option');
        o.value = c.codCargo;
        o.text = c.descripcion;
        sel.appendChild(o);
    });
    const cod = codSeleccionado != null ? Number(codSeleccionado) : null;
    if (cod != null && lista.some(c => Number(c.codCargo) === cod)) {
        sel.value = String(cod);
    } else if (cod != null && cargoHistorico) {
        // Se muestra el cargo historico solo al editar un usuario que ya lo tenia
        agregarOpcionSiFalta(sel, cargoHistorico.codCargo, cargoHistorico.descripcion, true);
        sel.value = String(cod);
    } else if (lista.length === 1) {
        sel.value = String(lista[0].codCargo);
    } else {
        sel.value = '';
    }
}

// Funcionario actualmente seleccionado en el formulario
function obtenerFuncionarioSeleccionado() {
    const sel = document.getElementById('funcionario-select');
    if (!sel.value) return null;
    const ci = Number(sel.value);
    return funcionariosData.find(f => Number(f.ciPersona) === ci) || null;
}

// Ajusta el desplegable Cargo según el funcionario elegido
function actualizarCargosFuncionario(codSeleccionado, cargoHistorico) {
    const f = obtenerFuncionarioSeleccionado();
    if (!f) {
        // Sin funcionario se ofrecen todos los cargos activos
        llenarSelectCargos(cargosData, codSeleccionado, cargoHistorico);
        return;
    }
    let cargos = cargosDeFuncionario(f);
    // Si el funcionario no tiene cargos activos, se ofrecen todos como respaldo
    if (!cargos.length) cargos = cargosData;
    llenarSelectCargos(cargos, codSeleccionado, cargoHistorico);
}

// Aplica filtros (texto, estado) y renderiza la tabla
function renderUsuarios() {
    const texto = (document.getElementById('search-usuarios').value || '').toLowerCase();
    const estadoF = document.getElementById('filtro-estado').value;

    // Ordena los usuarios por ID ascendente
    const rows = ordenarPorId(usuariosData.filter(u => {
        const cargo = cargoDeUsuario(u);
        const bloque = [
            u.usuario || '',
            String(u.idContraseña),
            (u.funcionario ? nombreFuncionario(u.funcionario) : ''),
            (u.funcionario ? String(u.funcionario.ciPersona) : ''),
            (cargo ? cargo.descripcion : '')
        ].join(' ').toLowerCase();
        if (texto && !bloque.includes(texto)) return false;
        const estado = u.eliminado ? 'inactivo' : 'activo';
        if (estadoF !== 'todos' && estado !== estadoF) return false;
        return true;
    }), 'idContraseña').map(u => {
        const cargo = cargoDeUsuario(u);
        return {
            ID: u.idContraseña,
            Usuario: u.usuario,
            Funcionario: (u.funcionario ? nombreFuncionario(u.funcionario) : '-'),
            Cargo: (cargo ? cargo.descripcion : '-'),
            Estado: u.eliminado ? 'Inactivo' : 'Activo'
        };
    });

    renderTable('usuarios-list', ['ID','Usuario','Funcionario','Cargo','Estado'], rows, (row) => {
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

// Carga los funcionarios activos en el desplegable "Funcionario"
async function cargarFuncionarios() {
    // Se guarda la lista completa para poder mostrar datos historicos al editar
    funcionariosData = await fetchAll('/api/funcionarios');
    const sel = document.getElementById('funcionario-select');
    sel.innerHTML = '<option value="">Seleccione funcionario...</option>';
    // En el desplegable solo se muestran funcionarios activos
    funcionariosData.filter(funcionarioActivo).forEach(f => {
        const o = document.createElement('option');
        o.value = f.ciPersona;
        o.text = nombreFuncionario(f);
        sel.appendChild(o);
    });
}

// Carga los cargos activos en el desplegable "Cargo"
async function cargarCargos() {
    cargosData = await fetchActivos('/api/cargos/activos');
    llenarSelectCargos(cargosData, null);
}

// Carga los datos del usuario seleccionado en el formulario
function editarUsuario(id) {
    const u = usuariosData.find(x => x.idContraseña === id);
    if (!u) return;
    editingUsuario = id;
    limpiarErroresFormulario(document.getElementById('usuario-form'));
    document.getElementById('usuario').value = u.usuario || '';
    // La contraseña no se muestra al editar; queda vacía y solo se actualiza si se ingresa una nueva
    document.getElementById('contrasena').value = '';
    // Precargar el funcionario asociado, aunque este inactivo
    const selF = document.getElementById('funcionario-select');
    limpiarOpcionesHistoricas(selF);
    if (u.funcionario) {
        agregarOpcionSiFalta(selF, u.funcionario.ciPersona, nombreFuncionario(u.funcionario), true);
        selF.value = u.funcionario.ciPersona;
    } else {
        selF.value = '';
    }
    // Precargar el cargo asociado (o el del funcionario si el usuario no tiene)
    const cargo = cargoDeUsuario(u);
    actualizarCargosFuncionario(cargo ? cargo.codCargo : null, cargo);
    document.getElementById('form-title').textContent = 'Editar Usuario';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Actualizar';
    document.getElementById('usuario-form').scrollIntoView({ behavior: 'smooth' });
}

// Limpia el formulario para registrar un usuario nuevo
function resetUsuarioForm() {
    editingUsuario = null;
    document.getElementById('usuario-form').reset();
    limpiarErroresFormulario(document.getElementById('usuario-form'));
    // Al registrar un usuario nuevo se quitan las opciones historicas inactivas
    limpiarOpcionesHistoricas(document.getElementById('funcionario-select'));
    document.getElementById('funcionario-select').value = '';
    actualizarCargosFuncionario(null);
    // Se elimina el borrador guardado al limpiar o guardar
    localStorage.removeItem('spc_usuario_draft');
    document.getElementById('form-title').textContent = 'Registrar Usuario';
    document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Guardar';
}

// Guarda los datos ya introducidos para no perderlos al ir a crear un funcionario
function guardarDraftUsuario() {
    const draft = {
        editing: editingUsuario,
        usuario: document.getElementById('usuario').value || ''
    };
    localStorage.setItem('spc_usuario_draft', JSON.stringify(draft));
}

// Restaura los datos introducidos (si existen) al volver a Usuarios
function restaurarDraftUsuario() {
    const raw = localStorage.getItem('spc_usuario_draft');
    if (!raw) return;
    try {
        const draft = JSON.parse(raw);
        editingUsuario = draft.editing || null;
        document.getElementById('usuario').value = draft.usuario || '';
        if (editingUsuario) {
            document.getElementById('form-title').textContent = 'Editar Usuario';
            document.getElementById('submit-btn').innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Actualizar';
        }
    } catch (e) { /* datos de borrador inválidos: se ignoran */ }
}

// Redirige al formulario de Personas (donde se registran los funcionarios) conservando el borrador
function crearFuncionario() {
    guardarDraftUsuario();
    window.location.href = 'personas.html?origen=usuarios';
}

// Muestra u oculta la contraseña introducida (nunca recupera contraseñas guardadas)
function togglePassword() {
    const input = document.getElementById('contrasena');
    const icon = document.getElementById('toggle-password-icon');
    const btn = document.getElementById('toggle-password');
    if (input.type === 'password') {
        input.type = 'text';
        icon.className = 'fa-solid fa-eye-slash';
        btn.setAttribute('aria-label', 'Ocultar contraseña');
    } else {
        input.type = 'password';
        icon.className = 'fa-solid fa-eye';
        btn.setAttribute('aria-label', 'Mostrar contraseña');
    }
}

// Valida los requisitos de la contraseña; devuelve el mensaje de error o null si es válida
function validarContrasenaLocal(contrasena, usuario) {
    if (!contrasena) return 'La contraseña es obligatoria';
    if (contrasena.length < 8) return 'La contraseña debe tener al menos 8 caracteres';
    if (!/[A-Z]/.test(contrasena)) return 'La contraseña debe contener al menos una letra mayúscula';
    if (!/[a-z]/.test(contrasena)) return 'La contraseña debe contener al menos una letra minúscula';
    if (!/[0-9]/.test(contrasena)) return 'La contraseña debe contener al menos un número';
    if (!/[^A-Za-z0-9]/.test(contrasena)) return 'La contraseña debe contener al menos un carácter especial';
    const normalizar = (v) => (v || '').trim().toLowerCase().replace(/\s+/g, '');
    if (normalizar(contrasena) === normalizar(usuario)) return 'La contraseña no puede ser igual al nombre de usuario';
    return null;
}

// Devuelve el mensaje de error de un campo del formulario de usuarios
function mensajeErrorUsuarioCampo(campo) {
    if (!campo) return null;
    const valor = campo.value;

    if (campo.id === 'usuario' && !valor.trim()) {
        return 'El nombre de usuario es obligatorio';
    }

    if (campo.id === 'contrasena') {
        if (!editingUsuario && !valor) return 'Debe ingresar una contraseña';
        if (valor) return validarContrasenaLocal(valor, document.getElementById('usuario').value);
    }

    if (campo.id === 'cargo-select') {
        const funcionario = obtenerFuncionarioSeleccionado();
        if (funcionario && cargosDeFuncionario(funcionario).length && !valor) {
            return 'Seleccione el cargo del funcionario';
        }
    }

    return null;
}

// Busca a qué campo corresponde un mensaje devuelto por el backend
function campoUsuarioPorMensaje(mensaje) {
    const texto = (mensaje || '').toLowerCase();
    if (texto.includes('nombre de usuario') || texto.includes('usuario')) return document.getElementById('usuario');
    if (texto.includes('contraseña') || texto.includes('contrasena')) return document.getElementById('contrasena');
    if (texto.includes('cargo')) return document.getElementById('cargo-select');
    if (texto.includes('funcionario')) return document.getElementById('funcionario-select');
    return null;
}

// Desactiva el usuario seleccionado
async function desactivarUsuario(id) {
    if (!confirm('¿Está seguro que desea desactivar este usuario?')) return;
    const res = await fetch('/api/usuarios/' + id, { method: 'DELETE' });
    if (res.ok) { showAlert('Usuario desactivado', 'success'); loadUsuarios(); }
    else showAlert('Error al desactivar el usuario', 'error');
}

// Reactiva un usuario desactivado
async function activarUsuario(id) {
    const res = await fetch('/api/usuarios/' + id + '/restore', { method: 'POST' });
    if (res.ok) { showAlert('Usuario activado', 'success'); loadUsuarios(); }
    else showAlert('Error al activar el usuario', 'error');
}

document.addEventListener('DOMContentLoaded', async function() {
    // Restaurar los datos del borrador (si se viene de crear un funcionario)
    restaurarDraftUsuario();

    // Recuperar el funcionario recién creado para auto-seleccionarlo al volver
    const nuevoRaw = localStorage.getItem('spc_nuevo_funcionario');
    if (nuevoRaw) {
        localStorage.removeItem('spc_nuevo_funcionario');
        try { nuevoFuncionarioPendiente = JSON.parse(nuevoRaw); } catch (e) { /* se ignora */ }
    }

    await cargarFuncionarios();
    await cargarCargos();

    await loadUsuarios();

    // Aplicar la auto-selección del nuevo funcionario, si corresponde
    if (nuevoFuncionarioPendiente) {
        document.getElementById('funcionario-select').value = nuevoFuncionarioPendiente.id;
        actualizarCargosFuncionario(null);
        showAlert('Funcionario creado y seleccionado', 'success');
        nuevoFuncionarioPendiente = null;
    }

    // Filtrar al escribir o cambiar seleccion
    document.getElementById('search-usuarios').addEventListener('input', renderUsuarios);
    document.getElementById('filtro-estado').addEventListener('change', renderUsuarios);

    // Al cambiar el funcionario, ajustar los cargos disponibles
    document.getElementById('funcionario-select').addEventListener('change', function() {
        actualizarCargosFuncionario(null);
        limpiarErrorCampo(document.getElementById('cargo-select'));
    });

    // Valida y envia los datos del usuario
    document.getElementById('usuario-form').addEventListener('submit', async function(e) {
        e.preventDefault();
        const form = this;
        limpiarErroresFormulario(form);

        // Se revisan las mismas reglas que ya tenía el formulario
        const campos = ['usuario', 'contrasena', 'cargo-select']
            .map(id => document.getElementById(id));
        const errores = erroresValidacionNativa(form);
        campos.forEach(campo => {
            const existente = errores.find(item => item.campo === campo);
            if (existente) {
                existente.validador = () => mensajeErrorUsuarioCampo(campo);
                return;
            }
            const mensaje = mensajeErrorUsuarioCampo(campo);
            if (mensaje) {
                errores.push({
                    campo,
                    mensaje,
                    validador: () => mensajeErrorUsuarioCampo(campo)
                });
            }
        });
        if (errores.length) {
            mostrarErroresFormulario(form, errores);
            return;
        }

        const contrasena = document.getElementById('contrasena').value;
        const nombreUsuario = document.getElementById('usuario').value;
        const funcSel = document.getElementById('funcionario-select');
        const cargoSel = document.getElementById('cargo-select');
        const obj = {
            usuario: nombreUsuario || null,
            contrasena: contrasena || null,
            funcionario: funcSel.value ? { ciPersona: Number(funcSel.value) } : null,
            cargo: cargoSel.value ? { codCargo: Number(cargoSel.value) } : null
        };

        const url = editingUsuario ? ('/api/usuarios/' + editingUsuario) : '/api/usuarios';
        const method = editingUsuario ? 'PUT' : 'POST';
        const res = await fetch(url, { method: method, headers:{'Content-Type':'application/json'}, body: JSON.stringify(obj) });
        if (res.ok) {
            showAlert(editingUsuario ? 'Usuario actualizado' : 'Usuario registrado', 'success');
            resetUsuarioForm();
            loadUsuarios();
        } else {
            const err = await res.json().catch(() => null);
            const mensajeServidor = err && err.message ? err.message : 'Error al guardar el usuario';
            const campo = campoUsuarioPorMensaje(mensajeServidor);
            if (campo) {
                mostrarErroresFormulario(form, [{
                    campo,
                    mensaje: mensajeServidor,
                    validador: () => mensajeErrorUsuarioCampo(campo)
                }]);
            } else {
                showAlert(mensajeServidor, 'error');
            }
        }
    });
});
