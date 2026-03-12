import { ref, onMounted } from 'vue'
import { apiRequest } from '@/utils/request'

export interface DashboardStats {
  totals: {
    tenderCount: number
    memberCount: number
    countryCount: number
    monthlyNewMembers: number
  }
  tendersByCategory: { name: string; value: number }[]
  tendersByCountry: { name: string; value: number }[]
  memberTrend: { month: string; count: number }[]
  membersByIndustry: { name: string; value: number }[]
  recentCompanies: string[]
}

const defaultStats: DashboardStats = {
  totals: { tenderCount: 0, memberCount: 0, countryCount: 193, monthlyNewMembers: 0 },
  tendersByCategory: [],
  tendersByCountry: [],
  memberTrend: [],
  membersByIndustry: [],
  recentCompanies: []
}

export const usePlatformStats = () => {
  const stats = ref<DashboardStats>({ ...defaultStats })
  const loaded = ref(false)

  const loadStats = async () => {
    try {
      const res: any = await apiRequest('/v3/dashboard/stats')
      if (res?.data) {
        stats.value = { ...defaultStats, ...res.data }
        loaded.value = true
      }
    } catch {
      // Silently fallback to defaults
    }
  }

  if (import.meta.client) {
    onMounted(() => { loadStats() })
  }

  return { stats, loaded, loadStats }
}
