<template>
  <section class="section stats-strip">
    <header class="section-header" v-if="title || showRefresh">
      <h3 v-if="title">{{ title }}</h3>
      <button
        v-if="showRefresh"
        class="refresh-btn"
        :disabled="loading"
        @click="refreshStats"
      >
        {{ loading ? '刷新中...' : '刷新' }}
      </button>
    </header>
    <div class="stats-grid">
      <div class="stat-card primary">
        <div class="stat-label">设备总数</div>
        <div class="stat-value">{{ stats.totalDevices ?? 0 }}</div>
      </div>
      <div class="stat-card success">
        <div class="stat-label">监控中数量</div>
        <div class="stat-value">{{ stats.monitoringCount ?? 0 }}</div>
      </div>
      <div class="stat-card warning">
        <div class="stat-label">监控中断数量</div>
        <div class="stat-value">{{ stats.monitorInterruptCount ?? 0 }}</div>
      </div>
      <div class="stat-card neutral">
        <div class="stat-label">未监控数量</div>
        <div class="stat-value">{{ stats.monitorOffCount ?? 0 }}</div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import axios from 'axios';

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  showRefresh: {
    type: Boolean,
    default: true
  }
});

const apiBase = import.meta.env.VITE_API_BASE_URL || '';
const stats = ref({
  totalDevices: 0,
  monitoringCount: 0,
  monitorInterruptCount: 0,
  monitorOffCount: 0
});
const loading = ref(false);

const refreshStats = async () => {
  if (loading.value) return;
  loading.value = true;
  try {
    const requests = [
      axios.get(`${apiBase}/api/device/countall`).catch(() => ({ data: { data: 0 } })),
      axios.get(`${apiBase}/api/device/monitoron-countnow`).catch(() => ({ data: { data: 0 } })),
      axios.get(`${apiBase}/api/device/monitorinterrupt-countnow`).catch(() => ({ data: { data: 0 } })),
      axios.get(`${apiBase}/api/device/monitoroff-countnow`).catch(() => ({ data: { data: 0 } }))
    ];
    const [totalRes, monitoringRes, interruptRes, offRes] = await Promise.all(requests);
    stats.value = {
      totalDevices: Number(totalRes.data?.data ?? 0),
      monitoringCount: Number(monitoringRes.data?.data ?? 0),
      monitorInterruptCount: Number(interruptRes.data?.data ?? 0),
      monitorOffCount: Number(offRes.data?.data ?? 0)
    };
  } catch (error) {
    console.error('获取设备监控状态统计失败:', error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  refreshStats();
});

defineExpose({ refreshStats });
</script>

<style scoped>
.stats-strip {
  margin-bottom: 1.2rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.section-header h3 {
  margin: 0;
  font-size: 1rem;
  color: #f3f4f6;
}

.refresh-btn {
  background: transparent;
  border: 1px solid rgba(148, 163, 184, 0.4);
  border-radius: 6px;
  padding: 0.25rem 0.6rem;
  color: #cbd5f5;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.2s;
}

.refresh-btn:hover:not(:disabled) {
  background: rgba(148, 163, 184, 0.1);
  border-color: rgba(148, 163, 184, 0.6);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 0.9rem;
}

.stat-card {
  background: rgba(30, 41, 59, 0.9);
  border-radius: 12px;
  padding: 1rem;
  border: 1px solid rgba(148, 163, 184, 0.25);
}

.stat-card.primary {
  border-color: rgba(96, 165, 250, 0.5);
}

.stat-card.success {
  border-color: rgba(34, 197, 94, 0.5);
}

.stat-card.warning {
  border-color: rgba(251, 191, 36, 0.5);
}

.stat-card.neutral {
  border-color: rgba(203, 213, 225, 0.5);
}

.stat-label {
  font-size: 0.85rem;
  color: #94a3b8;
  margin-bottom: 0.4rem;
}

.stat-value {
  font-size: 1.8rem;
  font-weight: 600;
  color: #93c5fd;
}

.stat-card.success .stat-value {
  color: #22c55e;
}

.stat-card.warning .stat-value {
  color: #fbbf24;
}

.stat-card.neutral .stat-value {
  color: #cbd5f5;
}
</style>

