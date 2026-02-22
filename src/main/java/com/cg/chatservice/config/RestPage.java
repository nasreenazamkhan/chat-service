package com.cg.chatservice.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * JSON-serializable Page for Redis caching.
 * Spring's PageImpl has no default constructor so Jackson
 * cannot deserialize it — this class fixes that.
 */
@JsonIgnoreProperties(ignoreUnknown = true, value = {"pageable"})
@Schema(hidden = true)   // ← hide from SpringDoc — prevents 500 error
public class RestPage<T> extends PageImpl<T> {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RestPage(
            @JsonProperty("content") List<T> content,
            @JsonProperty("number") int page,
            @JsonProperty("size") int size,
            @JsonProperty("totalElements") long totalElements) {
        super(content, PageRequest.of(page, size == 0 ? 1 : size), totalElements);
    }

    public RestPage(PageImpl<T> page) {
        super(page.getContent(), page.getPageable(), page.getTotalElements());
    }
}

