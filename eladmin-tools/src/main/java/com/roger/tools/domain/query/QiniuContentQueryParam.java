package com.roger.tools.domain.query;

import com.roger.common.core.annotation.Query;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
* @author jinjin
* @date 2020-09-27
*/
@Getter
@Setter
public class QiniuContentQueryParam{

    @Query(type = Query.Type.INNER_LIKE)
    private String key;

    /** BETWEEN */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Query(type = Query.Type.BETWEEN)
    private List<Date> createTime;
}
