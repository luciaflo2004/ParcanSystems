package com.sistemparcan.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.sistemparcan.entity.Cargo;
import com.sistemparcan.repository.CargoRepository;
import com.sistemparcan.util.ValidacionUtil;

// Reglas de negocio para los cargos
@Service
public class CargoService {

    private final CargoRepository repository;

    public CargoService(CargoRepository repository) {
        this.repository = repository;
    }

    // Devuelve todos los cargos (activos e inactivos)
    public List<Cargo> findAll() {
        return repository.findAll();
    }

    // Devuelve solamente los cargos activos para los desplegables
    public List<Cargo> findActivos() {
        return repository.findByEliminadoFalse();
    }

    // Busca un cargo por su codigo
    public Optional<Cargo> findById(Integer id) {
        return repository.findById(id);
    }

    // Guarda un cargo despues de validar su descripcion
    public Cargo save(Cargo cargo) {
        validarDescripcion(cargo);
        return repository.save(cargo);
    }

    /**
     * Valida la descripción del cargo: solo letras y espacios, máximo 100 caracteres,
     * sin más de dos espacios ni dos letras iguales consecutivas.
     */
    private void validarDescripcion(Cargo cargo) {
        String descripcion = cargo.getDescripcion();
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La descripción del cargo es obligatoria");
        }
        ValidacionUtil.validarSoloLetras(descripcion,
                "La descripción del cargo solo permite letras y espacios");
        ValidacionUtil.validarLongitud(descripcion, 100,
                "La descripción no puede superar los 100 caracteres");
        ValidacionUtil.validarEspaciosConsecutivos(descripcion);
        ValidacionUtil.validarCaracteresConsecutivos(descripcion, 2,
                "No se permiten más de dos letras iguales consecutivas");
    }

    // Desactiva el cargo sin borrarlo de la base de datos
    public void deleteById(Integer id) {
        repository.findById(id).ifPresent(c -> {
            c.setEliminado(true);
            repository.save(c);
        });
    }

    // Reactiva un cargo desactivado
    public void restoreById(Integer id) {
        repository.findById(id).ifPresent(c -> {
            c.setEliminado(false);
            repository.save(c);
        });
    }
}
