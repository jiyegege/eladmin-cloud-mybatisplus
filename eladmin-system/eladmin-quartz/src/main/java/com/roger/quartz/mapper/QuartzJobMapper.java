package com.roger.quartz.mapper;

import com.roger.common.core.domain.base.CommonMapper;
import com.roger.quartz.domain.QuartzJob;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* @author jinjin
* @date 2020-09-27
*/
@Repository
public interface QuartzJobMapper extends CommonMapper<QuartzJob> {

    List<QuartzJob> findByIsPauseIsFalse();
}
