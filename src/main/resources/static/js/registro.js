// ====== Lógica de la pantalla de Registro ======

// Muestra u oculta la contraseña introducida
function togglePassword() {
    const input = document.getElementById('contrasena');
    const icon = document.getElementById('toggle-password-icon');
    if (input.type === 'password') {
        input.type = 'text';
        icon.className = 'fa-solid fa-eye-slash';
    } else {
        input.type = 'password';
        icon.className = 'fa-solid fa-eye';
    }
}

function mostrarMensaje(msg, tipo) {
    const box = document.getElementById('registro-message');
    if (!box) return;
    box.textContent = msg;
    box.className = tipo === 'success' ? 'alert-success' : 'alert-error';
}

// Carga los cargos y prepara el formulario de registro
document.addEventListener('DOMContentLoaded', async function() {
    // Cargar los cargos disponibles
    const sel = document.getElementById('cargo-select');
    sel.innerHTML = '<option value="">Seleccione un cargo...</option>';
    try {
        const cargos = await fetchAll('/api/cargos');
        cargos.forEach(c => {
            if (c.eliminado) return;
            const o = document.createElement('option');
            o.value = c.codCargo;
            o.text = c.descripcion;
            sel.appendChild(o);
        });
    } catch (e) { /* sin cargos */ }

    // Valida los datos antes de crear la cuenta
    document.getElementById('registro-form').addEventListener('submit', async function(e) {
        e.preventDefault();

        const ci = document.getElementById('ci-funcionario').value.trim();
        const codCargo = document.getElementById('cargo-select').value;
        const usuario = document.getElementById('usuario').value.trim();
        const contrasena = document.getElementById('contrasena').value;
        const confirmacion = document.getElementById('confirmacion').value;

        if (!/^[0-9]{6,}$/.test(ci)) { mostrarMensaje('Ingrese una cédula válida', 'error'); return; }
        if (!codCargo) { mostrarMensaje('Debe seleccionar un cargo', 'error'); return; }
        if (!usuario) { mostrarMensaje('Debe ingresar un nombre de usuario', 'error'); return; }

        const errorContrasena = validarContrasenaLocal(contrasena, usuario);
        if (errorContrasena) { mostrarMensaje(errorContrasena, 'error'); return; }
        if (contrasena !== confirmacion) { mostrarMensaje('Las contraseñas no coinciden', 'error'); return; }

        const obj = { ciFuncionario: Number(ci), codCargo: Number(codCargo), usuario: usuario, contrasena: contrasena };
        const res = await fetch('/api/registro', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(obj) });
        const data = await res.json().catch(() => null);
        if (res.ok) {
            mostrarMensaje('Cuenta creada correctamente. Ya puede iniciar sesión.', 'success');
            this.reset();
            setTimeout(() => { window.location.href = 'login.html'; }, 2000);
        } else {
            mostrarMensaje(data && data.mensaje ? data.mensaje : 'Error al crear la cuenta', 'error');
        }
    });
});

function fetchAll(url) {
    return fetch(url).then(r => r.json()).catch(() => []);
}

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
