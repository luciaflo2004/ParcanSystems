package com.tesis.parcan.repository;

import com.tesis.parcan.model.Feligres;
import com.tesis.parcan.model.InstitucionesEclesiasticas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeligresRepository extends JpaRepository<Feligres, Long> {

    // Buscar por CI - los campos son heredados de Persona
    Optional<Feligres> findByCi(String ci);
    
    // Buscar por CI ignorando mayúsculas/minúsculas
    @Query("SELECT f FROM Feligres f WHERE LOWER(f.ci) = LOWER(:ci)")
    Optional<Feligres> findByCiIgnoreCase(@Param("ci") String ci);

    // Buscar por nombre (ignorando mayúsculas/minúsculas)
    List<Feligres> findByNombreContainingIgnoreCase(String nombre);
    
    // Buscar por apellido (ignorando mayúsculas/minúsculas)
    List<Feligres> findByApellidoContainingIgnoreCase(String apellido);

    // Buscar por nombre completo
    @Query("SELECT f FROM Feligres f WHERE LOWER(CONCAT(f.nombre, ' ', f.apellido)) LIKE LOWER(CONCAT('%', :nombreCompleto, '%'))")
    List<Feligres> findByNombreCompletoContainingIgnoreCase(@Param("nombreCompleto") String nombreCompleto);

    // Buscar por institución eclesiástica
    List<Feligres> findByInstitucionesEclesiasticas(InstitucionesEclesiasticas institucion);

    // Buscar por ID de institución eclesiástica
    List<Feligres> findByInstitucionesEclesiasticas_IdInstitucionEclesiastica(Long idInstitucion);
    
    // Buscar feligreses activos
    List<Feligres> findByActivoTrue();
    
    // Buscar feligreses por estado
    List<Feligres> findByActivo(boolean activo);
    
    // Buscar feligreses no eliminados
    @Query("SELECT f FROM Feligres f WHERE f.eliminado = false")
    List<Feligres> findAllNoEliminados();
    
    // Buscar feligreses activos y no eliminados
    @Query("SELECT f FROM Feligres f WHERE f.activo = true AND f.eliminado = false")
    List<Feligres> findActivosNoEliminados();
    
    // Contar feligreses activos
    long countByActivoTrue();
    
    // Contar feligreses no eliminados
    @Query("SELECT COUNT(f) FROM Feligres f WHERE f.eliminado = false")
    long countNoEliminados();
    
    // Verificar si existe por CI
    boolean existsByCi(String ci);
    
    // Verificar si existe por CI ignorando un ID (para actualizaciones)
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Feligres f WHERE f.ci = :ci AND f.idPersona != :id")
    boolean existsByCiAndIdNot(@Param("ci") String ci, @Param("id") Long id);
}