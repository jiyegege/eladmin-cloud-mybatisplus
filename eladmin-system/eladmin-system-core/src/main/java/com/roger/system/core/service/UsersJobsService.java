package com.roger.system.core.service;

import com.roger.common.core.domain.base.BaseService;
import com.roger.system.core.domain.UsersJobs;

import java.util.List;

/**
* @author jinjin
* @date 2020-09-25
*/
public interface UsersJobsService extends BaseService<UsersJobs> {
    List<Long> queryUserIdByJobId(Long id);
    List<Long> queryJobIdByUserId(Long id);
    int removeByUserId(Long id);
    int removeByJobId(Long id);
}
