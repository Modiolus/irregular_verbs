package com.english.irregular_verbs.model;

import org.springframework.context.annotation.Bean;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Optional;

@Entity
@Table(name = "verbgroup3", schema = "english")
public class VerbGroup3 {
    @Id
    private int id;
    private String infinitive;
    private String pastIndefinite;
    private String pastParticiple;
    private String translate;

    public VerbGroup3() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInfinitive() {
        return infinitive;
    }

    public void setInfinitive(String infinitive) {
        this.infinitive = infinitive;
    }

    public String getPastIndefinite() {
        return pastIndefinite;
    }

    public void setPastIndefinite(String pastIndefinite) {
        this.pastIndefinite = pastIndefinite;
    }

    public String getPastParticiple() {
        return pastParticiple;
    }

    public void setPastParticiple(String pastParticiple) {
        this.pastParticiple = pastParticiple;
    }

    public String getTranslate() {
        return translate;
    }

    public void setTranslate(String translate) {
        this.translate = translate;
    }

    @Override
    public String toString() {
        return "IrregularVerb{" +
                "id=" + id +
                ", infinitive='" + infinitive + '\'' +
                ", pastIndefinite='" + pastIndefinite + '\'' +
                ", pastParticiple='" + pastParticiple + '\'' +
                ", translate='" + translate + '\'' +
                '}';
    }
}
