<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\finance\pricing.vue
  版本: V1.1 (2026-02-06)
  
  ✅ 核心功能:
  1. [定价管理] 会员费、标书、培训、服务等收费标准
  2. [价格调整] 新增/编辑/删除（本地数据）
  3. [会员优惠] 会员差异化定价配置
  ========================================================================
-->
<template>
  <div class="space-y-6">
    <el-alert type="success" :closable="false" show-icon>
      <template #title>
        定价配置已接入真实 API，保存后会持久化到后台配置中心。
      </template>
    </el-alert>
    <div class="flex justify-end">
      <el-button :loading="loading" @click="loadPricingData">刷新配置</el-button>
    </div>
    <!-- Tab 切换 -->
    <div v-loading="loading" class="bg-white rounded-xl border border-slate-200 shadow-sm">
      <el-tabs v-model="activeTab" class="p-6">
        <!-- 1. 会员费定价 -->
        <el-tab-pane label="会员费定价" name="membership">
          <div class="space-y-4">
            <div class="flex justify-between items-center mb-4">
              <div>
                <h3 class="text-base font-bold text-slate-800">会员费价格体系</h3>
                <p class="text-xs text-slate-500 mt-1">设置不同会员等级的年费标准</p>
              </div>
              <el-button type="primary" @click="openMembershipDialog()">
                <el-icon class="mr-2"><Plus /></el-icon> 新增价格档位
              </el-button>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div
                v-for="tier in membershipTiers"
                :key="tier.id"
                class="border-2 rounded-lg p-6 transition-all hover:shadow-lg"
                :class="tier.isPopular ? 'border-blue-500 bg-blue-50' : 'border-slate-200'"
              >
                <div class="flex justify-between items-start mb-4">
                  <div>
                    <h4 class="text-lg font-bold text-slate-800">{{ tier.name }}</h4>
                    <p class="text-xs text-slate-500 mt-1">{{ tier.description }}</p>
                  </div>
                  <el-tag v-if="tier.isPopular" type="danger" effect="dark" size="small">热门</el-tag>
                </div>

                <div class="mb-6">
                  <div class="text-3xl font-bold text-slate-800">
                    ¥{{ tier.price.toLocaleString() }}
                    <span class="text-sm font-normal text-slate-500">/年</span>
                  </div>
                  <div class="text-xs text-slate-500 mt-1">
                    原价: ¥{{ tier.originalPrice.toLocaleString() }}
                  </div>
                </div>

                <div class="space-y-2 mb-6">
                  <div
                    v-for="(feature, index) in tier.features"
                    :key="index"
                    class="flex items-center gap-2 text-sm"
                  >
                    <el-icon class="text-green-600"><Check /></el-icon>
                    <span class="text-slate-700">{{ feature }}</span>
                  </div>
                </div>

                <div class="flex gap-2">
                  <el-button type="primary" plain class="flex-1" @click="openMembershipDialog(tier)">编辑</el-button>
                  <el-button type="danger" plain @click="handleRemoveMembership(tier)">删除</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 2. 标书定价 -->
        <el-tab-pane label="标书定价" name="tender">
          <div class="space-y-4">
            <div class="flex justify-between items-center mb-4">
              <div>
                <h3 class="text-base font-bold text-slate-800">标书销售价格</h3>
                <p class="text-xs text-slate-500 mt-1">设置标书销售的统一定价</p>
              </div>
              <el-button type="primary" @click="openTenderDialog()">
                <el-icon class="mr-2"><Plus /></el-icon> 新增标书类型
              </el-button>
            </div>

            <el-table :data="tenderPricing" border>
              <el-table-column prop="category" label="标书类型" width="160" />
              <el-table-column label="会员价格" width="150">
                <template #default="scope">
                  <span class="text-green-600 font-bold">¥{{ scope.row.memberPrice }}</span>
                </template>
              </el-table-column>
              <el-table-column label="非会员价格" width="150">
                <template #default="scope">
                  <span class="font-bold">¥{{ scope.row.normalPrice }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="说明" />
              <el-table-column label="操作" width="140">
                <template #default="scope">
                  <el-button link type="primary" @click="openTenderDialog(scope.row)">编辑</el-button>
                  <el-button link type="danger" @click="handleRemoveTender(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 3. 培训定价 -->
        <el-tab-pane label="培训定价" name="training">
          <div class="space-y-4">
            <div class="flex justify-between items-center mb-4">
              <div>
                <h3 class="text-base font-bold text-slate-800">培训课程价格</h3>
                <p class="text-xs text-slate-500 mt-1">设置各类培训课程的收费标准</p>
              </div>
              <el-button type="primary" @click="openTrainingDialog()">
                <el-icon class="mr-2"><Plus /></el-icon> 新增培训价格
              </el-button>
            </div>

            <el-table :data="trainingPricing" border stripe>
              <el-table-column prop="name" label="培训名称" min-width="200" />
              <el-table-column prop="duration" label="时长" width="100" />
              <el-table-column label="标准价格" width="120">
                <template #default="scope">
                  <span class="font-bold">¥{{ scope.row.price }}</span>
                </template>
              </el-table-column>
              <el-table-column label="会员折扣" width="120">
                <template #default="scope">
                  <el-tag type="success" size="small">{{ scope.row.memberDiscount }} 折</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100" align="center">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'" size="small">
                    {{ scope.row.status === 'active' ? '启用' : '停用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180">
                <template #default="scope">
                  <el-button link type="primary" size="small" @click="openTrainingDialog(scope.row)">编辑</el-button>
                  <el-button
                    link
                    :type="scope.row.status === 'active' ? 'danger' : 'success'"
                    size="small"
                    @click="handleToggleTrainingStatus(scope.row)"
                  >
                    {{ scope.row.status === 'active' ? '停用' : '启用' }}
                  </el-button>
                  <el-button link type="danger" size="small" @click="handleRemoveTraining(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 4. 服务定价 -->
        <el-tab-pane label="服务定价" name="service">
          <div class="space-y-4">
            <div class="flex justify-between items-center mb-4">
              <div>
                <h3 class="text-base font-bold text-slate-800">增值服务价格</h3>
                <p class="text-xs text-slate-500 mt-1">标书撰写、咨询服务等增值服务定价</p>
              </div>
              <el-button type="primary" @click="openServiceDialog()">
                <el-icon class="mr-2"><Plus /></el-icon> 新增服务定价
              </el-button>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div
                v-for="service in servicePricing"
                :key="service.id"
                class="border border-slate-200 rounded-lg p-6 hover:shadow-md transition-shadow"
              >
                <div class="flex justify-between items-start mb-4">
                  <div>
                    <h4 class="font-bold text-slate-800">{{ service.name }}</h4>
                    <p class="text-xs text-slate-500 mt-1">{{ service.description }}</p>
                  </div>
                  <div class="flex gap-2">
                    <el-button link type="primary" size="small" @click="openServiceDialog(service)">编辑</el-button>
                    <el-button link type="danger" size="small" @click="handleRemoveService(service)">删除</el-button>
                  </div>
                </div>

                <div class="bg-slate-50 rounded-lg p-4">
                  <div class="flex justify-between items-center mb-2">
                    <span class="text-sm text-slate-600">起步价</span>
                    <span class="text-lg font-bold text-slate-800">¥{{ service.basePrice }}</span>
                  </div>
                  <div class="flex justify-between items-center">
                    <span class="text-sm text-slate-600">{{ service.unit }}</span>
                    <span class="text-sm text-slate-500">{{ service.priceRange }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 会员费弹窗 -->
    <el-dialog v-model="membershipDialogVisible" :title="membershipForm.id ? '编辑会员档位' : '新增会员档位'" width="640px">
      <el-form :model="membershipForm" label-width="120px">
        <el-form-item label="名称" required>
          <el-input v-model="membershipForm.name" placeholder="如：VIP会员" />
        </el-form-item>
        <el-form-item label="描述" required>
          <el-input v-model="membershipForm.description" placeholder="简要说明" />
        </el-form-item>
        <el-form-item label="价格" required>
          <el-input-number v-model="membershipForm.price" :min="0" :step="100" class="!w-full" />
        </el-form-item>
        <el-form-item label="原价">
          <el-input-number v-model="membershipForm.originalPrice" :min="0" :step="100" class="!w-full" />
        </el-form-item>
        <el-form-item label="热门">
          <el-switch v-model="membershipForm.isPopular" />
        </el-form-item>
        <el-form-item label="权益列表">
          <el-input v-model="featuresInput" placeholder="用中文逗号分隔，例如：标书免费下载, 在线咨询" />
          <div class="text-xs text-slate-400 mt-1">保存时会自动拆分为权益列表</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="membershipDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveMembership">保存</el-button>
      </template>
    </el-dialog>

    <!-- 标书弹窗 -->
    <el-dialog v-model="tenderDialogVisible" :title="tenderForm.id ? '编辑标书定价' : '新增标书定价'" width="600px">
      <el-form :model="tenderForm" label-width="120px">
        <el-form-item label="标书类型" required>
          <el-input v-model="tenderForm.category" placeholder="如：联合国标书" />
        </el-form-item>
        <el-form-item label="会员价格" required>
          <el-input-number v-model="tenderForm.memberPrice" :min="0" :step="10" class="!w-full" />
        </el-form-item>
        <el-form-item label="非会员价格" required>
          <el-input-number v-model="tenderForm.normalPrice" :min="0" :step="10" class="!w-full" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="tenderForm.description" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tenderDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveTender">保存</el-button>
      </template>
    </el-dialog>

    <!-- 培训弹窗 -->
    <el-dialog v-model="trainingDialogVisible" :title="trainingForm.id ? '编辑培训定价' : '新增培训定价'" width="600px">
      <el-form :model="trainingForm" label-width="120px">
        <el-form-item label="培训名称" required>
          <el-input v-model="trainingForm.name" />
        </el-form-item>
        <el-form-item label="时长" required>
          <el-input v-model="trainingForm.duration" placeholder="如：2天" />
        </el-form-item>
        <el-form-item label="标准价格" required>
          <el-input-number v-model="trainingForm.price" :min="0" :step="100" class="!w-full" />
        </el-form-item>
        <el-form-item label="会员折扣" required>
          <el-input-number v-model="trainingForm.memberDiscount" :min="1" :max="10" :step="1" class="!w-full" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="trainingForm.status" class="w-full">
            <el-option label="启用" value="active" />
            <el-option label="停用" value="inactive" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="trainingDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveTraining">保存</el-button>
      </template>
    </el-dialog>

    <!-- 服务弹窗 -->
    <el-dialog v-model="serviceDialogVisible" :title="serviceForm.id ? '编辑服务定价' : '新增服务定价'" width="600px">
      <el-form :model="serviceForm" label-width="120px">
        <el-form-item label="服务名称" required>
          <el-input v-model="serviceForm.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="serviceForm.description" />
        </el-form-item>
        <el-form-item label="起步价" required>
          <el-input-number v-model="serviceForm.basePrice" :min="0" :step="100" class="!w-full" />
        </el-form-item>
        <el-form-item label="计费方式">
          <el-input v-model="serviceForm.unit" />
        </el-form-item>
        <el-form-item label="价格区间">
          <el-input v-model="serviceForm.priceRange" placeholder="如：¥5,000 - ¥50,000" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="serviceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveService">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Plus, Check } from '@element-plus/icons-vue'
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

const activeTab = ref('membership')
const loading = ref(false)
const saving = ref(false)

const PRICING_NAMESPACE = 'pricing'
const CONFIG_META = {
  membership_tiers: { name: '会员费定价', sortOrder: 10 },
  tender_pricing: { name: '标书定价', sortOrder: 20 },
  training_pricing: { name: '培训定价', sortOrder: 30 },
  service_pricing: { name: '服务定价', sortOrder: 40 }
} as const

type MembershipTier = {
  id: number
  name: string
  description: string
  price: number
  originalPrice: number
  isPopular: boolean
  features: string[]
}

type TenderPricingItem = {
  id: number
  category: string
  memberPrice: number
  normalPrice: number
  description: string
}

type TrainingPricingItem = {
  id: number
  name: string
  duration: string
  price: number
  memberDiscount: number
  status: string
}

type ServicePricingItem = {
  id: number
  name: string
  description: string
  basePrice: number
  unit: string
  priceRange: string
}

const createDefaultMembershipTiers = (): MembershipTier[] => ([
  { id: 1, name: '普通会员', description: '基础服务权益', price: 2980, originalPrice: 3980, isPopular: false, features: ['标书免费下载 50次/年', '基础培训课程 3次', '标书销售8折优惠', '在线咨询服务'] },
  { id: 2, name: 'VIP会员', description: '高级服务权益', price: 9800, originalPrice: 12800, isPopular: true, features: ['标书免费下载 200次/年', '所有培训课程 不限次数', '标书销售免费', '1对1专属顾问', '优先参加活动', '商机优先推送'] },
  { id: 3, name: 'SVIP会员', description: '尊享服务权益', price: 29800, originalPrice: 39800, isPopular: false, features: ['标书无限下载', '所有培训课程 VIP席位', '标书撰写服务 5次/年', '专属客户经理', '定制化服务方案', '年度战略咨询'] }
])

const createDefaultTenderPricing = (): TenderPricingItem[] => ([
  { id: 1, category: '联合国标书', memberPrice: 0, normalPrice: 299, description: '会员免费，非会员299元/份' },
  { id: 2, category: '政府采购标书', memberPrice: 199, normalPrice: 399, description: '会员199元，非会员399元/份' },
  { id: 3, category: '企业采购标书', memberPrice: 99, normalPrice: 199, description: '会员99元，非会员199元/份' }
])

const createDefaultTrainingPricing = (): TrainingPricingItem[] => ([
  { id: 1, name: 'CIPS国际采购认证', duration: '3天', price: 3999, memberDiscount: 7, status: 'active' },
  { id: 2, name: '联合国采购实战课', duration: '2天', price: 2999, memberDiscount: 8, status: 'active' },
  { id: 3, name: '标书撰写技巧', duration: '1天', price: 1999, memberDiscount: 8, status: 'active' },
  { id: 4, name: '国际贸易实务', duration: '3天', price: 3499, memberDiscount: 7, status: 'inactive' }
])

const createDefaultServicePricing = (): ServicePricingItem[] => ([
  { id: 1, name: '标书撰写服务', description: '专业团队撰写投标文件', basePrice: 5000, unit: '按项目复杂度收费', priceRange: '¥5,000 - ¥50,000' },
  { id: 2, name: '投标咨询服务', description: '投标策略规划与指导', basePrice: 3000, unit: '按咨询时长收费', priceRange: '¥3,000 - ¥20,000' },
  { id: 3, name: '海外认证服务', description: '协助企业获得国际认证', basePrice: 8000, unit: '按认证类型收费', priceRange: '¥8,000 - ¥80,000' },
  { id: 4, name: '展会对接服务', description: '国际展会参展协助', basePrice: 2000, unit: '按展会规模收费', priceRange: '¥2,000 - ¥30,000' }
])

// 会员费
const membershipTiers = ref<MembershipTier[]>(createDefaultMembershipTiers())

// 标书
const tenderPricing = ref<TenderPricingItem[]>(createDefaultTenderPricing())

// 培训
const trainingPricing = ref<TrainingPricingItem[]>(createDefaultTrainingPricing())

// 服务
const servicePricing = ref<ServicePricingItem[]>(createDefaultServicePricing())

// 会员弹窗
const membershipDialogVisible = ref(false)
const membershipForm = ref({ id: 0, name: '', description: '', price: 0, originalPrice: 0, isPopular: false, features: [] as string[] })
const featuresInput = ref('')

const openMembershipDialog = (tier?: any) => {
  if (tier) {
    membershipForm.value = { ...tier }
    featuresInput.value = tier.features.join('，')
  } else {
    membershipForm.value = { id: 0, name: '', description: '', price: 0, originalPrice: 0, isPopular: false, features: [] }
    featuresInput.value = ''
  }
  membershipDialogVisible.value = true
}

const handleSaveMembership = () => {
  const features = featuresInput.value.split(/[,，]/).map(s => s.trim()).filter(Boolean)
  if (!membershipForm.value.name || !membershipForm.value.description) {
    return ElMessage.warning('请填写名称与描述')
  }
  const payload = { ...membershipForm.value, features }
  if (payload.id) {
    const idx = membershipTiers.value.findIndex(x => x.id === payload.id)
    if (idx !== -1) membershipTiers.value[idx] = payload
  } else {
    payload.id = Date.now()
    membershipTiers.value.push(payload)
  }
  membershipDialogVisible.value = false
  void persistPricingSection('membership_tiers', membershipTiers.value, '会员费定价已保存')
}

const handleRemoveMembership = (tier: any) => {
  ElMessageBox.confirm(`确定删除 ${tier.name} 吗？`, '提示', { type: 'warning' }).then(() => {
    membershipTiers.value = membershipTiers.value.filter(x => x.id !== tier.id)
    void persistPricingSection('membership_tiers', membershipTiers.value, '会员费定价已更新')
  })
}

// 标书弹窗
const tenderDialogVisible = ref(false)
const tenderForm = ref({ id: 0, category: '', memberPrice: 0, normalPrice: 0, description: '' })

const openTenderDialog = (row?: any) => {
  tenderForm.value = row ? { ...row } : { id: 0, category: '', memberPrice: 0, normalPrice: 0, description: '' }
  tenderDialogVisible.value = true
}

const handleSaveTender = () => {
  if (!tenderForm.value.category) return ElMessage.warning('请填写标书类型')
  const payload = { ...tenderForm.value }
  if (payload.id) {
    const idx = tenderPricing.value.findIndex(x => x.id === payload.id)
    if (idx !== -1) tenderPricing.value[idx] = payload
  } else {
    payload.id = Date.now()
    tenderPricing.value.push(payload)
  }
  tenderDialogVisible.value = false
  void persistPricingSection('tender_pricing', tenderPricing.value, '标书定价已保存')
}

const handleRemoveTender = (row: any) => {
  ElMessageBox.confirm(`确定删除 ${row.category} 吗？`, '提示', { type: 'warning' }).then(() => {
    tenderPricing.value = tenderPricing.value.filter(x => x.id !== row.id)
    void persistPricingSection('tender_pricing', tenderPricing.value, '标书定价已更新')
  })
}

// 培训弹窗
const trainingDialogVisible = ref(false)
const trainingForm = ref({ id: 0, name: '', duration: '', price: 0, memberDiscount: 8, status: 'active' })

const openTrainingDialog = (row?: any) => {
  trainingForm.value = row ? { ...row } : { id: 0, name: '', duration: '', price: 0, memberDiscount: 8, status: 'active' }
  trainingDialogVisible.value = true
}

const handleSaveTraining = () => {
  if (!trainingForm.value.name || !trainingForm.value.duration) return ElMessage.warning('请填写培训名称与时长')
  const payload = { ...trainingForm.value }
  if (payload.id) {
    const idx = trainingPricing.value.findIndex(x => x.id === payload.id)
    if (idx !== -1) trainingPricing.value[idx] = payload
  } else {
    payload.id = Date.now()
    trainingPricing.value.push(payload)
  }
  trainingDialogVisible.value = false
  void persistPricingSection('training_pricing', trainingPricing.value, '培训定价已保存')
}

const handleToggleTrainingStatus = (row: any) => {
  row.status = row.status === 'active' ? 'inactive' : 'active'
  void persistPricingSection('training_pricing', trainingPricing.value, row.status === 'active' ? '培训定价已启用' : '培训定价已停用')
}

const handleRemoveTraining = (row: any) => {
  ElMessageBox.confirm(`确定删除 ${row.name} 吗？`, '提示', { type: 'warning' }).then(() => {
    trainingPricing.value = trainingPricing.value.filter(x => x.id !== row.id)
    void persistPricingSection('training_pricing', trainingPricing.value, '培训定价已更新')
  })
}

// 服务弹窗
const serviceDialogVisible = ref(false)
const serviceForm = ref({ id: 0, name: '', description: '', basePrice: 0, unit: '', priceRange: '' })

const openServiceDialog = (row?: any) => {
  serviceForm.value = row ? { ...row } : { id: 0, name: '', description: '', basePrice: 0, unit: '', priceRange: '' }
  serviceDialogVisible.value = true
}

const handleSaveService = () => {
  if (!serviceForm.value.name) return ElMessage.warning('请填写服务名称')
  const payload = { ...serviceForm.value }
  if (payload.id) {
    const idx = servicePricing.value.findIndex(x => x.id === payload.id)
    if (idx !== -1) servicePricing.value[idx] = payload
  } else {
    payload.id = Date.now()
    servicePricing.value.push(payload)
  }
  serviceDialogVisible.value = false
  void persistPricingSection('service_pricing', servicePricing.value, '服务定价已保存')
}

const handleRemoveService = (row: any) => {
  ElMessageBox.confirm(`确定删除 ${row.name} 吗？`, '提示', { type: 'warning' }).then(() => {
    servicePricing.value = servicePricing.value.filter(x => x.id !== row.id)
    void persistPricingSection('service_pricing', servicePricing.value, '服务定价已更新')
  })
}

const normalizeNumber = (value: unknown, fallback = 0) => {
  const num = Number(value ?? fallback)
  return Number.isFinite(num) ? num : fallback
}

const normalizeMembershipTiers = (value: unknown): MembershipTier[] => {
  if (!Array.isArray(value)) return createDefaultMembershipTiers()
  return value.map((item: any, index) => ({
    id: normalizeNumber(item?.id, index + 1),
    name: item?.name || '',
    description: item?.description || '',
    price: normalizeNumber(item?.price),
    originalPrice: normalizeNumber(item?.originalPrice),
    isPopular: Boolean(item?.isPopular),
    features: Array.isArray(item?.features) ? item.features.map((x: any) => String(x || '').trim()).filter(Boolean) : []
  }))
}

const normalizeTenderPricing = (value: unknown): TenderPricingItem[] => {
  if (!Array.isArray(value)) return createDefaultTenderPricing()
  return value.map((item: any, index) => ({
    id: normalizeNumber(item?.id, index + 1),
    category: item?.category || '',
    memberPrice: normalizeNumber(item?.memberPrice),
    normalPrice: normalizeNumber(item?.normalPrice),
    description: item?.description || ''
  }))
}

const normalizeTrainingPricing = (value: unknown): TrainingPricingItem[] => {
  if (!Array.isArray(value)) return createDefaultTrainingPricing()
  return value.map((item: any, index) => ({
    id: normalizeNumber(item?.id, index + 1),
    name: item?.name || '',
    duration: item?.duration || '',
    price: normalizeNumber(item?.price),
    memberDiscount: normalizeNumber(item?.memberDiscount, 8),
    status: item?.status === 'inactive' ? 'inactive' : 'active'
  }))
}

const normalizeServicePricing = (value: unknown): ServicePricingItem[] => {
  if (!Array.isArray(value)) return createDefaultServicePricing()
  return value.map((item: any, index) => ({
    id: normalizeNumber(item?.id, index + 1),
    name: item?.name || '',
    description: item?.description || '',
    basePrice: normalizeNumber(item?.basePrice),
    unit: item?.unit || '',
    priceRange: item?.priceRange || ''
  }))
}

const loadPricingData = async () => {
  loading.value = true
  try {
    const res: any = await apiRequest(`/v3/admin/configs/${PRICING_NAMESPACE}`)
    const items = Array.isArray(res?.data?.items) ? res.data.items : []
    const itemMap = items.reduce((acc: Record<string, any>, item: any) => {
      acc[item.key] = item.value
      return acc
    }, {})

    membershipTiers.value = normalizeMembershipTiers(itemMap.membership_tiers)
    tenderPricing.value = normalizeTenderPricing(itemMap.tender_pricing)
    trainingPricing.value = normalizeTrainingPricing(itemMap.training_pricing)
    servicePricing.value = normalizeServicePricing(itemMap.service_pricing)
  } catch (error: any) {
    ElMessage.error(error?.message || '定价配置加载失败')
  } finally {
    loading.value = false
  }
}

const persistPricingSection = async (
  key: keyof typeof CONFIG_META,
  value: unknown,
  successMessage: string
) => {
  saving.value = true
  try {
    const meta = CONFIG_META[key]
    await apiRequest(`/v3/admin/configs/${PRICING_NAMESPACE}/batch`, {
      method: 'POST',
      body: {
        items: [
          {
            key,
            name: meta.name,
            sort_order: meta.sortOrder,
            enabled: true,
            value
          }
        ]
      }
    })
    ElMessage.success(successMessage)
    await loadPricingData()
  } catch (error: any) {
    ElMessage.error(error?.message || '定价配置保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loadPricingData()
})
</script>
