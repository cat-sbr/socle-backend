create sequence hibernate_sequence start 1 increment 1

    create table commune (
       id int8 not null,
        nom varchar(40),
        primary key (id)
    )

    create table maire (
       id int8 not null,
        nom varchar(40),
        commune_id int8,
        primary key (id)
    )

    alter table if exists maire 
       add constraint FKd9365plwnkta8k0i9vi48ea8y 
       foreign key (commune_id) 
       references commune
