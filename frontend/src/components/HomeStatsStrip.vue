<template>
  <div class="home-stats-strip">
    <div class="info-row">
      <div class="info-left">
        <span class="info-item">设备总数：{{ stats.totalDevices ?? 0 }}</span>
        <span class="info-item">今日报警数量：{{ stats.todayAlerts ?? 0 }}</span>
        <span class="info-item">待校准设备数量：{{ stats.pendingCalibration ?? 0 }}</span>
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
  todayAlerts: 0,
  pendingCalibration: 0
});

const refreshStats = async () => {
  try {
    const now = new Date();
    const formattedNow = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(
      now.getDate()
    ).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(
      2,
      '0'
    )}:${String(now.getSeconds()).padStart(2, '0')}`;

    const requests = [
      axios.get(`${apiBase}/api/device/countall`).catch(() => ({ data: { data: 0 } })),
      axios
        .post(`${apiBase}/api/alert/count-today`, { currentTime: formattedNow })
        .catch(() => ({ data: { data: 0 } })),
      axios.get(`${apiBase}/api/device/waitcalibration-countnow`).catch(() => ({ data: { data: 0 } }))
    ];
    const [totalRes, todayAlertRes, pendingRes] = await Promise.all(requests);
    stats.value = {
      totalDevices: Number(totalRes.data?.data ?? 0),
      todayAlerts: Number(todayAlertRes.data?.data ?? 0),
      pendingCalibration: Number(pendingRes.data?.data ?? 0)
    };
  } catch (error) {
    console.error('获取首页统计信息失败:', error);
  }
};

onMounted(() => {
  refreshStats();
});

defineExpose({ refreshStats });
</script>

<style scoped>
.home-stats-strip {
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

