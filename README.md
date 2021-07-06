# socle-backend - JPA


https://docs.oracle.com/cd/E16439_01/doc.1013/e13981/undejbs003.htm

http://java.boot.by/scbcd5-guide/ch06.html

https://www.baeldung.com/spring-data-jpa-generate-db-schema

http://blog.paumard.org/cours/jpa/chap03-entite-relation.html

https://www.baeldung.com/jpa-one-to-one

https://vladmihalcea.com/the-best-way-to-map-a-onetoone-relationship-with-jpa-and-hibernate/

https://www.baeldung.com/jpa-cascade-types

https://www.baeldung.com/jpa-hibernate-persistence-context

## Persistence context

Un _persistence context_ est un ensemble d'instances correspondant à des enregistrements en base.
Chaque instance est une instance de classe @Entity.
Dans le _persistence context_ chaque instance d'entité a un état qui évolue dans un cycle de vie.

Le _persistence context_ est le cache de premier niveau (L1) qui contient les entités mappées à des enregistrements en base.
Le _persistence context_ assure un suivi de toutes les modifications qui ont lieu sur les entités.
Lorsqu'une entité est modifiée (CRUD), la modification est persistée en base :
* à la fin de la transaction
* lors d'un flush

Pourquoi un cache ? Si chaque CRUD sur une instance en JVM donnait lieu à la requête équivalente en base de données, alors ça génèrerait trop d'appels.
Les performances seraient dégradées (les appels JDBC vers une base sont coûteux)

## EntityManager

Un EntityManager est associé à un _persistence context_. EntityManager est l'interface qui permet d'interagir avec le _persistence context_.
C'est l'API de EntityManager qui permet de faire évoluer les entités dans le cycle de vie ou de requêter des entités.

## Entity lifecycle

![lifecycle_1](./doc/lifecycle_1.gif?raw=true)

[source](http://java.boot.by/scbcd5-guide/ch06.html)

<hr/>

![lifecycle_2](./doc/lifecycle_2.gif?raw=true)

[source](https://docs.oracle.com/cd/E16439_01/doc.1013/e13981/undejbs003.htm)

<hr/>

[http://blog.paumard.org/cours/jpa/chap03-entite-operations.html](http://blog.paumard.org/cours/jpa/chap03-entite-operations.html)

## Run Postgres

Les diagrammes de tables sont générés avec pgAdmin ERD Tool.

### bare metal
```
C:\'Program Files'\PostgreSQL\13\bin\pg_ctl.exe -D 'C:\Program Files\PostgreSQL\13\data' stop|start
```

### Docker
```
docker-compose up
```

## Run UT javax.persistence.schema-generation

[application.properties](./src/main/resources/application.properties) **javax.persistence.schema-generation.create-source** est valorisé à "metadata".
Donc la génération du schéma est déterminée par les annotations sur les entités.

## Direction d'une relation

La direction d'une relation est définie lors de la conception du MCD. Si on considère par exemple un client et ses commandes :
* Un client peut être défini alors qu'il n'a pas encore passé de commande.
* Au contraire une commande n'est pas définie sans le client associé
* On peut supprimer une commande sans nécessairement supprimer le client associé
* Au contraire, si on supprime un client alors ses commandes seront "orphelines" et il faudra les supprimer
Donc une commande doit faire référence à un client.
En base, la table commande contient un champ qui porte la relation de clé étrangère avec la table client : commande.client_id.
  
Dans le cas d'une commune et d'un maire, on convient ici que c'est la table maire qui fait référence à la table commune, avec un champ maire.commune_id.

### "parent, enfant"

En termes POO, une relation parent-enfant est équivalente à une composition : l'entité enfant ne peut pas exister sans l'entité parent.
Une commune peut exister sans un maire, mais un maire ne peut pas exister sans une commune.

(Un maire en tant que personne peut exister sans sa commune, mais fonctionnellement un maire est défini relativement à une commune)

Donc Maire est l'entité enfant, Commune est l'entité parent.
On va inverser la relation Maire/Commune de cet article [http://blog.paumard.org/cours/jpa/chap03-entite-relation.html](http://blog.paumard.org/cours/jpa/chap03-entite-relation.html)

### "inverse, owning side"

La clé étrangère qui représente la relation est dans la table maire : maire.commune_id. 
Donc Maire est le _owning side_ de la relation Maire Commune.
("the owning side is the entity that has the reference to the other")

Côté Entités JPA, si on définit une relation bidirectionnelle, l'attribut `mappedBy` indique que l'entité est du côté _inverse_ de la relation

La classe Commune peut avoir un attribut Maire, annoté ainsi :
```
@OneToOne(mappedBy="commune")
private Maire maire;
```
Commune est le _inverse side_ de la relation Maire Commune.

## Relation 1:1

### Cas unidirectionnel - [branche des sources](https://github.com/cat-sbr/socle-backend/tree/un-un--unidir)

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

### Cas bidirectionnel - [branche des sources](https://github.com/cat-sbr/socle-backend/tree/OneToOne_bidirectionnel)

Ajouter une référence à l'entité Maire dans la classe Commune **ne change rien au SQL généré**
```
@OneToOne(mappedBy="commune")
private Maire maire;
```

Une relation bidirectionnelle ne crée pas une colonne dans la table parent (commune) vers la table enfant (maire). 
Lorsque l'on veut lire la relation retour, à partir de l'entité parent (Commune), une requête est lancée sur la base, en utilisant le caractère bidirectionnel de la relation.

### fetch - [branches des sources](https://github.com/cat-sbr/socle-backend/tree/OneToOne_bidirectionnel_fetch)

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

### cascade - [branches des sources](https://github.com/cat-sbr/socle-backend/tree/OneToOne_bidirectionnel_cascade)

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

### préconisation vladmihalcea - [branches des sources](https://github.com/cat-sbr/socle-backend/tree/OneToOne_vladmihalcea)

[https://vladmihalcea.com/the-best-way-to-map-a-onetoone-relationship-with-jpa-and-hibernate/](https://vladmihalcea.com/the-best-way-to-map-a-onetoone-relationship-with-jpa-and-hibernate/)

Rappel du schema actuel :

![actuel](./doc/un-un-unidir-v1.png?raw=true)

Avec cette référence dans Commune.java :
```
@OneToOne(mappedBy="commune", cascade=CascadeType.ALL, fetch=FetchType.LAZY, optional=false)
private Maire maire;
```
Lorsqu'on lit une Commune :
```
Commune commune = entityManager.find(Commune.class, niort.getId());
```
Hibernate déclenche 2 select :
```
    select
        commune0_.id as id1_0_0_,
        commune0_.nom as nom2_0_0_ 
    from
        commune commune0_ 
    where
        commune0_.id=?

    select
        maire0_.id as id1_1_0_,
        maire0_.commune_id as commune_3_1_0_,
        maire0_.nom as nom2_1_0_ 
    from
        maire maire0_ 
    where
        maire0_.commune_id=?
```

Pour une association @OneToOne bidirectionnelle,
* non optionnelle `optional=false`
* en `fetch=FetchType.LAZY`
Lorsqu'on lit l'entité parente Commune, alors la relation se comporte comme une FetchType.EAGER !
Et [EAGER n'est pas recommandé](https://vladmihalcea.com/eager-fetching-is-a-code-smell/)

**Pourquoi ce comportement ?**
Pour chaque entité dans le _persistent context_ Hibernate a besoin à la fois :
* du type de l'entité persistante
* et de son id

Hibernate a besoin de connaître l'id de l'entité Maire.
Donc ici, la seule façon de récupérer l'id de l'entité Maire est de faire ce deuxième select.

La table maire a une colonne de clé primaire (id) et une colonne de clé étrangère (commune_id)
Mais il ne peut y avoir qu'un seul maire associé à une commune.
Une solution serait de :
* définir commune_id à la fois comme clé primaire de la table maire et comme clé étrangère
* faire correspondre cette clé maire.commune_id avec la clé primaire de la table commune

Autrement dit pour chaque association Commune Maire, le Maire et la Commune partagent la même clé primaire :
* commune.id
* maire.commune_id

Les colonnes de clés primaire ou de clés étrangères sont indexées. Utiliser la même colonne permet de mutualiser un seul index.

Pour implémenter cette solution, il faut utiliser l'annotation MapsId. Cf Maire.java :
```
@Id
private Long id;

@OneToOne(fetch = FetchType.LAZY)
@MapsId
private Commune commune;
```

Le SQL de schema-generation est le suivant :
```
create sequence hibernate_sequence start 1 increment 1

 create table commune (
    id int8 not null,
     nom varchar(40),
     primary key (id)
 )

create table maire (
   nom varchar(40),
    commune_id int8 not null,
    primary key (commune_id)
)

alter table if exists maire 
   add constraint FKd9365plwnkta8k0i9vi48ea8y 
   foreign key (commune_id) 
   references commune
```

![un-un-bidir-v1](./doc/un-un-bidir-v1.PNG?raw=true)

Mais lorsqu'on lit une Commune :
```
Commune commune = entityManager.find(Commune.class, niort.getId());
```
Hibernate déclenche encore 2 select !
```
    select
        commune0_.id as id1_0_0_,
        commune0_.nom as nom2_0_0_ 
    from
        commune commune0_ 
    where
        commune0_.id=?

    select
        maire0_.commune_id as commune_2_1_0_,
        maire0_.nom as nom1_1_0_ 
    from
        maire maire0_ 
    where
        maire0_.commune_id=?
```

On peut même récupérer un Maire en utilisant l'id de sa Commune, donc on n'a plus besoin d'une relation bidirectionnelle.
Donc on supprime la réfèrence à Maire dans Commune. Le même schéma est généré.

Lorsqu'on requête une Commune, évidemment Hibernate ne fait qu'un seul select.

On peut ensuite requêter son maire avec :
```
Maire maire = entityManager.find(
		Maire.class,
		niort.getId()
);
```

Mais en supprimant la référence à Maire dans Commune, on se prive de l'attribut `cascade`. On ne peut plus mettre à jour le Maire d'une Commune en manipulant uniquement l'entité Commune.

### préconisation Baeldung

En complément de ce qu'on a vu jusque là, Baledung propose une solution 
lorsqu'on implémente une relation optionnelle
lorsqu'on ne veut pas de valeur _null_

[https://www.baeldung.com/jpa-one-to-one#jt-model](https://www.baeldung.com/jpa-one-to-one#jt-model)