package com.roger.system.core.domain.dto;

import com.roger.common.core.annotation.Query;
import lombok.Data;

/**
* @author jinjin
* @date 2020-09-24
*/
@Data
public class DictQueryParam{

    @Query(blurry = "name,description")
    private String blurry;

    /** 精确 */
    @Query
    private Long dictId;

    /** 模糊 */
    @Query(type = Query.Type.INNER_LIKE)
    private String name;
}
