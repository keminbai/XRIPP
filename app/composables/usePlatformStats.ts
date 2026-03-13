import { ref, onMounted } from 'vue'
import { apiRequest } from '@/utils/request'

export interface DashboardStats {
  totals: {
    tenderCount: number
    memberCount: number
    countryCount: number
    monthlyNewMembers: number
    organizationCount?: number
    monthlyTenderCount?: number
    industryCount?: number
    svipCount?: number
    vipCount?: number
    normalCount?: number
  }
  tendersByCategory: { name: string; value: number }[]
  tendersByCountry: { name: string; value: number }[]
  tendersByOrganization?: { name: string; value: number }[]
  memberTrend: { month: string; count: number }[]
  membersByIndustry: { name: string; value: number }[]
  memberLevels?: { name: string; value: number }[]
  memberCities?: { name: string; value: number }[]
  provinceHeat?: { name: string; value: number }[]
  domesticNetwork?: {
    count: number
    provinces: number
    cities: number
    provinceRank: { name: string; value: number }[]
    cityPoints: { name: string; value: number }[]
    list: { name: string; location: string }[]
  }
  overseasNetwork?: {
    count: number
    countries: number
    cities: number
    countryRank: { name: string; value: number }[]
    points: { name: string; country: string; city: string; lat: number; lng: number; value: number }[]
    list: { name: string; location: string }[]
  }
  recentCompanies: string[]
}

const defaultStats: DashboardStats = {
  totals: {
    tenderCount: 0,
    memberCount: 0,
    countryCount: 0,
    monthlyNewMembers: 0,
    organizationCount: 0,
    monthlyTenderCount: 0,
    industryCount: 0,
    svipCount: 0,
    vipCount: 0,
    normalCount: 0
  },
  tendersByCategory: [],
  tendersByCountry: [],
  tendersByOrganization: [],
  memberTrend: [],
  membersByIndustry: [],
  memberLevels: [],
  memberCities: [],
  provinceHeat: [],
  domesticNetwork: {
    count: 0,
    provinces: 0,
    cities: 0,
    provinceRank: [],
    cityPoints: [],
    list: []
  },
  overseasNetwork: {
    count: 0,
    countries: 0,
    cities: 0,
    countryRank: [],
    points: [],
    list: []
  },
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
