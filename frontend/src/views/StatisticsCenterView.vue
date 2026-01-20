<template>
  <div class="calibration-center">
    <h2>计量校准中心</h2>
    <p class="hint">查看设备校准状态、校准时间计划与执行情况。</p>
<!--    <DeviceStatusStrip title="监控网络概览" />-->

    <section class="section">
      <header class="section-header">
        <div>
          <h3>设备校准总览</h3>
          <p class="sub-hint">展示每台设备的校准状态与时间信息</p>
        </div>
        <button class="refresh-btn" :disabled="loading" @click="refreshAll">
          {{ loading ? '刷新中...' : '刷新' }}
        </button>
      </header>

      <div class="card-grid">
        <article class="device-card" v-for="device in devices" :key="device.deviceId">
          <div class="device-header">
            <div>
              <h4>{{ device.deviceName || '-' }}</h4>
              <p class="code">编号：{{ device.deviceCode }}</p>
              <p class="location">位置：{{ device.location || '未配置' }}</p>
            </div>
            <div class="status-block">
              <span :class="['calibration-status', device.calibrationStatus === 'PENDING' ? 'pending' : 'normal']">
                {{ formatCalibrationStatus(device.calibrationStatus) }}
              </span>
            </div>
          </div>

          <div class="stats-row">
            <div>
              <span class="label">运行状态</span>
              <span class="value">{{ formatOperationalStatus(device.operationalStatus) }}</span>
            </div>
            <div>
              <span class="label">校准状态</span>
              <span :class="['value', device.calibrationStatus === 'PENDING' ? 'warning' : '']">
                {{ formatCalibrationStatus(device.calibrationStatus) }}
              </span>
            </div>
          </div>

          <div class="calibration-block">
            <header>
              <h5>校准时间信息</h5>
            </header>
            <div class="calibration-grid">
              <div>
                <span class="label">自校准时间</span>
                <span class="value">{{ formatDateTime(device.selfCalibration) }}</span>
              </div>
              <div>
                <span class="label">手动校准时间</span>
                <span class="value">{{ formatDateTime(device.lastCalibration) }}</span>
              </div>
              <div>
                <span class="label">下次手动校准时间</span>
                <span :class="['value', isCalibrationDue(device.nextCalibration) ? 'due' : '']">
                  {{ formatDateTime(device.nextCalibration) }}
                </span>
              </div>
              <div v-if="device.calibrationOperator">
                <span class="label">校准操作员</span>
                <span class="value">{{ device.calibrationOperator }}</span>
              </div>
            </div>
          </div>

          <div class="actions">
            <button class="ghost-btn" @click="goReport(device)">获取报告</button>
          </div>
        </article>
        <div v-if="!devices.length" class="empty-card">暂无设备数据</div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const apiBase = import.meta.env.VITE_API_BASE_URL || '';
const devices = ref([]);
const loading = ref(false);
const router = useRouter();

const formatDateTime = (value) => {
  if (!value) return '-';
  return value.toString().replace('T', ' ').replace('Z', '');
};

const formatOperationalStatus = (status) => {
  const map = {
    RUNNING: '运行中',
    OFFLINE: '离线/待机'
  };
  return map[status] || status || '-';
};

const formatCalibrationStatus = (status) => {
  const map = {
    NORMAL: '已校准',
    PENDING: '待校准'
  };
  return map[status] || status || '-';
};

const isCalibrationDue = (nextCalibration) => {
  if (!nextCalibration) return false;
  const next = new Date(nextCalibration);
  const now = new Date();
  return next <= now;
};

const fetchDevices = async () => {
  const { data } = await axios.get(`${apiBase}/api/device/getall`);
  devices.value = data?.data ?? [];
};

const refreshAll = async () => {
  loading.value = true;
  try {
    await fetchDevices();
  } finally {
    loading.value = false;
  }
};

const goReport = (device) => {
  router.push({ name: 'report-center', query: { device: device.deviceCode } });
};

onMounted(() => {
  refreshAll();
});
</script>

<style scoped>
.calibration-center h2 {
  margin: 0 0 0.5rem;
}

.hint {
  color: #9ca3af;
  margin-bottom: 1rem;
}

.section {
  background: #ffffff;
  border-radius: 16px;
  padding: 1.2rem 1.3rem;
  border: 1px solid rgba(0, 0, 0, 0.06);
  margin-top: 1.5rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1rem;
}

.section-header h3 {
  margin: 0;
}

.sub-hint {
  margin: 0.2rem 0 0;
  color: #94a3b8;
  font-size: 0.85rem;
}

.refresh-btn,
.ghost-btn {
  border: 1px solid rgba(148, 163, 184, 0.5);
  border-radius: 999px;
  padding: 0.35rem 0.9rem;
  background: transparent;
  color: #cbd5f5;
  cursor: pointer;
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 1rem;
}

.device-card {
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: #ffffff;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
}

.device-header {
  display: flex;
  justify-content: space-between;
  gap: 0.5rem;
}

.device-header h4 {
  margin: 0;
}

.device-header .code,
.device-header .location {
  margin: 0;
  color: #94a3b8;
  font-size: 0.85rem;
}

.status-block {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.3rem;
}

.calibration-status {
  padding: 0.25rem 0.6rem;
  border-radius: 999px;
  font-size: 0.75rem;
  color: #fff;
}

.calibration-status.normal {
  background: #22c55e;
}

.calibration-status.pending {
  background: #fbbf24;
  color: #1f2937;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.6rem;
  font-size: 0.85rem;
}

.stats-row .label {
  display: block;
  color: #94a3b8;
  font-size: 0.78rem;
}

.stats-row .value {
  color: #f8fafc;
  font-weight: 600;
}

.stats-row .value.warning {
  color: #fbbf24;
}

.calibration-block {
  border-top: 1px solid rgba(148, 163, 184, 0.15);
  padding-top: 0.8rem;
}

.calibration-block header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  color: #94a3b8;
  font-size: 0.85rem;
  margin-bottom: 0.6rem;
}

.calibration-block h5 {
  margin: 0;
  font-size: 0.95rem;
  color: #e2e8f0;
}

.calibration-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.6rem;
  font-size: 0.85rem;
}

.calibration-grid .label {
  display: block;
  color: #94a3b8;
  font-size: 0.78rem;
}

.calibration-grid .value {
  color: #f8fafc;
  font-weight: 500;
}

.calibration-grid .value.due {
  color: #f87171;
  font-weight: 600;
}

.actions {
  display: flex;
  justify-content: flex-end;
}

.empty-card {
  text-align: center;
  color: #6b7280;
  grid-column: 1 / -1;
  padding: 2rem;
}
</style>


