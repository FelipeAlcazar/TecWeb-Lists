package edu.uclm.esi.listasbe.dao;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import edu.uclm.esi.listasbe.model.Producto;

import jakarta.transaction.Transactional;

public interface ProductoDao extends CrudRepository<Producto, String>{

    @Modifying
    @Transactional
    @Query(value = "UPDATE producto SET unidades_compradas = :unidadesCompradas WHERE id = :id", nativeQuery = true)
    void comprar(String id, float unidadesCompradas);

}
