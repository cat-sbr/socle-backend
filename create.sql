create sequence hibernate_sequence start 1 increment 1

    create table commune (
       id int8 not null,
        nom varchar(40),
        prenom varchar(40),
        nom_commune varchar(40),
        primary key (id)
    )
