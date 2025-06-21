package com.epam.rd.autocode.spring.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;

@ToString
@Getter
public class MetaDTO {
    private final int page;

    @JsonProperty("total_count")
    private final long totalCount;

    @JsonProperty("page_size")
    private final int pageSize;

    @JsonProperty("total_pages")
    private final int totalPages;

    @JsonProperty("has_next")
    private final boolean hasNext;

    @JsonProperty("has_previous")
    private final boolean hasPrevious;

    public MetaDTO(Page<?> page) {
        this.page = page.getNumber() + 1;
        this.totalCount = page.getTotalElements();
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        this.hasNext = page.hasNext();
        this.hasPrevious = page.hasPrevious();
    }
}
