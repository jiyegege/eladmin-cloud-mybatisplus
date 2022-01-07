package com.roger.system.core.service;

import com.roger.common.core.domain.base.BaseService;
import com.roger.system.core.domain.UsersRoles;

import java.util.List;

/**
* @author jinjin
* @date 2020-09-25
*/
public interface UsersRolesService extends BaseService<UsersRoles> {
    List<Long> queryUserIdByRoleId(Long id);
    List<Long> queryRoleIdByUserId(Long id);
    int removeByRoleId(Long id);
    int removeByUserId(Long id);


}
