package com.xripp.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xripp.backend.entity.StateTransitionLog;
import com.xripp.backend.mapper.StateTransitionLogMapper;
import com.xripp.backend.service.IStateTransitionLogService;
import org.springframework.stereotype.Service;

@Service
public class StateTransitionLogServiceImpl extends ServiceImpl<StateTransitionLogMapper, StateTransitionLog> implements IStateTransitionLogService {
}
