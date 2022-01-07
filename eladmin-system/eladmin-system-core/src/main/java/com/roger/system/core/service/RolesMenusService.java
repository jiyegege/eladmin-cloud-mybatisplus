package com.roger.system.core.service;

import com.roger.common.core.domain.base.BaseService;
import com.roger.system.core.domain.RolesMenus;

import java.util.List;

/**
* @author jinjin
* @date 2020-09-25
*/
public interface RolesMenusService extends BaseService<RolesMenus> {
    List<Long> queryMenuIdByRoleId(Long id);
    List<Long> queryRoleIdByMenuId(Long id);
    int removeByRoleId(Long id);
    int removeByMenuId(Long id);
}
