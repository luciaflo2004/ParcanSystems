// ====== Funciones compartidas de la aplicacion ======

// URL base de la API (vacia porque es relativa)
const API = '';

// HTML del menu lateral que se inyecta en todas las paginas
const sidebarHTML = `<nav class="sidebar-nav">
<div class="sidebar-brand"><i class="fa-solid fa-place-of-worship"></i><div><h2>SystemParCan</h2><p>Parroquia de Katuete</p></div></div>
<ul class="nav-list">
<li><a href="index.html"><i class="fa-solid fa-house"></i> Inicio</a></li>
<li><a href="instituciones.html"><i class="fa-solid fa-church"></i> Instituciones Eclesiasticas</a></li>
<li><a href="personas.html"><i class="fa-solid fa-users"></i> Personas</a></li>
<li><a href="funcionarios.html"><i class="fa-solid fa-user-tie"></i> Funcionarios</a></li>
<li><a href="cargos.html"><i class="fa-solid fa-briefcase"></i> Cargos</a></li>
<li><a href="usuarios.html"><i class="fa-solid fa-user-shield"></i> Usuarios</a></li>
<li><a href="roles.html"><i class="fa-solid fa-key"></i> Roles</a></li>
<li class="nav-section">Solicitudes y Servicios</li>
<li><a href="solicitudes.html"><i class="fa-solid fa-file-pen"></i> Solicitudes</a></li>
<li><a href="servicios.html"><i class="fa-solid fa-bell-concierge"></i> Servicios</a></li>
</ul>
<div class="sidebar-logout"><a href="#" onclick="confirmarCerrarSesion()"><i class="fa-solid fa-right-from-bracket"></i> Cerrar sesión</a></div>
</nav>`;

// HTML del modal de confirmación de cierre de sesión
const logoutModalHTML = `<div id="logout-modal" class="modal-overlay hidden">
<div class="modal-card">
<h3>Cerrar sesión</h3>
<p>¿Qué desea hacer?</p>
<div class="modal-actions">
<button class="btn btn-outline" onclick="cerrarSesionSalir()"><i class="fa-solid fa-right-from-bracket"></i> Salir del sistema</button>
<button class="btn btn-primary" onclick="cerrarSesionCambiarCuenta()"><i class="fa-solid fa-user-plus"></i> Iniciar sesión con otra cuenta</button>
<button class="btn btn-outline" onclick="cerrarSesionCancelar()"><i class="fa-solid fa-xmark"></i> Cancelar</button>
</div>
</div>
</div>`;

document.addEventListener('DOMContentLoaded', function() {
    // Si no existe el menu lateral, se inyecta al inicio del body
    if (!document.querySelector('.sidebar-nav')) {
        document.body.insertAdjacentHTML('afterbegin', sidebarHTML);
    }
    // Se inyecta el modal de cierre de sesión si no existe
    if (!document.getElementById('logout-modal')) {
        document.body.insertAdjacentHTML('beforeend', logoutModalHTML);
    }
    // Prepara los formularios para mostrar errores junto a cada campo
    configurarErroresFormulario();
    highlightActive();
    // Muestra el saludo del funcionario que inicio sesion
    cargarSaludo();
});

// Muestra el modal de confirmación de cierre de sesión
function confirmarCerrarSesion() {
    const modal = document.getElementById('logout-modal');
    if (modal) modal.classList.remove('hidden');
}

// Cierra la sesión y redirige al Login (salir del sistema)
function cerrarSesionSalir() {
    fetch('/logout', { method: 'POST' }).then(() => { window.location.href = '/login.html'; });
}

// Cierra la sesión y redirige al Login (cambiar de cuenta)
function cerrarSesionCambiarCuenta() {
    fetch('/logout', { method: 'POST' }).then(() => { window.location.href = '/login.html'; });
}

// Cancela el cierre de sesión
function cerrarSesionCancelar() {
    const modal = document.getElementById('logout-modal');
    if (modal) modal.classList.add('hidden');
}

// Resalta en el menu el enlace de la pagina actual
function highlightActive() {
    const path = window.location.pathname.split('/').pop();
    document.querySelectorAll('.nav-list a').forEach(a => {
        a.classList.remove('active');
        if (a.getAttribute('href') === path) a.classList.add('active');
    });
}

// Obtiene datos de la API en formato JSON; si falla devuelve un arreglo vacio
async function fetchAll(url) {
    try {
        const res = await fetch(API + url);
        return await res.json();
    } catch(e) {
        console.error('Error fetching:', e);
        return [];
    }
}

// Ordena una lista de registros por su identificador ascendente
function ordenarPorId(lista, campoId) {
    return [...lista].sort((a, b) => Number(a[campoId]) - Number(b[campoId]));
}

// Muestra el saludo del funcionario autenticado en el encabezado
async function cargarSaludo() {
    const pageHead = document.querySelector('.page-head');
    if (!pageHead || document.getElementById('user-greeting')) return;

    // Contenedor con el texto del saludo y el circulo de iniciales
    const saludo = document.createElement('div');
    saludo.id = 'user-greeting';
    saludo.className = 'user-greeting';

    const texto = document.createElement('span');
    texto.className = 'greeting-text';
    texto.textContent = 'Hola, bienvenido';

    const iniciales = document.createElement('span');
    iniciales.className = 'user-avatar hidden';
    iniciales.setAttribute('aria-hidden', 'true');

    saludo.appendChild(texto);
    saludo.appendChild(iniciales);

    // Se ubica junto a las acciones del encabezado para no cambiar el diseno
    const acciones = pageHead.querySelector('.head-actions');
    if (acciones) {
        acciones.insertBefore(saludo, acciones.firstChild);
    } else {
        pageHead.appendChild(saludo);
    }

    const datos = await fetchAll('/api/sesion/saludo');
    if (datos && datos.nombre) {
        texto.textContent = 'Hola, ' + datos.nombre;
    }
    // Las iniciales solo se muestran cuando el backend las devuelve
    if (datos && datos.iniciales) {
        iniciales.textContent = datos.iniciales;
        iniciales.classList.remove('hidden');
    }
}

// Indica si un registro no fue desactivado
function estaActivo(registro) {
    return !!registro && !registro.eliminado;
}

// Un funcionario esta activo solo si el funcionario y su persona siguen activos
function funcionarioActivo(funcionario) {
    return estaActivo(funcionario) && (!funcionario.persona || estaActivo(funcionario.persona));
}

// Carga una lista desde la API y deja solamente los registros activos
async function fetchActivos(url) {
    const datos = await fetchAll(url);
    return Array.isArray(datos) ? datos.filter(estaActivo) : [];
}

// Agrega una opcion al desplegable si todavia no existe.
// Si es una opcion historica, se marca para poder quitarla despues.
function agregarOpcionSiFalta(select, value, text, historica) {
    if (!select || value == null) return;
    if (Array.from(select.options).some(o => o.value === String(value))) return;
    const o = document.createElement('option');
    o.value = value;
    o.text = text;
    if (historica) o.dataset.historico = 'true';
    select.appendChild(o);
}

// Quita las opciones historicas que solo se muestran al editar un registro
function limpiarOpcionesHistoricas(select) {
    if (!select) return;
    Array.from(select.querySelectorAll('option[data-historico="true"]'))
        .forEach(o => o.remove());
}

// Muestra un mensaje temporal (exito o error) en la parte superior del contenido
function showAlert(msg, type) {
    const div = document.createElement('div');
    div.className = type === 'success' ? 'alert-success' : 'alert-error';
    div.textContent = msg;
    const content = document.querySelector('.content');
    content.insertBefore(div, content.firstChild);
    setTimeout(() => div.remove(), 3000);
}

// ====== Errores de formulario por campo ======

// Obtiene el texto visible de la etiqueta de un campo
function obtenerEtiquetaCampo(campo) {
    const grupo = campo.closest('.form-group');
    const label = grupo ? grupo.querySelector('label') : null;
    return label ? label.textContent.replace(':', '').trim() : 'campo';
}

// Muestra el error encima del campo y lo marca en rojo
function mostrarErrorCampo(campo, mensaje, validador) {
    if (!campo || !mensaje) return;
    const grupo = campo.closest('.form-group') || campo.parentElement;
    let error = grupo.querySelector('.field-error[data-campo="' + campo.id + '"]');

    if (!error) {
        error = document.createElement('div');
        error.className = 'field-error';
        error.id = 'error-' + campo.id;
        error.dataset.campo = campo.id;
        error.setAttribute('role', 'alert');

        // Si el campo esta dentro de una fila con botones, el mensaje va antes de la fila
        const contenedor = campo.closest('.solicitante-row') || campo;
        contenedor.parentElement.insertBefore(error, contenedor);
    }

    // Evita duplicar la palabra Error si el mensaje ya la trae
    error.textContent = mensaje.startsWith('Error:') ? mensaje : 'Error: ' + mensaje;
    error.hidden = false;
    campo.classList.add('campo-invalido');
    campo.setAttribute('aria-invalid', 'true');
    campo.setAttribute('aria-describedby', error.id);

    // Guarda la validacion para poder limpiar el error mientras se corrige
    if (validador) campo._validadorError = validador;
}

// Retira el mensaje y el borde rojo de un campo
function limpiarErrorCampo(campo) {
    if (!campo) return;
    const grupo = campo.closest('.form-group') || campo.parentElement;
    const error = grupo.querySelector('.field-error[data-campo="' + campo.id + '"]');
    if (error) {
        error.textContent = '';
        error.hidden = true;
    }
    campo.classList.remove('campo-invalido');
    campo.removeAttribute('aria-invalid');
    campo.removeAttribute('aria-describedby');
    delete campo._validadorError;
}

// Limpia todos los errores de un formulario
function limpiarErroresFormulario(form) {
    if (!form) return;
    form.querySelectorAll('.campo-invalido').forEach(limpiarErrorCampo);
    form.querySelectorAll('.field-error').forEach(error => {
        error.textContent = '';
        error.hidden = true;
    });
}

// Muestra una lista de errores y lleva la vista al primero
function mostrarErroresFormulario(form, errores) {
    limpiarErroresFormulario(form);
    (errores || []).forEach(item => {
        if (item && item.campo && item.mensaje) {
            mostrarErrorCampo(item.campo, item.mensaje, item.validador);
        }
    });

    const primero = form.querySelector('.campo-invalido');
    if (primero) {
        primero.scrollIntoView({ behavior: 'smooth', block: 'center' });
        try {
            primero.focus({ preventScroll: true });
        } catch (e) {
            primero.focus();
        }
    }
}

// Traduce las restricciones nativas del navegador a un mensaje entendible
function mensajeValidacionNativa(campo) {
    if (!campo.validity || campo.validity.valid) return null;

    const etiqueta = obtenerEtiquetaCampo(campo);
    if (campo.validity.valueMissing) {
        return 'El campo ' + etiqueta + ' es obligatorio.';
    }
    if (campo.validity.patternMismatch) {
        return campo.title || 'El formato del campo ' + etiqueta + ' no es válido.';
    }
    if (campo.validity.tooShort) {
        return 'El campo ' + etiqueta + ' debe tener al menos ' + campo.minLength + ' caracteres.';
    }
    if (campo.validity.tooLong) {
        return 'El campo ' + etiqueta + ' no puede superar los ' + campo.maxLength + ' caracteres.';
    }
    if (campo.validity.rangeUnderflow) {
        return 'El campo ' + etiqueta + ' debe ser mayor o igual a ' + campo.min + '.';
    }
    if (campo.validity.rangeOverflow) {
        return 'El campo ' + etiqueta + ' debe ser menor o igual a ' + campo.max + '.';
    }
    if (campo.validity.typeMismatch || campo.validity.badInput) {
        return 'Ingrese un valor válido en el campo ' + etiqueta + '.';
    }
    return campo.validationMessage || 'El valor ingresado no es válido.';
}

// Revalida un campo cuando el usuario lo corrige
function revalidarCampo(campo) {
    if (!campo || !campo.classList.contains('campo-invalido')) return;

    const validador = campo._validadorError;
    const mensaje = validador ? validador() : mensajeValidacionNativa(campo);
    if (mensaje) {
        mostrarErrorCampo(campo, mensaje, validador);
    } else {
        limpiarErrorCampo(campo);
    }
}

// Recoge los errores de las restricciones nativas del formulario
function erroresValidacionNativa(form) {
    const errores = [];
    form.querySelectorAll('input, select, textarea').forEach(campo => {
        if (campo.disabled || campo.type === 'hidden' || campo.offsetParent === null) return;
        const mensaje = mensajeValidacionNativa(campo);
        if (mensaje) {
            errores.push({
                campo,
                mensaje,
                validador: () => mensajeValidacionNativa(campo)
            });
        }
    });
    return errores;
}

// Activa la validacion nativa y la limpieza automatica en todos los formularios
function configurarErroresFormulario() {
    document.querySelectorAll('form').forEach(form => {
        // Se usa nuestra presentacion de errores, pero se siguen revisando las mismas reglas
        form.noValidate = true;
        form.addEventListener('input', e => revalidarCampo(e.target));
        form.addEventListener('change', e => revalidarCampo(e.target));
    });
}

// (Legado) Filtra las filas de una tabla por texto en el DOM
function bindSearch(inputId, containerId) {
    const input = document.getElementById(inputId);
    if (!input) return;
    input.addEventListener('input', function () {
        const q = this.value.trim().toLowerCase();
        const rows = document.querySelectorAll('#' + containerId + ' tbody tr');
        rows.forEach(r => {
            r.style.display = (!q || r.textContent.toLowerCase().includes(q)) ? '' : 'none';
        });
    });
}

// (Legado) Limpia el filtro de texto de una tabla
function clearSearch(inputId, containerId) {
    const input = document.getElementById(inputId);
    if (input) input.value = '';
    document.querySelectorAll('#' + containerId + ' tbody tr').forEach(r => r.style.display = '');
}

// Convierte un texto a una forma comparable, sin distinguir mayusculas ni tildes
function compararTexto(a, b) {
    return String(a || '').trim().localeCompare(String(b || '').trim(), 'es', { sensitivity: 'base' });
}

// Ordena por nombre; usa el apellido y luego el ID como desempate
function ordenarAlfabeticamente(lista, obtenerNombre, obtenerApellido, obtenerId, direccion) {
    const factor = direccion === 'desc' ? -1 : 1;
    return [...lista].sort((a, b) => {
        const cmpNombre = compararTexto(obtenerNombre(a), obtenerNombre(b));
        if (cmpNombre !== 0) return cmpNombre * factor;

        const cmpApellido = compararTexto(obtenerApellido(a), obtenerApellido(b));
        if (cmpApellido !== 0) return cmpApellido * factor;

        // El ID mantiene un orden estable cuando nombre y apellido coinciden
        return Number(obtenerId(a)) - Number(obtenerId(b));
    });
}

// Devuelve la letra inicial del nombre para el indice alfabetico
function letraInicial(nombre) {
    const texto = String(nombre || '').trim();
    if (!texto) return '';
    const primera = texto.charAt(0).toUpperCase();
    // La Ñ se conserva como letra independiente
    if (primera === 'Ñ') return 'Ñ';
    // Las vocales acentuadas se agrupan con su letra base
    return primera.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toUpperCase();
}

// Arma el indice vertical con las letras que tienen registros disponibles
function crearIndiceAlfabetico(disponibles) {
    const letras = 'ABCDEFGHIJKLMNÑOPQRSTUVWXYZ'.split('');
    return '<div class="alpha-index">' + letras.map(letra =>
        '<button type="button" class="alpha-letter" data-letra="' + letra + '"' +
        (disponibles.has(letra) ? '' : ' disabled') + '>' + letra + '</button>'
    ).join('') + '</div>';
}

// Hace que cada letra lleve al primer registro visible que empieza con ella
function activarIndiceAlfabetico(container) {
    container.querySelectorAll('.alpha-letter:not(:disabled)').forEach(boton => {
        boton.addEventListener('click', function() {
            const fila = container.querySelector('tr[data-letra="' + this.dataset.letra + '"]');
            if (fila) fila.scrollIntoView({ behavior: 'smooth', block: 'center' });
        });
    });
}

// Construye una tabla HTML dentro de un contenedor y la muestra.
// Si recibe la opcion indiceAlfabetico, agrega el indice lateral para navegar.
function renderTable(containerId, headers, rows, actions, opciones) {
    const container = document.getElementById(containerId);
    const lista = rows || [];
    const conIndice = !!(opciones && opciones.indiceAlfabetico);
    const letrasDisponibles = new Set(lista.map(r => r._letra).filter(Boolean));
    const indice = conIndice ? crearIndiceAlfabetico(letrasDisponibles) : '';
    const vacio = '<div class="no-data"><span>&#128196;</span><br>No hay datos registrados</div>';

    if (lista.length === 0) {
        container.innerHTML = conIndice
            ? '<div class="alpha-layout">' + indice + '<div class="alpha-table">' + vacio + '</div></div>'
            : vacio;
        if (conIndice) activarIndiceAlfabetico(container);
        return;
    }

    let html = conIndice
        ? '<div class="alpha-layout">' + indice + '<div class="alpha-table">'
        : '';
    html += '<table><thead><tr>';
    headers.forEach(h => html += '<th>' + h + '</th>');
    if (actions) html += '<th class="actions-col">Acciones</th>';
    html += '</tr></thead><tbody>';
    lista.forEach(row => {
        html += '<tr' + (conIndice ? ' data-letra="' + (row._letra || '') + '"' : '') + '>';
        headers.forEach(h => {
            const val = row[h];
            html += '<td>' + (val != null ? val : '-') + '</td>';
        });
        if (actions) html += '<td class="actions-col">' + actions(row) + '</td>';
        html += '</tr>';
    });
    html += '</tbody></table>';
    if (conIndice) html += '</div></div>';
    container.innerHTML = html;
    if (conIndice) activarIndiceAlfabetico(container);
}

// ====== Validaciones reutilizables ======

// Fecha actual en formato YYYY-MM-DD (local)
function hoyLocal() {
    return fechaISO(new Date());
}

// Convierte un objeto Date local a texto YYYY-MM-DD
function fechaISO(fecha) {
    return fecha.getFullYear() + '-' +
        String(fecha.getMonth() + 1).padStart(2, '0') + '-' +
        String(fecha.getDate()).padStart(2, '0');
}

// Resta meses calendario conservando el día cuando sea posible
function fechaHaceMeses(meses) {
    const hoy = new Date();
    const objetivo = new Date(hoy.getFullYear(), hoy.getMonth() - meses, 1);
    const ultimoDia = new Date(objetivo.getFullYear(), objetivo.getMonth() + 1, 0).getDate();
    objetivo.setDate(Math.min(hoy.getDate(), ultimoDia));
    return fechaISO(objetivo);
}

// Comprueba que el texto sea una fecha real en formato YYYY-MM-DD
function esFechaISOValida(valor) {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(valor || '')) return false;
    const partes = valor.split('-').map(Number);
    const fecha = new Date(partes[0], partes[1] - 1, partes[2]);
    return fecha.getFullYear() === partes[0]
        && fecha.getMonth() === partes[1] - 1
        && fecha.getDate() === partes[2];
}

// Fecha máxima para un funcionario (hoy menos 18 años) en YYYY-MM-DD
function fechaMaximaFuncionario() {
    const d = new Date();
    d.setFullYear(d.getFullYear() - 18);
    return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
}

// Devuelve true si hay más de dos espacios consecutivos
function tieneEspaciosConsecutivos(valor) {
    return (valor || '').includes('   ');
}

// Devuelve true si hay más de "maximo" caracteres idénticos consecutivos (ignora mayúsculas)
function tieneCaracteresConsecutivos(valor, maximo) {
    const s = (valor || '').toLowerCase();
    let count = 1;
    for (let i = 1; i < s.length; i++) {
        if (s[i] === s[i - 1]) {
            count++;
            if (count > maximo) return true;
        } else {
            count = 1;
        }
    }
    return false;
}

// Devuelve true si el valor contiene solo letras (con tildes y ñ) y espacios
function esSoloLetras(valor) {
    return /^[A-Za-zÁÉÍÓÚáéíóúÑñÜü ]*$/.test(valor || '');
}

// Devuelve true si el valor contiene solo números
function esSoloNumeros(valor) {
    return /^[0-9]*$/.test(valor || '');
}

// Formatea un importe numérico como moneda paraguaya: 1000 -> "1.000 Gs."
// Se arma manualmente para no depender de la configuración regional del navegador.
function formatoMoneda(valor) {
    if (valor == null || valor === '') return '-';
    const n = Number(valor);
    if (isNaN(n)) return '-';
    const entero = Math.round(Math.abs(n)).toString();
    const conMiles = entero.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
    return (n < 0 ? '-' : '') + conMiles + ' Gs.';
}

// Convierte un importe escrito con separadores y "Gs." a número. Devuelve NaN si no es válido.
function parsearMoneda(texto) {
    if (texto == null) return NaN;
    const limpio = String(texto)
        .replace(/Gs\.?/gi, '')
        .replace(/[.\s]/g, '')
        .trim();
    if (limpio === '' || !/^\d+$/.test(limpio)) return NaN;
    return Number(limpio);
}

// Buscador reutilizable para desplegables.
// Filtra las opciones del select por texto (sin distinguir mayúsculas), muestra como
// máximo 6 registros visibles con scroll y conserva el valor seleccionado.
function crearBuscadorSelect(selectId, searchId) {
    const select = document.getElementById(selectId);
    const search = document.getElementById(searchId);
    if (!select || !search) return null;

    // El panel se posiciona respecto del contenedor del campo de búsqueda
    const contenedor = search.parentElement;
    if (window.getComputedStyle(contenedor).position === 'static') {
        contenedor.style.position = 'relative';
    }

    const panel = document.createElement('div');
    panel.className = 'dropdown-panel';
    panel.style.display = 'none';
    contenedor.appendChild(panel);

    function opciones() {
        return Array.from(select.options)
            .filter(o => o.value !== '')
            .map(o => ({ value: o.value, text: o.text }));
    }

    function ocultar() {
        panel.style.display = 'none';
    }

    function seleccionar(opt) {
        select.value = opt.value;
        select.dispatchEvent(new Event('change'));
        search.value = '';
        ocultar();
    }

    function render() {
        const q = (search.value || '').toLowerCase().trim();
        const lista = opciones().filter(o => o.text.toLowerCase().includes(q));
        panel.innerHTML = '';

        if (lista.length === 0) {
            const vacio = document.createElement('div');
            vacio.className = 'dropdown-empty';
            vacio.textContent = 'No se encontraron registros';
            panel.appendChild(vacio);
        } else {
            lista.forEach(o => {
                const item = document.createElement('div');
                item.className = 'dropdown-item' + (o.value === select.value ? ' selected' : '');
                item.textContent = o.text;
                item.addEventListener('mousedown', function(ev) {
                    // mousedown evita que el input pierda el foco antes de registrar el click
                    ev.preventDefault();
                    seleccionar(o);
                });
                panel.appendChild(item);
            });
        }

        panel.style.top = (search.offsetTop + search.offsetHeight + 2) + 'px';
        panel.style.left = search.offsetLeft + 'px';
        panel.style.width = search.offsetWidth + 'px';
        panel.style.display = 'block';
    }

    search.addEventListener('input', render);
    search.addEventListener('focus', render);
    search.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            ocultar();
        }
        // Evita que Enter envie el formulario mientras se busca
        if (e.key === 'Enter') {
            e.preventDefault();
        }
    });
    select.addEventListener('change', ocultar);

    document.addEventListener('click', function(e) {
        if (!contenedor.contains(e.target)) ocultar();
    });

    return { render: render, ocultar: ocultar };
}
