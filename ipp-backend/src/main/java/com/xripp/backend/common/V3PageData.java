package com.xripp.backend.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class V3PageData<T> {
    private List<T> items;
    private long page;
    private long page_size;
    private long total;
}
