package com.tesis.parcan.repository;

import com.tesis.parcan.model.InstitucionesEclesiasticas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InstitucionesEclesiasticasRepository extends JpaRepository<InstitucionesEclesiasticas, Integer> {
    
    // Orden ascendente por ID (más antiguas primero)
    List<InstitucionesEclesiasticas> findAllByOrderByIdInstitucionEclesiasticaAsc();
    
    Optional<InstitucionesEclesiasticas> findByNombre(String nombre);
    
    @Query("SELECT i FROM InstitucionesEclesiasticas i WHERE LOWER(i.tipo) LIKE LOWER(CONCAT('%', :tipo, '%')) ORDER BY i.idInstitucionEclesiastica ASC")
    List<InstitucionesEclesiasticas> findByTipoContainingIgnoreCase(@Param("tipo") String tipo);
    
    @Query("SELECT i FROM InstitucionesEclesiasticas i WHERE LOWER(i.ciudad) LIKE LOWER(CONCAT('%', :ciudad, '%')) ORDER BY i.idInstitucionEclesiastica ASC")
    List<InstitucionesEclesiasticas> findByCiudadContainingIgnoreCase(@Param("ciudad") String ciudad);
    
    @Query("SELECT i FROM InstitucionesEclesiasticas i WHERE LOWER(i.diosesis) LIKE LOWER(CONCAT('%', :diosesis, '%')) ORDER BY i.idInstitucionEclesiastica ASC")
    List<InstitucionesEclesiasticas> findByDiosesisContainingIgnoreCase(@Param("diosesis") String diosesis);
    
    long countByTipo(String tipo);
}