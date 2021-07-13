package com.poc.backendpersistencejpa.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class Commune implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(length=40)
    private String nomCommune;

    @Embedded
    private Maire maire;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomCommune() {
        return nomCommune;
    }

    public void setNomCommune(String nomCommune) {
        this.nomCommune = nomCommune;
    }

    public Maire getMaire() {
        return maire;
    }

    public void setMaire(Maire maire) {
        this.maire = maire;
    }
}