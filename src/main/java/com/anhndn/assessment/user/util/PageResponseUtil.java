package com.anhndn.assessment.user.util;

import com.anhndn.assessment.user.controller.response.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

@Slf4j
public class PageResponseUtil {

    public static PageResponse buildPageResponse(Page page){
        int currentPage = page.getNumber() + 1;
        if(currentPage <= page.getTotalPages()) {
            return new PageResponse(page.getTotalPages(), page.hasNext(), page.hasPrevious(), currentPage, page.getSize(), page.getTotalElements());
        } else {
            return new PageResponse(page.getTotalPages(), null, null, null, null, page.getTotalElements());
        }
    }
}
