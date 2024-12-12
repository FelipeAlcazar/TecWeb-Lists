package edu.uclm.esi.listasbe.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import edu.uclm.esi.listasbe.model.Lista;
import jakarta.transaction.Transactional;

public interface ListaDao extends CrudRepository<Lista, String>{
	
	@Query(value="select lista_id from lista_emails_usuarios where emails_usuarios=:email", nativeQuery=true)
	List<String> getListasDe(String email);

    @Modifying
    @Transactional
    @Query(value = "UPDATE lista_emails_usuarios SET confirmado = TRUE WHERE lista_id = :idLista AND emails_usuarios = :email", nativeQuery = true)
    void confirmar(String idLista, String email);

}
