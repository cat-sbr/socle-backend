package com.poc.backendpersistencejpa.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class Commune implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(length=40)
    private String nom;

    /*
    @OneToOne(mappedBy="commune", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private Maire maire;
     */

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

    /*
    public Maire getMaire() {
        return maire;
    }

    public void setMaire(Maire maire) {
        this.maire = maire;
    }
     */
}