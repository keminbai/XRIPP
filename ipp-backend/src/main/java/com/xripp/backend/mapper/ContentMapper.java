package com.xripp.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xripp.backend.entity.ContentEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ContentMapper extends BaseMapper<ContentEntity> {
}
