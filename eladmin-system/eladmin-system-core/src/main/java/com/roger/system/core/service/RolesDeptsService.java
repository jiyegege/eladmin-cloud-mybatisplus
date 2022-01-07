package com.roger.system.core.service;

import com.roger.common.core.domain.base.BaseService;
import com.roger.system.core.domain.RolesDepts;

import java.util.List;

/**
* @author jinjin
* @date 2020-09-25
*/
public interface RolesDeptsService extends BaseService<RolesDepts> {

    List<Long> queryDeptIdByRoleId(Long id);
    List<Long> queryRoleIdByDeptId(Long id);
    int removeByRoleId(Long id);
    int removeByDeptId(Long id);
}
