package com.sistemparcan.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.sistemparcan.entity.Cargo;
import com.sistemparcan.entity.Funcionario;
import com.sistemparcan.entity.Usuario;
import com.sistemparcan.repository.CargoRepository;
import com.sistemparcan.repository.FuncionarioRepository;
import com.sistemparcan.repository.UsuarioRepository;

// Reglas de negocio para los usuarios y su seguridad
@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final FuncionarioRepository funcionarioRepository;
    private final CargoRepository cargoRepository;
    private final PasswordService passwordService;

    public UsuarioService(UsuarioRepository repository, FuncionarioRepository funcionarioRepository,
            CargoRepository cargoRepository, PasswordService passwordService) {
        this.repository = repository;
        this.funcionarioRepository = funcionarioRepository;
        this.cargoRepository = cargoRepository;
        this.passwordService = passwordService;
    }

    // Devuelve todos los usuarios (activos e inactivos)
    public List<Usuario> findAll() {
        return repository.findAll();
    }

    // Busca un usuario por su identificador
    public Optional<Usuario> findById(Integer id) {
        return repository.findById(id);
    }

    /**
     * Obtiene el primer nombre y las iniciales del funcionario asociado a la cuenta.
     * Devuelve valores vacios si el usuario no tiene funcionario o persona relacionada.
     *
     * @param nombreUsuario nombre con el que se autentico el usuario
     * @return mapa con las claves "nombre" e "iniciales"
     */
    @Transactional(readOnly = true)
    public Map<String, String> obtenerSaludoFuncionario(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            return Map.of("nombre", "", "iniciales", "");
        }
        Usuario usuario = repository.findByUsuarioNormalizado(normalizarUsuario(nombreUsuario)).orElse(null);
        if (usuario == null || usuario.getFuncionario() == null
                || usuario.getFuncionario().getPersona() == null) {
            return Map.of("nombre", "", "iniciales", "");
        }

        String nombreCompleto = usuario.getFuncionario().getPersona().getNombrePersona();
        String apellidoCompleto = usuario.getFuncionario().getPersona().getApellidoPersona();
        String primerNombre = primeraPalabra(nombreCompleto);
        String primerApellido = primeraPalabra(apellidoCompleto);

        // Las iniciales solo se muestran si existen nombre y apellido
        String iniciales = "";
        if (!primerNombre.isEmpty() && !primerApellido.isEmpty()) {
            iniciales = (primerNombre.substring(0, 1) + primerApellido.substring(0, 1)).toUpperCase();
        }

        return Map.of("nombre", primerNombre, "iniciales", iniciales);
    }

    // Devuelve la primera palabra de un texto, sin espacios sobrantes
    private String primeraPalabra(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return "";
        }
        return valor.trim().split("\\s+")[0];
    }

    /**
     * Guarda un usuario validando la unicidad del nombre (sin distinguir
     * mayúsculas/minúsculas ni espacios), preparando la contraseña (hash y huella)
     * y resolviendo el funcionario asociado.
     */
    @Transactional
    public Usuario save(Usuario usuario) {
        normalizarNombre(usuario);
        validarUnicidadUsuario(usuario);
        prepararContrasena(usuario);
        // Al editar se busca el usuario actual para conservar sus relaciones historicas
        Usuario actual = usuario.getIdContraseña() != null
                ? repository.findById(usuario.getIdContraseña()).orElse(null)
                : null;
        validarYResolverFuncionario(usuario, actual);
        resolverCargo(usuario, actual);
        validarUnicidadFuncionarioCargo(usuario);
        return repository.save(usuario);
    }

    /**
     * Normaliza el nombre de usuario: recorta espacios y almacena la versión
     * normalizada (minúsculas y sin espacios) para las comprobaciones de unicidad.
     */
    private void normalizarNombre(Usuario usuario) {
        if (usuario.getUsuario() != null) {
            usuario.setUsuario(usuario.getUsuario().trim());
        }
        usuario.setUsuarioNormalizado(normalizarUsuario(usuario.getUsuario()));
    }

    /**
     * Devuelve la versión normalizada de un texto: minúsculas y sin espacios.
     */
    public String normalizarUsuario(String valor) {
        if (valor == null) return "";
        return valor.trim().toLowerCase().replaceAll("\\s+", "");
    }

    /**
     * Impide registrar dos nombres de usuario equivalentes. En edición se excluye el propio registro.
     */
    private void validarUnicidadUsuario(Usuario usuario) {
        String normalizado = usuario.getUsuarioNormalizado();
        if (normalizado == null || normalizado.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre de usuario es obligatorio");
        }
        Optional<Usuario> existente = repository.findByUsuarioNormalizado(normalizado);
        if (existente.isPresent()
                && !existente.get().getIdContraseña().equals(usuario.getIdContraseña())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este nombre de usuario ya está registrado. Introduzca uno diferente.");
        }
    }

    /**
     * Si la contraseña viene en texto plano (nueva o cambiada), calcula su huella HMAC
     * y la almacena como hash BCrypt. Si ya es un hash (contraseña conservada), no la toca.
     */
    private void prepararContrasena(Usuario usuario) {
        String contrasena = usuario.getContrasena();
        if (contrasena == null || contrasena.isEmpty()) return;
        if (passwordService.esHash(contrasena)) return;

        String huella = passwordService.huella(contrasena);
        usuario.setHuellaContrasena(huella);
        usuario.setContrasena(passwordService.hash(contrasena));
        validarReutilizacion(usuario);
    }

    /**
     * Evita que dos usuarios usen la misma contraseña, comparando su huella HMAC
     * (sin exponer ni comparar las contraseñas en claro).
     */
    private void validarReutilizacion(Usuario usuario) {
        String huella = usuario.getHuellaContrasena();
        if (huella == null) return;
        for (Usuario existente : repository.findByHuellaContrasena(huella)) {
            if (existente.getIdContraseña() != null
                    && existente.getIdContraseña().equals(usuario.getIdContraseña())) {
                continue;
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La contraseña no está disponible. Introduzca una diferente.");
        }
    }

    /**
     * Valida que una contraseña nueva cumpla los requisitos de seguridad:
     * al menos 8 caracteres, una mayúscula, una minúscula, un número,
     * un carácter especial y que no sea igual al nombre de usuario.
     */
    public void validarContrasena(String contrasena, String usuario) {
        if (contrasena == null || contrasena.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es obligatoria");
        }
        if (contrasena.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener al menos 8 caracteres");
        }
        if (!contrasena.matches(".*[A-Z].*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña debe contener al menos una letra mayúscula");
        }
        if (!contrasena.matches(".*[a-z].*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña debe contener al menos una letra minúscula");
        }
        if (!contrasena.matches(".*[0-9].*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña debe contener al menos un número");
        }
        if (!contrasena.matches(".*[^A-Za-z0-9].*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña debe contener al menos un carácter especial");
        }
        if (normalizarUsuario(contrasena).equals(normalizarUsuario(usuario))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña no puede ser igual al nombre de usuario");
        }
    }

    /**
     * Si el usuario viene con un funcionario seleccionado, valida que exista y no esté
     * eliminado lógicamente, y lo reemplaza por el registro real. Si no trae funcionario,
     * se deja sin relacionar (null).
     */
    private void validarYResolverFuncionario(Usuario usuario, Usuario actual) {
        Funcionario funcionario = usuario.getFuncionario();
        if (funcionario == null || funcionario.getCiPersona() == null) {
            usuario.setFuncionario(null);
            return;
        }
        Funcionario existente = funcionarioRepository.findById(funcionario.getCiPersona()).orElse(null);
        if (existente == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El funcionario seleccionado no existe");
        }
        // Se controla el estado del funcionario y tambien el de su persona
        boolean personaInactiva = existente.getPersona() != null
                && Boolean.TRUE.equals(existente.getPersona().getEliminado());
        if (Boolean.TRUE.equals(existente.getEliminado()) || personaInactiva) {
            // Se permite conservar el funcionario inactivo solo si ya estaba asociado
            boolean mismoFuncionario = actual != null && actual.getFuncionario() != null
                    && existente.getCiPersona().equals(actual.getFuncionario().getCiPersona());
            if (!mismoFuncionario) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El funcionario seleccionado está inactivo");
            }
        }
        usuario.setFuncionario(existente);
    }

    /**
     * Resuelve el cargo asociado al usuario cargando el registro real.
     * Si no se indicó un cargo y el funcionario tiene uno activo, se usa ese,
     * de modo que la relación Usuario–Funcionario–Cargo se conserve.
     */
    private void resolverCargo(Usuario usuario, Usuario actual) {
        Cargo cargoSeleccionado = null;
        if (usuario.getCargo() != null && usuario.getCargo().getCodCargo() != null) {
            cargoSeleccionado = cargoRepository.findById(usuario.getCargo().getCodCargo())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "El cargo seleccionado no existe"));
        } else if (usuario.getFuncionario() != null) {
            // Solo se toma automaticamente el cargo del funcionario si esta activo
            Cargo cargoFuncionario = usuario.getFuncionario().getCargo();
            if (cargoFuncionario != null && !Boolean.TRUE.equals(cargoFuncionario.getEliminado())) {
                cargoSeleccionado = cargoFuncionario;
            }
        }

        if (cargoSeleccionado == null) {
            usuario.setCargo(null);
            return;
        }

        boolean mismoCargo = actual != null && actual.getCargo() != null
                && cargoSeleccionado.getCodCargo().equals(actual.getCargo().getCodCargo());
        // No se permite una nueva relacion con un cargo inactivo, pero si conservar el historico
        if (Boolean.TRUE.equals(cargoSeleccionado.getEliminado()) && !mismoCargo) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El cargo seleccionado está inactivo");
        }
        usuario.setCargo(cargoSeleccionado);
    }

    /**
     * Impide dos cuentas para la misma combinación funcionario + cargo.
     * Solo se valida cuando ambos están presentes; se excluye el propio registro al editar.
     */
    private void validarUnicidadFuncionarioCargo(Usuario usuario) {
        if (usuario.getFuncionario() == null || usuario.getCargo() == null) return;
        Integer ci = usuario.getFuncionario().getCiPersona();
        Integer codCargo = usuario.getCargo().getCodCargo();
        for (Usuario existente : repository.findByFuncionarioCiPersonaAndCargoCodCargo(ci, codCargo)) {
            if (existente.getIdContraseña() != null
                    && existente.getIdContraseña().equals(usuario.getIdContraseña())) {
                continue;
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este funcionario ya tiene una cuenta para ese cargo.");
        }
    }

    // Desactiva el usuario sin borrarlo de la base de datos
    public void deleteById(Integer id) {
        repository.findById(id).ifPresent(u -> {
            u.setEliminado(true);
            repository.save(u);
        });
    }

    // Reactiva un usuario desactivado
    public void restoreById(Integer id) {
        repository.findById(id).ifPresent(u -> {
            u.setEliminado(false);
            repository.save(u);
        });
    }
}
