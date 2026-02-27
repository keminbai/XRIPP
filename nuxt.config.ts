// nuxt.config.ts
export default defineNuxtConfig({
  modules: [
    '@element-plus/nuxt',
    '@nuxtjs/tailwindcss'
  ],

  // 这里的配置简化，避免 SCSS 循环引用
  elementPlus: {
    icon: 'ElIcon',
    // 暂时不强制使用 scss 导入，避免初学者环境配置复杂度
    importStyle: 'css',
  },

  app: {
    head: {
      title: 'IPP中小企业国际公共采购服务平台',
      htmlAttrs: {
        lang: 'zh-CN'
      },
      meta: [
        { charset: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
      ],
    }
  },

  // 确保这里路径正确，Nuxt 4 会自动解析 app 目录下的 assets
  css: [
    '~/assets/css/main.css'
  ],

  // 开发环境将前端 /api/* 代理到后端 8080
  routeRules: {
    '/api/**': { proxy: 'http://localhost:8080/api/**' }
  },

  compatibilityDate: '2024-11-01',
  devtools: { enabled: true },
  ssr: true
})
