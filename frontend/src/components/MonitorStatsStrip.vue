<template>
  <div class="monitor-stats-strip">
    <div class="info-row">
      <div class="info-left">
        <span class="info-item">设备总数：{{ stats.totalDevices ?? 0 }}</span>
        <span class="info-item">监控中数量：{{ stats.monitoringCount ?? 0 }}</span>
        <span class="info-item">监控中断数量：{{ stats.monitorInterruptCount ?? 0 }}</span>
        <span class="info-item">未监控数量：{{ stats.monitorOffCount ?? 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import axios from 'axios';

const apiBase = import.meta.env.VITE_API_BASE_URL || '';
const stats = ref({
  totalDevices: 0,
  monitoringCount: 0,
  monitorInterruptCount: 0,
  monitorOffCount: 0
});

const refreshStats = async () => {
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
    console.error('获取监控统计信息失败:', error);
  }
};

onMounted(() => {
  refreshStats();
});

defineExpose({ refreshStats });
</script>

<style scoped>
.monitor-stats-strip {
  margin-bottom: 0.6rem;
}

.info-row {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  font-size: 0.95rem;
}

.info-left {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.info-item {
  color: #e2e8f0;
}
</style>

