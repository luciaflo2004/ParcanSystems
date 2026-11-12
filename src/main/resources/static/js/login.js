// ====== Lógica de la pantalla de Login ======

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

// Muestra el mensaje correspondiente según el resultado del login
document.addEventListener('DOMContentLoaded', function() {
    const params = new URLSearchParams(window.location.search);
    const box = document.getElementById('login-message');
    if (!box) return;

    if (params.get('error')) {
        box.textContent = 'Usuario o contraseña incorrectos.';
        box.className = 'alert-error';
    } else if (params.get('disabled')) {
        box.textContent = 'La cuenta está desactivada. Contacte al administrador.';
        box.className = 'alert-error';
    } else if (params.get('logout')) {
        box.textContent = 'Sesión cerrada correctamente.';
        box.className = 'alert-success';
    }
});
