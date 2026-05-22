package com.tesis.parcan.repository;

import com.tesis.parcan.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    // Búsqueda por nombre del servicio (parcial e ignorando mayúsculas)
    List<Servicio> findByNombreServicioContainingIgnoreCase(String nombreServicio);

    // Búsqueda por descripción
    List<Servicio> findByDescripcionContainingIgnoreCase(String descripcion);

    // Buscar servicios con costo mayor o igual a un valor
    List<Servicio> findByCostoServicioGreaterThanEqual(Integer costo);

    // Buscar servicios con costo entre un rango
    List<Servicio> findByCostoServicioBetween(Integer minCosto, Integer maxCosto);

    // Ordenar por costo ascendente
    List<Servicio> findAllByOrderByCostoServicioAsc();

    // Ordenar por nombre ascendente
    List<Servicio> findAllByOrderByNombreServicioAsc();

    // Ordenar por fecha de servicio descendente (más recientes primero)
    List<Servicio> findAllByOrderByFechaServicioDesc();
}