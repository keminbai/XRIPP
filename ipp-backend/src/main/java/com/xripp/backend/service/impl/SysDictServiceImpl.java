package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.SysDict;
import com.xripp.backend.mapper.SysDictMapper;
import com.xripp.backend.service.ISysDictService;
import org.springframework.stereotype.Service;

@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements ISysDictService {
}
