# socle-backend - JPA

https://www.baeldung.com/spring-data-jpa-generate-db-schema

http://blog.paumard.org/cours/jpa/chap03-entite-relation.html

https://www.baeldung.com/jpa-one-to-one

https://vladmihalcea.com/the-best-way-to-map-a-onetoone-relationship-with-jpa-and-hibernate/

https://www.baeldung.com/jpa-cascade-types

## Postgres

Les diagrammes de tables sont générés avec pgAdmin ERD Tool.

### bare metal
```
C:\'Program Files'\PostgreSQL\13\bin\pg_ctl.exe -D 'C:\Program Files\PostgreSQL\13\data' stop|start
```

### Docker
```
docker-compose up
```

## javax.persistence.schema-generation

[application.properties](./src/main/resources/application.properties) **javax.persistence.schema-generation.create-source** est valorisé à "metadata".
Donc la génération du schéma est déterminée par les annotations sur les entités.

En termes POO, une relation parent-enfant est équivalente à une composition : l'entité enfant ne peut pas exister sans l'entité parent.
Une commune peut exister sans un maire, mais un maire ne peut pas exister sans une commune.
Donc Maire est l'entité enfant, Commune est l'entité parent.
On va inverser la relation Maire/Commune de cet article [http://blog.paumard.org/cours/jpa/chap03-entite-relation.html](http://blog.paumard.org/cours/jpa/chap03-entite-relation.html)

### Relation 1:1

#### Cas unidirectionnel - [branche des sources](https://github.com/cat-sbr/socle-backend/tree/un-un--unidir)

Le SQL de schema-generation est :
```
create sequence hibernate_sequence start 1 increment 1
create table commune (id int8 not null, nom varchar(40), primary key (id))
create table maire (id int8 not null, nom varchar(40), commune_id int8, primary key (id))
alter table if exists maire add constraint FKd9365plwnkta8k0i9vi48ea8y foreign key (commune_id) references commune
```

![OneToOne_unidirectionnel](./doc/un-un-unidir-v1.png?raw=true)

La table **maire** a une colonne spéciale **commune_id**. Cette colonne de clé étrangère référence la clé primaire **id** de la table **commune**.
La clé étrangère est dans la table **maire** :
 * **Commune** est la table / l'entité parent
 * **Maire** est la table / l'entité enfant

On remarque que le script de génération n'impose pas (!) une relation 1:1.
En effet il faudrait y ajouter une contrainte d'unicité sur la colonne maire.commune_id
pour éviter que plusieurs maires aient la même commune :
```
ALTER TABLE maire ADD UNIQUE (commune_id);
```

Avec cette contrainte (et uniquement grâce à cette contrainte), les insert SQL :
```
INSERT INTO public.commune(
id, nom)
VALUES (1, 'Thorigné');

INSERT INTO public.maire(
	id, nom, commune_id)
	VALUES (1, 'Pierre', 1);
	
INSERT INTO public.maire(
	id, nom, commune_id)
	VALUES (2, 'Paul', 1);
```
retournent une alerte :
```
ERROR:  duplicate key value violates unique constraint "maire_commune_id_key"
DETAIL:  Key (commune_id)=(1) already exists.
SQL state: 23505
```

#### Cas bidirectionnel - [branche des sources](https://github.com/cat-sbr/socle-backend/tree/OneToOne_bidirectionnel)

Ajouter une référence à l'entité Maire dans la classe Commune **ne change rien au SQL généré**
```
@OneToOne(mappedBy="commune")
private Maire maire;
```

Une relation bidirectionnelle ne crée pas une colonne dans la table parent (commune) vers la table enfant (maire). 
Lorsque l'on veut lire la relation retour, à partir de l'entité parent (Commune), une requête est lancée sur la base, en utilisant le caractère bidirectionnel de la relation.

#### fetch - [branches des sources](https://github.com/cat-sbr/socle-backend/tree/OneToOne_bidirectionnel_fetch)

* FetchType.LAZY : indique que la relation doit être chargée à la demande ; 
* FetchType.EAGER : indique que la relation doit être chargée en même temps que l'entité qui la porte.

```
@OneToOne
private Commune commune;
```
Le comportement par défaut est EAGER :
```
    select
        maire0_.id as id1_1_0_,
        maire0_.commune_id as commune_3_1_0_,
        maire0_.nom as nom2_1_0_,
        commune1_.id as id1_0_1_,
        commune1_.nom as nom2_0_1_ 
    from
        maire maire0_ 
    left outer join
        commune commune1_ 
            on maire0_.commune_id=commune1_.id 
    where
        maire0_.id=?
```

Avec FetchType.LAZY :
```
@OneToOne(fetch = FetchType.LAZY)
private Commune commune;
```
On obtient ce select :
```
    select
        maire0_.id as id1_1_0_,
        maire0_.commune_id as commune_3_1_0_,
        maire0_.nom as nom2_1_0_ 
    from
        maire maire0_ 
    where
        maire0_.id=?
```

#### cascade

Le entityManager permet d'effectuer les opération suivantes sur une entité : DETACH, MERGE, PERSIST, REMOVE, REFRESH.
Le comportement cascade consiste à spécifier ce qui se passe pour une entité (Maire) en relation avec une entité parent (Commune),
lorsque cette entité parent (Commune) subit une des 5 opérations ci-dessus.

Sans la Commune, l'entité Maire n'a pas de sens. Lorsqu'on supprime l'entité Commune, l'entité Maire doit aussi être supprimée.

Que se passe-t-il pour l'entité Maire, lorsqu'on persiste son entité parent Commune ?

Avec le TU whenParentSavedThenChildSaved(), si on laisse la référence telle quelle :
```
@OneToOne(mappedBy="commune")
private Maire maire;
```
On obtient l'erreur :
> object references an unsaved transient instance - save the transient instance before flushing : com.poc.backendpersistencejpa.entities.Commune.maire

Avec l'attribut cascade :
```
@OneToOne(mappedBy="commune", cascade = CascadeType.ALL)
private Maire maire;
```
On obtient ces inserts :
```
    insert 
    into
        commune
        (nom, id) 
    values
        (?, ?)
Hibernate: 
    insert 
    into
        maire
        (commune_id, nom, id) 
    values
        (?, ?, ?)
```

#### préconisations relation 1:1

TODO : explorer https://vladmihalcea.com/the-best-way-to-map-a-onetoone-relationship-with-jpa-and-hibernate/ et tester des inserts...