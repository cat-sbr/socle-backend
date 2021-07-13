package com.poc.backendpersistencejpa.entities;

import javax.persistence.*;
import java.io.Serializable;

@Embeddable
public class Maire implements Serializable {

    @Column(length=40)
    private String nom;

    @Column(length=40)
    private String prenom;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
}
