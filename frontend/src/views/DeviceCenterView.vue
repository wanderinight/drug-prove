<template>
  <div class="device-center">
    <h2>设备管理中心</h2>
    <p class="hint">查看设备状态、最新检测结果，并可跳转获取测量报告。</p>
<!--    <DeviceStatusStrip title="监控网络概览" />-->

    <section class="section">
      <header class="section-header">
        <div>
          <h3>设备总览</h3>
          <p class="sub-hint">展示每台设备的运行与检测情况</p>
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
              <span :class="['status', device.monitorStatus?.toLowerCase() || 'unknown']">
                {{ formatMonitorStatus(device.monitorStatus) }}
              </span>
              <span class="status-text">运行：{{ formatOperationalStatus(device.operationalStatus) }}</span>
            </div>
          </div>

          <div class="stats-row">
            <div>
              <span class="label">报警状态</span>
              <span :class="['value', device.alertStatus === 'ON' ? 'danger' : '']">
                {{ formatAlertStatus(device.alertStatus) }}
              </span>
            </div>
            <div>
              <span class="label">监控状态</span>
              <span class="value">{{ formatMonitorStatus(device.monitorStatus) }}</span>
            </div>
            <div>
              <span class="label">上次校准</span>
              <span class="value">{{ formatDateTime(device.lastCalibration) }}</span>
            </div>
          </div>

          <div class="inspection-block">
            <header>
              <h5>最近检测结果</h5>
              <span>{{ formatDateTime(latestInspection(device.deviceCode)?.inspectTime) }}</span>
            </header>
            <div v-if="latestInspection(device.deviceCode)" class="inspection-grid">
              <div>
                <span class="label">产品</span>
                <span class="value">
                  {{ latestInspection(device.deviceCode)?.product?.productName || '-' }}
                </span>
              </div>
              <div>
                <span class="label">批次</span>
                <span class="value">{{ latestInspection(device.deviceCode)?.batchNo || '-' }}</span>
              </div>
              <div>
                <span class="label">重量(mg)</span>
                <span class="value">{{ formatNumber(latestInspection(device.deviceCode)?.weight) }}</span>
              </div>
              <div>
                <span class="label">厚度(mm)</span>
                <span class="value">{{ formatNumber(latestInspection(device.deviceCode)?.thickness) }}</span>
              </div>
              <div>
                <span class="label">硬度(N)</span>
                <span class="value">{{ formatNumber(latestInspection(device.deviceCode)?.hardness) }}</span>
              </div>
              <div>
                <span class="label">直径(mm)</span>
                <span class="value">{{ formatNumber(latestInspection(device.deviceCode)?.diameter) }}</span>
              </div>
            </div>
            <div v-else class="empty">暂无检测记录</div>
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
const inspections = ref({});
const loading = ref(false);
const router = useRouter();

const formatDateTime = (value) => {
  if (!value) return '-';
  return value.toString().replace('T', ' ').replace('Z', '');
};

const formatNumber = (value) => {
  if (value === null || value === undefined) return '-';
  return Number(value).toFixed(2);
};

const formatMonitorStatus = (status) => {
  const map = {
    MONITORON: '监控中',
    MONITORINTERRUPT: '监控中断',
    MONITOROFF: '未监控'
  };
  return map[status] || status || '-';
};

const formatOperationalStatus = (status) => {
  const map = {
    RUNNING: '运行中',
    OFFLINE: '离线/待机'
  };
  return map[status] || status || '-';
};

const formatAlertStatus = (status) => {
  const map = {
    ON: '报警中',
    OFF: '正常'
  };
  return map[status] || status || '-';
};

const latestInspection = (deviceCode) => inspections.value[deviceCode];

const fetchDevices = async () => {
  const { data } = await axios.get(`${apiBase}/api/device/getall`);
  devices.value = data?.data ?? [];
};

const fetchInspections = async () => {
  const { data } = await axios.get(`${apiBase}/api/inspect-data/showrecent-all`);
  const map = {};
  (data?.data ?? []).forEach(item => {
    const code = item?.device?.deviceCode;
    if (code) {
      map[code] = item;
    }
  });
  inspections.value = map;
};

const refreshAll = async () => {
  loading.value = true;
  try {
    await Promise.all([fetchDevices(), fetchInspections()]);
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
.device-center h2 {
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

.status {
  padding: 0.25rem 0.6rem;
  border-radius: 999px;
  font-size: 0.75rem;
  color: #fff;
}

.status.monitoron {
  background: #22c55e;
}
.status.monitorinterrupt {
  background: #f97316;
}
.status.monitoroff,
.status.unknown {
  background: #6b7280;
}

.status-text {
  font-size: 0.8rem;
  color: #cbd5f5;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
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

.stats-row .value.danger {
  color: #f87171;
}

.inspection-block {
  border-top: 1px solid rgba(148, 163, 184, 0.15);
  padding-top: 0.8rem;
}

.inspection-block header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  color: #94a3b8;
  font-size: 0.85rem;
}

.inspection-block h5 {
  margin: 0;
  font-size: 0.95rem;
  color: #e2e8f0;
}

.inspection-grid {
  margin-top: 0.6rem;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.6rem;
  font-size: 0.85rem;
}

.inspection-grid .label {
  display: block;
  color: #94a3b8;
  font-size: 0.78rem;
}

.inspection-grid .value {
  color: #f8fafc;
  font-weight: 500;
}

.actions {
  display: flex;
  justify-content: flex-end;
}

.empty,
.empty-card {
  text-align: center;
  color: #6b7280;
}
</style>
