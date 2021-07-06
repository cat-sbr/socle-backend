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
		niort.setNom("Niort");
		//niort.setMaire(pierre);
		entityManager.persist(niort);

		Maire pierre = new Maire();
		pierre.setNom("Pierre");
		pierre.setCommune(niort);
		entityManager.persist(pierre);

		entityManager.flush();
		entityManager.clear();

		/*
		SELECTS
		 */
		Commune commune = entityManager.find(Commune.class, niort.getId());

		/*
		Modifier le maire de Niort
		 */
		Maire maireDeNiort = entityManager.find(
				Maire.class,
				niort.getId()
		);
		maireDeNiort.setNom("Paul");
		entityManager.persist(maireDeNiort);
		entityManager.flush();
		entityManager.clear();

	}

}
