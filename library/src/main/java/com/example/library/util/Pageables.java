package com.example.library.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class Pageables {

    private Pageables() {}   // utility class, never instantiated

    // Append the unique id as a final sort key (if absent) so paging is deterministic.
    public static Pageable withIdTiebreaker(Pageable pageable) {
        Sort sort = pageable.getSort();
        boolean hasId = sort.stream().anyMatch(o -> o.getProperty().equals("id"));
        return hasId ? pageable
            : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort.and(Sort.by("id")));
    }
}
