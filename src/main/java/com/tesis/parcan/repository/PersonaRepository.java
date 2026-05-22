package com.tesis.parcan.repository;

import com.tesis.parcan.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {
    
    // Buscar por CI (solo no eliminados)
    Optional<Persona> findByCiAndEliminadoFalse(String ci);
    
    // Verificar si existe CI (solo no eliminados)
    boolean existsByCiAndEliminadoFalse(String ci);
    
    // Listar todos NO eliminados ordenados ascendente por ID
    List<Persona> findAllByEliminadoFalseOrderByIdPersonaAsc();
    
    // Listar todos ELIMINADOS ordenados ascendente por ID
    List<Persona> findAllByEliminadoTrueOrderByIdPersonaAsc();
    
    // Buscar por nombre en NO eliminados
    @Query("SELECT p FROM Persona p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND p.eliminado = false ORDER BY p.idPersona ASC")
    List<Persona> buscarPorNombre(@Param("nombre") String nombre);
    
    // Buscar por apellido en NO eliminados
    @Query("SELECT p FROM Persona p WHERE LOWER(p.apellido) LIKE LOWER(CONCAT('%', :apellido, '%')) AND p.eliminado = false ORDER BY p.idPersona ASC")
    List<Persona> buscarPorApellido(@Param("apellido") String apellido);
    
    // Buscar por nombre y apellido en NO eliminados
    @Query("SELECT p FROM Persona p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND LOWER(p.apellido) LIKE LOWER(CONCAT('%', :apellido, '%')) AND p.eliminado = false ORDER BY p.idPersona ASC")
    List<Persona> buscarPorNombreYApellido(@Param("nombre") String nombre, @Param("apellido") String apellido);
    
    // Buscar por CI (incluye eliminados)
    @Query("SELECT p FROM Persona p WHERE LOWER(p.ci) LIKE LOWER(CONCAT('%', :ci, '%')) AND p.eliminado = false ORDER BY p.idPersona ASC")
    List<Persona> buscarPorCi(@Param("ci") String ci);
    
    // Buscar por teléfono en NO eliminados
    @Query("SELECT p FROM Persona p WHERE LOWER(p.telefono) LIKE LOWER(CONCAT('%', :telefono, '%')) AND p.eliminado = false ORDER BY p.idPersona ASC")
    List<Persona> buscarPorTelefono(@Param("telefono") String telefono);
    
    // Eliminación lógica
    @Modifying
    @Transactional
    @Query("UPDATE Persona p SET p.eliminado = true, p.fechaEliminacion = CURRENT_DATE WHERE p.idPersona = :id")
    void softDelete(@Param("id") Long id);
    
    // Restaurar persona
    @Modifying
    @Transactional
    @Query("UPDATE Persona p SET p.eliminado = false, p.fechaEliminacion = null WHERE p.idPersona = :id")
    void restore(@Param("id") Long id);
}