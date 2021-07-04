package com.poc.backendpersistencejpa.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class Maire implements Serializable {

    @Id
    private Long id;

    @Column(length=40)
    private String nom;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private Commune commune;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Commune getCommune() {
        return commune;
    }

    public void setCommune(Commune commune) {
        this.commune = commune;
    }
}
