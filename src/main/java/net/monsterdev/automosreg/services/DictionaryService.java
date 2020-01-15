package net.monsterdev.automosreg.services;

import net.monsterdev.automosreg.domain.FilterOption;
import net.monsterdev.automosreg.enums.FilterType;

import java.util.List;

public interface DictionaryService {
    /**
     * Сохраняет новый фильтр
     * @param type - тип фильтра
     * @param filter - новый фильтр
     * @return - возвращает сохраненный фильтр
     */
    FilterOption saveFilter(FilterType type, FilterOption filter);

    /**
     * Возвращает все фильтры указанного типа, сохраненные в БД
     * @return список фильтров по предложениям
     */
    List<FilterOption> findAllFilters(FilterType type);
}
