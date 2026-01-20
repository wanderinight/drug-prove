<template>
  <div class="monitor-center">
    <h2>监控报警中心</h2>
    <p class="hint">展示实时监控状态、历史告警记录，并支持时间范围/设备筛选。</p>

<!--    <DeviceStatusStrip title="监控网络概览" />-->

    <section class="section">
      <header class="section-header">
        <div>
          <h3>设备实时状态</h3>
          <p class="sub-hint">按设备展示监控状态、最新报警信息</p>
        </div>
        <button class="refresh-btn" @click="fetchDeviceCards" :disabled="loading">
          {{ loading ? '刷新中...' : '刷新' }}
        </button>
      </header>

      <div class="card-grid">
        <article class="device-card" v-for="device in deviceCards" :key="device.deviceId">
          <div class="device-header">
            <div>
              <h4>{{ device.deviceName || '-' }}</h4>
              <p class="code">编号：{{ device.deviceCode }}</p>
            </div>
            <span :class="['status', device.monitorStatus?.toLowerCase() || 'unknown']">
              {{ formatMonitorStatus(device.monitorStatus) }}
            </span>
          </div>
          <div class="device-body">
            <div class="body-row">
              <span>类型：{{ formatDeviceType(device.deviceType) }}</span>
              <span>位置：{{ device.location || '-' }}</span>
            </div>
            <div class="body-row">
              <span>运行状态：{{ formatOperationalStatus(device.operationalStatus) }}</span>
              <span>报警状态：{{ formatAlertStatus(device.alertStatus) }}</span>
            </div>
          </div>
          <div class="alert-panel">
            <div class="panel-title">最近报警</div>
            <div v-if="device.latestAlertTime" class="panel-content">
              <div class="panel-row">
                <span>时间：{{ formatDateTime(device.latestAlertTime) }}</span>
                <span>
                  等级：
                  <span :class="['badge', device.latestAlertLevel?.toLowerCase()]">
                    {{ formatAlertLevel(device.latestAlertLevel) }}
                  </span>
                </span>
              </div>
              <div class="panel-row">{{ device.latestAlertContext || '-' }}</div>
            </div>
            <div v-else class="panel-empty">暂无报警记录</div>
          </div>
        </article>
        <div v-if="!deviceCards.length" class="empty">暂无设备数据</div>
      </div>
    </section>

    <section class="section">
      <header class="section-header">
        <div>
          <h3>历史报警查询</h3>
          <p class="sub-hint">按时间范围查询历史警报记录，支持导出</p>
        </div>
        <div class="filter-row">
          <label>
            起始时间
            <input type="datetime-local" v-model="historyFilters.start" />
          </label>
          <label>
            结束时间
            <input type="datetime-local" v-model="historyFilters.end" />
          </label>
          <label>
            设备筛选
            <div class="select-wrapper">
              <select v-model="historyFilters.deviceCode">
              <option value="">全部设备</option>
              <option v-for="device in deviceOptions" :key="device.deviceCode" :value="device.deviceCode">
                {{ device.deviceName || device.deviceCode }} ({{ device.deviceCode }})
              </option>
            </select>
              <span class="select-arrow">▾</span>
            </div>
          </label>
          <button class="refresh-btn" @click="fetchHistoryAlerts" :disabled="historyLoading">
            {{ historyLoading ? '查询中...' : '查询' }}
          </button>
        </div>
      </header>

      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>设备名称</th>
              <th>设备类型</th>
              <th>位置</th>
              <th>监控状态</th>
              <th>报警时间</th>
              <th>报警内容</th>
              <th>报警等级</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in historyAlerts" :key="`history-${index}`">
              <td>{{ item.deviceName || '-' }}</td>
              <td>{{ formatDeviceType(item.deviceType) }}</td>
              <td>{{ item.location || '-' }}</td>
              <td>{{ formatMonitorStatus(item.monitorStatus) }}</td>
              <td>{{ formatDateTime(item.alertTime) }}</td>
              <td class="alert-cell">{{ item.context || '-' }}</td>
              <td>
                <span :class="['badge', item.alertLevel?.toLowerCase()]">
                  {{ formatAlertLevel(item.alertLevel) }}
                </span>
              </td>
            </tr>
            <tr v-if="!historyAlerts.length">
              <td colspan="7" class="empty">
                {{ historyLoading ? '查询中...' : '当前时间范围暂无记录' }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import axios from 'axios';

const apiBase = import.meta.env.VITE_API_BASE_URL || '';
const deviceCards = ref([]);
const deviceOptions = ref([]);
const historyAlerts = ref([]);
const historyLoading = ref(false);
const loading = ref(false);
const historyFilters = ref({
  start: defaultStart(),
  end: defaultEnd(),
  deviceCode: ''
});

function defaultEnd() {
  return new Date().toISOString().slice(0, 16);
}

function defaultStart() {
  const d = new Date();
  d.setDate(d.getDate() - 7);
  return d.toISOString().slice(0, 16);
}

const formatDateTime = (value) => {
  if (!value) return '-';
  return value.toString().replace('T', ' ');
};

const formatDeviceType = (type) => {
  const map = { D4: 'D4 检测仪', DB: 'DB 检测仪' };
  return map[type] || type || '-';
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
    OFFLINE: '待机/离线'
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

const formatAlertLevel = (level) => {
  const map = { YELLOW: '黄色', RED: '红色' };
  return map[level] || level || '-';
};

const fetchDeviceCards = async () => {
  loading.value = true;
  try {
    const { data } = await axios.get(`${apiBase}/api/alert/device-overview`);
    deviceCards.value = data?.data ?? [];
    deviceOptions.value = deviceCards.value.map(device => ({
      deviceCode: device.deviceCode,
      deviceName: device.deviceName
    }));
  } catch (error) {
    console.error('获取设备概览失败:', error);
    deviceCards.value = [];
    deviceOptions.value = [];
  } finally {
    loading.value = false;
  }
};

const fetchHistoryAlerts = async () => {
  historyLoading.value = true;
  try {
    const params = {
      page: 0,
      size: 50
    };
    if (historyFilters.value.start) {
      params.startTime = new Date(historyFilters.value.start).toISOString();
    }
    if (historyFilters.value.end) {
      params.endTime = new Date(historyFilters.value.end).toISOString();
    }
    if (historyFilters.value.deviceCode) {
      params.deviceCode = historyFilters.value.deviceCode;
    }

    const { data } = await axios.get(`${apiBase}/api/alert/details-by-time-range`, {
      params
    });
    historyAlerts.value = data?.data?.content ?? [];
  } catch (error) {
    console.error('查询历史报警失败:', error);
    historyAlerts.value = [];
  } finally {
    historyLoading.value = false;
  }
};

onMounted(() => {
  fetchDeviceCards();
  fetchHistoryAlerts();
});
</script>

<style scoped>
.monitor-center h2 {
  margin: 0 0 0.5rem;
}

.hint {
  color: #9ca3af;
  margin-bottom: 1rem;
}

.sub-hint {
  margin: 0.2rem 0 0;
  color: #94a3b8;
  font-size: 0.85rem;
}

.section {
  background: #ffffff;
  border-radius: 16px;
  padding: 1.2rem 1.3rem;
  border: 1px solid rgba(0, 0, 0, 0.06);
  margin-bottom: 1.5rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  margin-bottom: 0.8rem;
}

.section-header h3 {
  margin: 0;
}

.filter-row {
  display: flex;
  align-items: flex-end;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.filter-row label {
  display: flex;
  flex-direction: column;
  font-size: 0.85rem;
  color: #94a3b8;
}

.filter-row input {
  margin-top: 0.25rem;
  padding: 0.35rem 0.4rem;
  border-radius: 6px;
  border: 1px solid rgba(148, 163, 184, 0.4);
  background: transparent;
  color: #000000;
}

.select-wrapper {
  position: relative;
  margin-top: 0.25rem;
}

.select-wrapper select {
  appearance: none;
  width: 220px;
  padding: 0.4rem 1.8rem 0.4rem 0.5rem;
  border-radius: 6px;
  border: 1px solid rgba(148, 163, 184, 0.4);
  background: #ffffff;
  color: #000000;
  font-size: 0.9rem;
}

.select-wrapper select:focus {
  outline: none;
  border-color: rgba(59, 130, 246, 0.7);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.25);
}

.select-arrow {
  position: absolute;
  right: 0.6rem;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
  color: #94a3b8;
  font-size: 0.75rem;
}

.refresh-btn {
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
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1rem;
}

.device-card {
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(243, 244, 246, 0.8);
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
}

.device-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.6rem;
}

.device-header h4 {
  margin: 0;
}

.device-header .code {
  margin: 0;
  font-size: 0.85rem;
  color: #3a96a8;
}

.status {
  padding: 0.25rem 0.5rem;
  border-radius: 999px;
  font-size: 0.75rem;
  text-transform: uppercase;
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

.device-body {
  font-size: 0.85rem;
  color: #7db8c4;
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.body-row {
  display: flex;
  justify-content: space-between;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.alert-panel {
  border-top: 1px solid rgba(148, 163, 184, 0.15);
  padding-top: 0.8rem;
}

.panel-title {
  font-size: 0.85rem;
  color: #3a96a8;
  margin-bottom: 0.4rem;
}

.panel-content {
  font-size: 0.85rem;
  color: #7db8c4;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.panel-row {
  display: flex;
  justify-content: space-between;
  gap: 0.4rem;
  flex-wrap: wrap;
}

.panel-empty {
  font-size: 0.85rem;
  color: #64748b;
}

.table-wrapper {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
  color: #000000;
}

th,
td {
  padding: 0.55rem 0.5rem;
  text-align: left;
  border-bottom: 1px solid rgba(30, 64, 175, 0.4);
}

th {
  color: #9ca3af;
  font-weight: 500;
}

.alert-cell {
  max-width: 360px;
  word-break: break-word;
}

.badge {
  padding: 0.2rem 0.4rem;
  border-radius: 4px;
  font-size: 0.8rem;
  color: #fff;
}

.badge.yellow {
  background: #facc15;
  color: #1e1b4b;
}

.badge.red {
  background: #ef4444;
}

.empty {
  text-align: center;
  color: #6b7280;
  grid-column: 1 / -1;
}
</style>


