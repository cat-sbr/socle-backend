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
	void simpleTest() {

		/*
		INSERTS
		 */
		Commune niort = new Commune();
		niort.setNom("Niort");
		/* merge returns the managed instance that the state was merged to. */
		niort = entityManager.merge(niort);

		Maire pierre = new Maire();
		pierre.setNom("Pierre");
		pierre.setCommune(niort);
		entityManager.persist(pierre);
		entityManager.flush();

		/*
		SELECTS
		 */
		entityManager.clear();
		Maire maire = entityManager.find(Maire.class, pierre.getId());
		System.out.println(maire.getId());
	}

}
