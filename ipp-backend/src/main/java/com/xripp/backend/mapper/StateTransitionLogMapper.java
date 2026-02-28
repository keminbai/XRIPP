package com.xripp.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xripp.backend.entity.StateTransitionLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StateTransitionLogMapper extends BaseMapper<StateTransitionLog> {
}
