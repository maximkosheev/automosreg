package net.monsterdev.automosreg.repository;

import net.monsterdev.automosreg.domain.FilterOption;
import net.monsterdev.automosreg.enums.FilterType;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class FiltersReponitory {
    @PersistenceContext
    private EntityManager em;

    public FilterOption save(FilterOption filter) {
        em.persist(filter);
        return filter;
    }

    public List<FilterOption> findAll(FilterType filterType) {
        TypedQuery<FilterOption> query = em.createQuery("from FilterOption filterOption where filterOption.type = (:type)", FilterOption.class);
        query.setParameter("type", filterType);
        return query.getResultList();
    }
}
