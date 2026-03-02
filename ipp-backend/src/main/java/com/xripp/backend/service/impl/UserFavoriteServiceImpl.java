package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.UserFavorite;
import com.xripp.backend.mapper.UserFavoriteMapper;
import com.xripp.backend.service.IUserFavoriteService;
import org.springframework.stereotype.Service;

@Service
public class UserFavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite>
        implements IUserFavoriteService {
}
