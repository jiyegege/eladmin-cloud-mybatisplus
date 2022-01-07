package com.roger.mnt.domain.query;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Date;
import com.roger.common.core.annotation.Query;
import org.springframework.format.annotation.DateTimeFormat;

/**
* @author jinjin
* @date 2020-09-27
*/
@Getter
@Setter
public class DeployQueryParam{

    private String appName;

    /** BETWEEN */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Query(type = Query.Type.BETWEEN)
    private List<Date> createTime;
}
