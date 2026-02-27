package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.SysUser;
import com.xripp.backend.mapper.SysUserMapper;
import com.xripp.backend.service.ISysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Override
    public SysUser findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return baseMapper.selectOne(new QueryWrapper<SysUser>().eq("username", username));
    }
}
