package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.ContentEntity;
import com.xripp.backend.mapper.ContentMapper;
import com.xripp.backend.service.IContentService;
import org.springframework.stereotype.Service;

@Service
public class ContentServiceImpl
        extends ServiceImpl<ContentMapper, ContentEntity>
        implements IContentService {}
