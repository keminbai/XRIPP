<template>
  <div class="space-y-6">
    <el-alert type="warning" :closable="false" show-icon>
      <template #title>
        数据库备份与恢复由运维/DBA 在基础设施层统一管理，本页仅说明当前备份治理口径，不提供页面内直接备份、恢复或删除能力。
      </template>
    </el-alert>

    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <div class="mb-6">
        <h3 class="text-lg font-bold text-slate-800">备份治理说明</h3>
        <p class="text-xs text-slate-500 mt-1">避免在应用层伪造高风险运维能力，统一以环境级策略执行</p>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="执行主体">运维 / DBA</el-descriptions-item>
        <el-descriptions-item label="执行层级">数据库与存储基础设施</el-descriptions-item>
        <el-descriptions-item label="默认策略">每日自动备份 + 定期校验恢复</el-descriptions-item>
        <el-descriptions-item label="应用侧能力">只读说明，不开放直接恢复</el-descriptions-item>
        <el-descriptions-item label="风险控制">生产恢复需审批、演练、回滚预案</el-descriptions-item>
        <el-descriptions-item label="当前状态">页面不承担真实备份入口</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <h4 class="font-bold text-slate-800 mb-4">建议执行流程</h4>
      <div class="space-y-3 text-sm text-slate-600 leading-7">
        <div>1. 由运维系统或数据库计划任务执行定时备份，不在业务后台手工触发。</div>
        <div>2. 备份文件保存到受控存储，并与数据库实例、时间点、环境标识绑定。</div>
        <div>3. 恢复动作必须由审批后的运维流程执行，避免后台误操作直接覆盖生产数据。</div>
        <div>4. 若后续确有“备份记录展示”需求，应先明确只读展示边界，再决定是否单独建模。</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'admin' })
</script>
