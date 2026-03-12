import { reactive, onMounted } from 'vue'
import { apiRequest } from '@/utils/request'

export const usePlatformStats = () => {
  const platformStats = reactive({
    tenderCount: 0,
    memberCount: 0,
    countryCount: 193,
    unAnnualAmount: '$25.7B'
  })

  const loadStats = async () => {
    try {
      await Promise.allSettled([
        apiRequest('/v3/admin/tenders/stats').then((res: any) => {
          const data = res?.data || {}
          let total = 0
          Object.values(data).forEach((v: any) => { total += Number(v || 0) })
          if (total > 0) platformStats.tenderCount = total
        }),

        apiRequest('/v3/admin/members', { query: { page: 1, page_size: 1 } }).then((res: any) => {
          const total = Number(res?.data?.total || 0)
          if (total > 0) platformStats.memberCount = total
        })
      ])
    } catch {
      // Silently fallback to defaults
    }
  }

  if (import.meta.client) {
    onMounted(() => { loadStats() })
  }

  return { platformStats }
}
