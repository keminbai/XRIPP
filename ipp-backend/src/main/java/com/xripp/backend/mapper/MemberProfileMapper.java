package com.xripp.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xripp.backend.entity.MemberProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberProfileMapper extends BaseMapper<MemberProfile> {
}
