package com.tesis.parcan.repository;

import com.tesis.parcan.model.Solicitud;
import com.tesis.parcan.model.Persona;
import com.tesis.parcan.model.InstitucionesEclesiasticas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    // Buscar por Persona (correcto según tu entidad)
    List<Solicitud> findByPersona(Persona persona);

    // Buscar solicitudes de un Feligrés (usando herencia)
    @Query("SELECT s FROM Solicitud s WHERE s.persona.id = :feligresId")
    List<Solicitud> findByFeligresId(@Param("feligresId") Long feligresId);

    // Buscar por institución eclesiástica (a través de la persona/feligres)
    @Query("SELECT s FROM Solicitud s WHERE s.persona.id IN " +
           "(SELECT f.id FROM Feligres f WHERE f.institucionesEclesiasticas = :institucion)")
    List<Solicitud> findByInstitucionEclesiastica(@Param("institucion") InstitucionesEclesiasticas institucion);

    // Buscar por fecha
    List<Solicitud> findByFechaSolicitud(LocalDateTime fechaSolicitud);

    // Buscar entre fechas
    List<Solicitud> findByFechaSolicitudBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Buscar por estado (CORREGIDO)
    List<Solicitud> findByEstado(String estado);

    // Buscar por persona y estado
    List<Solicitud> findByPersonaAndEstado(Persona persona, String estado);

    // Contar por estado
    long countByEstado(String estado);

    // Todas las solicitudes ordenadas por fecha descendente
    List<Solicitud> findAllByOrderByFechaSolicitudDesc();

    // Filtros múltiples (CORREGIDO)
    @Query("SELECT s FROM Solicitud s WHERE " +
           "(:persona IS NULL OR s.persona = :persona) AND " +
           "(:estado IS NULL OR s.estado = :estado) AND " +
           "(:fechaInicio IS NULL OR s.fechaSolicitud >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR s.fechaSolicitud <= :fechaFin)")
    List<Solicitud> findWithFilters(@Param("persona") Persona persona,
                                    @Param("estado") String estado,
                                    @Param("fechaInicio") LocalDateTime fechaInicio,
                                    @Param("fechaFin") LocalDateTime fechaFin);
}