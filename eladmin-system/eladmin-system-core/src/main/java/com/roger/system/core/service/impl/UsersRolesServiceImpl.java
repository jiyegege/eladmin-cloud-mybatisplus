package com.roger.system.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.roger.common.core.domain.base.impl.BaseServiceImpl;
import com.roger.system.core.domain.UsersRoles;
import com.roger.system.core.mapper.UsersRolesMapper;
import com.roger.system.core.service.UsersRolesService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by jinjin on 2020-09-25.
 */
@AllArgsConstructor
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class UsersRolesServiceImpl extends BaseServiceImpl<UsersRoles> implements UsersRolesService {

    private final UsersRolesMapper usersRolesMapper;

    @Override
    public List<Long> queryUserIdByRoleId(Long id) {
        LambdaQueryWrapper<UsersRoles> query = new LambdaQueryWrapper<>();
        query.eq(UsersRoles::getRoleId, id);
        return usersRolesMapper.selectList(query).stream().map(UsersRoles::getUserId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> queryRoleIdByUserId(Long id) {
        LambdaQueryWrapper<UsersRoles> query = new LambdaQueryWrapper<>();
        query.eq(UsersRoles::getUserId, id);
        return usersRolesMapper.selectList(query).stream().map(UsersRoles::getRoleId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeByRoleId(Long id) {
        LambdaUpdateWrapper<UsersRoles> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UsersRoles::getRoleId, id);
        return usersRolesMapper.delete(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeByUserId(Long id) {
        LambdaUpdateWrapper<UsersRoles> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UsersRoles::getUserId, id);
        return usersRolesMapper.delete(wrapper);
    }

}
