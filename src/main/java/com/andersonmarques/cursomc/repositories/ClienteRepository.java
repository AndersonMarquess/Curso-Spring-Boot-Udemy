package com.andersonmarques.cursomc.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.andersonmarques.cursomc.domain.Cliente;
import org.springframework.transaction.annotation.Transactional;

//Essa interface com a anotação @Repository, permite realizar buscas no banco de dados, ela estende do JpaRepository e informa
//Qual o tipo da classe/objeto que será buscado e qual é o ID desse objeto, neste caso nós definimos como Integer. 
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer>{

    //Com base no nome da variavel email ele faz uma busca no banco de dados.
    @Transactional(readOnly = true)
    Cliente findByEmail(String email);

}
