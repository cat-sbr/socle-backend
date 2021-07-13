package com.poc.backendpersistencejpa;

import com.poc.backendpersistencejpa.entities.Commune;
import com.poc.backendpersistencejpa.entities.Maire;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

/**
 * Pré-requis : docker-compose up
 */
@SpringBootTest
class BackendPersistenceJpaApplicationTests {

	@Autowired
	EntityManager entityManager;

	@Test
	@Transactional
	void whenParentSavedThenChildSaved() {

		/*
		INSERTS
		 */
		Commune niort = new Commune();
		niort.setNomCommune("Niort");

		Maire juste = new Maire();
		juste.setNom("Leblanc");
		juste.setPrenom("Juste");
		niort.setMaire(juste);

		entityManager.persist(niort);

		entityManager.flush();
		entityManager.clear();

		/*
		SELECTS
		 */
		Commune commune = entityManager.find(Commune.class, niort.getId());

	}

}
