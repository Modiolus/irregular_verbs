package com.english.irregular_verbs.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "irregularverb", schema = "english")
public class IrregularVerb {
    @Id
    private int id;

    private String infinitive;
    private String pastIndefinite;
    private String pastParticiple;
    private String translate;

    public IrregularVerb() {
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
