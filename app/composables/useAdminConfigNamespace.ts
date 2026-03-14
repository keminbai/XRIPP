import { ref } from 'vue'
import { apiRequest } from '@/utils/request'

type AdminConfigMeta = {
  name: string
  sortOrder: number
  description?: string
  enabled?: boolean
}

type SaveItemPayload = {
  key: string
  value: unknown
  name?: string
  description?: string
  sortOrder?: number
  enabled?: boolean
}

export const useAdminConfigNamespace = (
  namespace: string,
  meta: Record<string, AdminConfigMeta>
) => {
  const loading = ref(false)
  const saving = ref(false)

  const loadNamespaceItems = async () => {
    loading.value = true
    try {
      const res: any = await apiRequest(`/v3/admin/configs/${namespace}`)
      const items = Array.isArray(res?.data?.items) ? res.data.items : []
      return items.reduce((acc: Record<string, any>, item: any) => {
        acc[item.key] = item.value
        return acc
      }, {})
    } finally {
      loading.value = false
    }
  }

  const saveNamespaceItems = async (items: SaveItemPayload[]) => {
    saving.value = true
    try {
      await apiRequest(`/v3/admin/configs/${namespace}/batch`, {
        method: 'POST',
        body: {
          items: items.map((item) => {
            const itemMeta = meta[item.key] || {
              name: item.key,
              sortOrder: 0
            }
            return {
              key: item.key,
              name: item.name ?? itemMeta.name,
              description: item.description ?? itemMeta.description ?? '',
              sort_order: item.sortOrder ?? itemMeta.sortOrder ?? 0,
              enabled: item.enabled ?? itemMeta.enabled ?? true,
              value: item.value
            }
          })
        }
      })
    } finally {
      saving.value = false
    }
  }

  const deleteNamespaceItem = async (key: string) => {
    saving.value = true
    try {
      await apiRequest(`/v3/admin/configs/${namespace}/${key}`, {
        method: 'DELETE'
      })
    } finally {
      saving.value = false
    }
  }

  return {
    loading,
    saving,
    loadNamespaceItems,
    saveNamespaceItems,
    deleteNamespaceItem
  }
}
