package com.english.irregular_verbs.repositories;

import com.english.irregular_verbs.model.IrregularVerb;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IrregularVerbRepository extends JpaRepository<IrregularVerb, Integer> {

}
