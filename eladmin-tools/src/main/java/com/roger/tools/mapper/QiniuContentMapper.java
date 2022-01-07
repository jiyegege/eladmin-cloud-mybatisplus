package com.roger.tools.mapper;

import com.roger.common.core.domain.base.CommonMapper;
import com.roger.tools.domain.QiniuContent;
import org.springframework.stereotype.Repository;

/**
* @author jinjin
* @date 2020-09-27
*/
@Repository
public interface QiniuContentMapper extends CommonMapper<QiniuContent> {

    QiniuContent findByKey(String key);
}
