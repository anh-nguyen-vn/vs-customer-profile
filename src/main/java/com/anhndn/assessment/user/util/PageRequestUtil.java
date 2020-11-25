package com.anhndn.assessment.user.util;

import com.anhndn.assessment.user.constant.PageConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Map;

@Slf4j
public class PageRequestUtil {

    public static PageRequest buildPageRequest(Map<String, String> searchCriteriaMap, Sort sort) {
        Boolean paging = Boolean.valueOf(searchCriteriaMap.getOrDefault(PageConstant.KeyName.PAGING_KEY_NAME, "true"));
        Integer pageIndex = NumberUtils.toInt(searchCriteriaMap.get(PageConstant.KeyName.PAGE_INDEX_KEY_NAME), 1);
        Integer pageSize = NumberUtils.toInt(searchCriteriaMap.get(PageConstant.KeyName.RECORD_PER_PAGE_KEY_NAME), PageConstant.DEFAULT_RECORD_PER_PAGE);

        return buildPageRequest(pageIndex, paging, pageSize, sort);
    }

    public static PageRequest buildPageRequest(Integer pageIndex, Boolean paging, Integer pageSize, Sort sort) {
        if (paging) {
            return PageRequest.of(pageIndex - 1, pageSize, sort);
        } else {
            return PageRequest.of(0, Integer.MAX_VALUE, sort);
        }
    }

}
