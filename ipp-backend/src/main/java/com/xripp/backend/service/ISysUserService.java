package com.xripp.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xripp.backend.entity.SysUser;

public interface ISysUserService extends IService<SysUser> {
    SysUser findByUsername(String username);
}
