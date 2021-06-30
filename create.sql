create sequence hibernate_sequence start 1 increment 1
create table commune (id int8 not null, nom varchar(40), maire_id int8, primary key (id))
create table maire (id int8 not null, nom varchar(40), primary key (id))
alter table if exists commune add constraint FK500quanfbudhlhxqdww1ff32u foreign key (maire_id) references maire
