<script setup lang="ts">
import { computed } from 'vue'
import { NConfigProvider, NDialogProvider, NMessageProvider, zhCN, dateZhCN } from 'naive-ui'

const themeOverrides = computed(() => ({
  common: {
    primaryColor: '#1f8f78',
    primaryColorHover: '#259780',
    primaryColorPressed: '#156b5a',
    primaryColorSuppl: '#259780',
    infoColor: '#225f6d',
    successColor: '#1f8f78',
    warningColor: '#d2843f',
    errorColor: '#c64d3e',
    borderRadius: '18px',
    borderColor: 'rgba(91, 108, 98, 0.16)',
    fontFamily:
      '"Segoe UI", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif',
    textColorBase: '#182420',
    bodyColor: 'rgba(255, 255, 255, 0.82)',
    cardColor: 'rgba(255, 255, 255, 0.88)',
    modalColor: '#ffffff',
    popoverColor: '#ffffff',
    tableColor: 'transparent',
  },
  Layout: {
    color: 'transparent',
    siderColor: 'rgba(246, 247, 242, 0.92)',
    headerColor: 'transparent',
  },
  Card: {
    color: 'transparent',
    borderColor: 'rgba(91, 108, 98, 0.16)',
    borderRadius: '22px',
    boxShadow: '0 12px 32px rgba(25, 41, 35, 0.08)',
    titleTextColor: '#182420',
    textColor: '#52615c',
  },
  Button: {
    borderRadiusMedium: '14px',
    borderRadiusSmall: '12px',
    textColorPrimary: '#f8faf8',
    textColorHoverPrimary: '#f8faf8',
    textColorPressedPrimary: '#f8faf8',
    colorPrimary: '#1f8f78',
    colorHoverPrimary: '#259780',
    colorPressedPrimary: '#156b5a',
    colorFocusPrimary: '#259780',
  },
  Input: {
    borderRadius: '14px',
    color: 'rgba(255, 255, 255, 0.86)',
    colorFocus: '#ffffff',
  },
  Select: {
    peers: {
      InternalSelection: {
        borderRadius: '14px',
      },
    },
  },
  Menu: {
    itemColorActive: 'rgba(31, 143, 120, 0.12)',
    itemTextColorActive: '#156b5a',
    itemTextColorActiveHover: '#156b5a',
    itemIconColorActive: '#156b5a',
    itemTextColorHover: '#182420',
    itemColorHover: 'rgba(91, 108, 98, 0.08)',
    borderRadius: '14px',
  },
  DataTable: {
    thColor: 'rgba(248, 250, 247, 0.92)',
    tdColor: 'rgba(255, 255, 255, 0.2)',
    borderColor: 'rgba(91, 108, 98, 0.12)',
    thTextColor: '#52615c',
    tdTextColor: '#182420',
  },
  Drawer: {
    color: 'rgba(250, 251, 248, 0.94)',
  },
}))
</script>

<template>
  <n-config-provider :locale="zhCN" :date-locale="dateZhCN" :theme-overrides="themeOverrides">
    <n-message-provider>
      <n-dialog-provider>
        <router-view />
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>
