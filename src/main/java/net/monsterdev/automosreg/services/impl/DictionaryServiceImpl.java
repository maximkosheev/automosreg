package net.monsterdev.automosreg.services.impl;

import net.monsterdev.automosreg.domain.FilterOption;
import net.monsterdev.automosreg.enums.FilterType;
import net.monsterdev.automosreg.repository.FiltersReponitory;
import net.monsterdev.automosreg.services.DictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Qualifier("dictionaryService")
public class DictionaryServiceImpl implements DictionaryService {
    @Autowired
    private FiltersReponitory filtersReponitory;

    @Override
    public FilterOption saveFilter(FilterType type, FilterOption filter) {
        filter.setType(type);
        return filtersReponitory.save(filter);
    }

    @Override
    public List<FilterOption> findAllFilters(FilterType type) {
        return filtersReponitory.findAll(type);
    }
}
