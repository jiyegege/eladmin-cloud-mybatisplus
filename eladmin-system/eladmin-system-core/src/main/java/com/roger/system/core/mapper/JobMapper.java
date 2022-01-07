package com.roger.system.core.mapper;

import com.roger.common.core.domain.base.CommonMapper;
import com.roger.system.core.domain.Job;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
* @author jinjin
* @date 2020-09-25
*/
@Repository
public interface JobMapper extends CommonMapper<Job> {
    @Select("select j.job_id as id, j.* from sys_job where job_id = #{id}")
    Job selectLink(Long id);
}
