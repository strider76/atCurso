package com.fot.atcurso.dao;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

@NoRepositoryBean
public interface GenericDAO<D> extends PagingAndSortingRepository<D, Integer>{

}
