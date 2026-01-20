<template>
  <div class="home">
    <!-- 实时设备统计模块 -->
    <section class="section stats-section">
      <header class="section-header">
        <h2>实时设备统计</h2>
      </header>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-label">监控设备数量</div>
          <div class="stat-value">{{ deviceStats.monitoringDevices ?? 0 }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">实时报警数量</div>
          <div class="stat-value alert-value">{{ deviceStats.alertingDevices ?? 0 }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">实时工作数量</div>
          <div class="stat-value">{{ deviceStats.workingDevices ?? 0 }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">待机数量</div>
          <div class="stat-value">{{ deviceStats.standbyDevices ?? 0 }}</div>
        </div>
      </div>
    </section>

    <!-- 实时报警数据 -->
    <section class="section">
      <header class="section-header">
        <h2>实时报警数据</h2>
      </header>
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>时间</th>
              <th>设备编号</th>
              <th>设备位置</th>
              <th class="alert-content-col">报警内容</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(alert, index) in alerts" :key="index">
              <td>{{ formatDateTime(alert.alertTime) }}</td>
              <td>{{ alert.deviceCode }}</td>
              <td>{{ alert.location }}</td>
              <td class="alert-content-cell">{{ alert.context || '-' }}</td>
            </tr>
            <tr v-if="!alerts.length">
              <td colspan="4" class="empty">暂无报警记录</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- 本周报警情况汇总 -->
    <section class="section summary-section">
      <header class="section-header">
        <h2>本周报警情况汇总</h2>
      </header>
      <div class="summary-grid">
        <div class="summary-card yellow">
          <div class="summary-label">黄色报警</div>
          <div class="summary-value">{{ weekSummary.yellowAlert ?? 0 }}</div>
        </div>
        <div class="summary-card red">
          <div class="summary-label">红色报警</div>
          <div class="summary-value">{{ weekSummary.redAlert ?? 0 }}</div>
        </div>
        <div class="summary-card machine">
          <div class="summary-label">设备故障停机次数</div>
          <div class="summary-value">{{ weekSummary.machineFailure ?? 0 }}</div>
        </div>
      </div>
    </section>

    <section class="section">
      <header class="section-header">
        <h2>我关注的设备</h2>
        <p class="hint">每 5 分钟自动刷新一次，也可以手动刷新。</p>
      </header>
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>设备编号</th>
              <th>类别</th>
              <th>名称</th>
              <th>位置</th>
              <th>监控状态</th>
              <th>运行状态</th>
              <th>报警状态</th>
              <th>校准状态</th>
              <th>上次校准时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="d in followedDevices" :key="d.deviceId">
              <td>{{ d.deviceCode }}</td>
              <td>{{ formatDeviceType(d.deviceType) }}</td>
              <td>{{ d.deviceName }}</td>
              <td>{{ d.location }}</td>
              <td>{{ formatMonitorStatus(d.monitorStatus) }}</td>
              <td>{{ formatOperationalStatus(d.operationalStatus) }}</td>
              <td>{{ formatAlertStatus(d.alertStatus) }}</td>
              <td>{{ formatCalibrationStatus(d.calibrationStatus) }}</td>
              <td>{{ formatDateTime(d.lastCalibration) }}</td>
            </tr>
            <tr v-if="!followedDevices.length">
              <td colspan="9" class="empty">暂无关注设备</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue';
import axios from 'axios';

const apiBase = import.meta.env.VITE_API_BASE_URL || '';

const alerts = ref([]);
const followedDevices = ref([]);
const deviceStats = ref({
  monitoringDevices: 0,
  alertingDevices: 0,
  workingDevices: 0,
  standbyDevices: 0
});
const weekSummary = ref({
  yellowAlert: 0,
  redAlert: 0,
  machineFailure: 0
});
let timerId = null;

const formatDateTime = (value) => {
  if (!value) return '-';
  return value.toString().replace('T', ' ');
};

const formatDeviceType = (type) => {
  if (!type) return '-';
  const map = {
    D4: 'D4 检测仪',
    DB: 'DB 检测仪'
  };
  return map[type] || type;
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
    OFFLINE: '离线'
  };
  return map[status] || status || '-';
};

const formatAlertStatus = (status) => {
  const map = {
    ON: '报警中',
    OFF: '无报警'
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

const fetchAlerts = async () => {
  try {
    const { data } = await axios.get(`${apiBase}/api/alert/details`, {
      params: { page: 0, size: 10 }
    });
    alerts.value = data?.data?.content ?? data?.data ?? [];
  } catch (error) {
    console.error('获取报警数据失败:', error);
    alerts.value = [];
  }
};

const fetchFollowedDevices = async () => {
  const staffCode = localStorage.getItem('staffCode');
  if (!staffCode) return;

  try {
    const { data } = await axios.get(`${apiBase}/api/device/follow`, {
      params: { staffcode: staffCode }
    });
    followedDevices.value = data?.data ?? [];
  } catch (error) {
    console.error('获取关注设备失败:', error);
    followedDevices.value = [];
  }
};

// 获取实时设备统计（监控设备数量、实时报警数量、实时工作数量、待机数量）
const fetchDeviceStats = async () => {
  try {
    // 使用实时统计接口一次性获取所有数据
    const { data } = await axios.get(`${apiBase}/api/device/realtime-stats`).catch(() => ({ data: { data: {} } }));
    const stats = data?.data ?? {};
    deviceStats.value = {
      monitoringDevices: Number(stats.monitoringDevices ?? 0),
      alertingDevices: Number(stats.alertingDevices ?? 0),
      workingDevices: Number(stats.workingDevices ?? 0),
      standbyDevices: Number(stats.standbyDevices ?? 0)
    };
  } catch (error) {
    console.error('获取实时设备统计失败:', error);
  }
};

const fetchWeekSummary = async () => {
  try {
    const { data } = await axios.post(`${apiBase}/api/alert/week-summary`, {
      currentTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
    });
    weekSummary.value = data?.data ?? {
      yellowAlert: 0,
      redAlert: 0,
      machineFailure: 0
    };
  } catch (error) {
    console.error('获取本周报警汇总失败:', error);
  }
};

const refreshAll = async () => {
  await Promise.all([
    fetchAlerts(),
    fetchFollowedDevices(),
    fetchDeviceStats(),
    fetchWeekSummary()
  ]);
};

onMounted(() => {
  refreshAll();
  timerId = setInterval(refreshAll, 5 * 60 * 1000);
});

onUnmounted(() => {
  if (timerId) {
    clearInterval(timerId);
  }
});
</script>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.section {
  background: #ffffff;
  border-radius: 16px;
  padding: 1.2rem 1.3rem;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 0.8rem;
}

.hint {
  font-size: 0.85rem;
  color: #9ca3af;
}

.table-wrapper {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

th,
td {
  padding: 0.5rem 0.6rem;
  text-align: left;
  border-bottom: 1px solid rgba(30, 64, 175, 0.4);
}

th {
  color: #9ca3af;
  font-weight: 500;
}

.empty {
  text-align: center;
  color: #6b7280;
}

/* 报警内容列样式 - 预留更大空间 */
.alert-content-col {
  min-width: 300px;
  width: 40%;
}

.alert-content-cell {
  word-wrap: break-word;
  word-break: break-all;
  max-width: 500px;
}

/* 实时设备统计模块样式 */
.stats-section {
  margin-bottom: 1.5rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(200px, 1fr));
  gap: 1rem;
}

.stat-card {
  background: rgba(243, 244, 246, 0.8);
  border-radius: 12px;
  padding: 1.2rem;
  border: 1px solid rgba(59, 130, 246, 0.3);
  text-align: center;
}

.stat-label {
  font-size: 0.85rem;
  color: #3a96a8;
  margin-bottom: 0.5rem;
}

.stat-value {
  font-size: 2rem;
  font-weight: 600;
  color: #7db8c4;
}

.stat-value.alert-value {
  color: #f87171;
}

/* 本周报警情况汇总模块样式 */
.summary-section {
  margin-top: 1.5rem;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.summary-card {
  border-radius: 12px;
  padding: 1.2rem;
  text-align: center;
  border: 1px solid;
}

.summary-card.yellow {
  background: rgba(234, 179, 8, 0.15);
  border-color: rgba(234, 179, 8, 0.4);
}

.summary-card.red {
  background: rgba(239, 68, 68, 0.15);
  border-color: rgba(239, 68, 68, 0.4);
}

.summary-card.machine {
  background: rgba(107, 114, 128, 0.15);
  border-color: rgba(107, 114, 128, 0.4);
}

.summary-label {
  font-size: 0.9rem;
  color: #3a96a8;
  margin-bottom: 0.5rem;
}

.summary-value {
  font-size: 2rem;
  font-weight: 600;
}

.summary-card.yellow .summary-value {
  color: #fbbf24;
}

.summary-card.red .summary-value {
  color: #f87171;
}

.summary-card.machine .summary-value {
  color: #9ca3af;
}
</style>


