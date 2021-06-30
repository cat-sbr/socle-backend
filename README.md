# socle-backend - JPA

https://www.baeldung.com/spring-data-jpa-generate-db-schema

http://blog.paumard.org/cours/jpa/chap03-entite-relation.html

## Postgres

### bare metal
```
C:\'Program Files'\PostgreSQL\13\bin\pg_ctl.exe -D 'C:\Program Files\PostgreSQL\13\data' stop|start
```

### Docker
```
docker-compose up
```

## javax.persistence.schema-generation

### Relation 1:1

#### Cas unidirectionnel 

Le SQL de schema-generation est :
```
create sequence hibernate_sequence start 1 increment 1
create table commune (id int8 not null, nom varchar(40), maire_id int8, primary key (id))
create table maire (id int8 not null, nom varchar(40), primary key (id))
alter table if exists commune add constraint FK500quanfbudhlhxqdww1ff32u foreign key (maire_id) references maire
```

application.properties/javax.persistence.schema-generation.create-source est valorisé à "metadata". 
Donc la génération du schéma est déterminée par les annotations sur les entités.

On remarque que le script de génération n'impose pas (!) une relation 1:1.
En effet il faudrait y ajouter une contrainte d'unicité sur la colonne commune.maire_id
pour éviter que plusieurs communes aient le même maire :
```
ALTER TABLE commune ADD UNIQUE (maire_id);
```

Avec cette contrainte, les insert SQL :
```
INSERT INTO public.commune(
id, nom, maire_id)
VALUES (1, 'Thorigné', 1);
INSERT INTO public.commune(
id, nom, maire_id)
VALUES (1, 'Mougon', 1);
```
retournent une alerte :
```
ERROR:  duplicate key value violates unique constraint "commune_pkey"
DETAIL:  Key (id)=(1) already exists.
SQL state: 23505
```

![OneToOne_unidirectionnel](./doc/OneToOne_unidirectionnel.svg?raw=true)