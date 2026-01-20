<template>
  <div class="statistics-analysis">
    <h2>统计分析中心</h2>
    <p class="hint">支持多种统计方式、内容组合和时间筛选，提供可视化图表和多种格式导出。</p>
<!--    <DeviceStatusStrip title="监控网络概览" />-->

    <!-- 查询条件 -->
    <section class="section">
      <header class="section-header">
        <h3>统计条件设置</h3>
      </header>
      <div class="filter-form">
        <div class="form-row">
          <div class="form-group">
            <label>统计方式</label>
            <select v-model="filters.statisticsType" class="select-input">
              <option value="BY_DEVICE">按设备</option>
              <option value="BY_PRODUCT">按产品</option>
              <option value="BY_BATCH">按批次</option>
              <option value="BY_TIME">按时间</option>
              <option value="BY_INDICATOR">按指标</option>
            </select>
          </div>
          <div class="form-group">
            <label>聚合方式</label>
            <select v-model="filters.aggregationType" class="select-input">
              <option value="AVG">平均值</option>
              <option value="SUM">总和</option>
              <option value="MAX">最大值</option>
              <option value="MIN">最小值</option>
              <option value="COUNT">数量</option>
            </select>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>统计内容（可多选）</label>
            <div class="checkbox-group">
              <label class="checkbox-label">
                <input type="checkbox" v-model="filters.statisticsContent" value="WEIGHT" />
                重量
              </label>
              <label class="checkbox-label">
                <input type="checkbox" v-model="filters.statisticsContent" value="THICKNESS" />
                厚度
              </label>
              <label class="checkbox-label">
                <input type="checkbox" v-model="filters.statisticsContent" value="HARDNESS" />
                硬度
              </label>
              <label class="checkbox-label">
                <input type="checkbox" v-model="filters.statisticsContent" value="DIAMETER" />
                直径
              </label>
              <label class="checkbox-label">
                <input type="checkbox" v-model="filters.statisticsContent" value="FRIABILITY" />
                脆碎度
              </label>
            </div>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>时间范围</label>
            <div class="time-range">
              <input type="datetime-local" v-model="filters.startTime" class="datetime-input" />
              <span>至</span>
              <input type="datetime-local" v-model="filters.endTime" class="datetime-input" />
            </div>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>设备筛选（可选）</label>
            <select multiple v-model="filters.deviceCodes" class="select-input multiple">
              <option v-for="device in deviceOptions" :key="device.deviceCode" :value="device.deviceCode">
                {{ device.deviceName || device.deviceCode }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>产品筛选（可选）</label>
            <select multiple v-model="filters.productIds" class="select-input multiple">
              <option v-for="product in productOptions" :key="product.productId" :value="product.productId">
                {{ product.productName }}
              </option>
            </select>
          </div>
        </div>

        <div class="form-actions">
          <button class="primary-btn" @click="performAnalysis" :disabled="analyzing">
            {{ analyzing ? '分析中...' : '执行分析' }}
          </button>
          <button class="secondary-btn" @click="resetFilters">重置</button>
        </div>
      </div>
    </section>

    <!-- 统计结果 -->
    <section v-if="statisticsResult" class="section">
      <header class="section-header">
        <div>
          <h3>统计结果</h3>
          <p class="sub-hint">共 {{ statisticsResult.totalCount }} 条数据</p>
        </div>
        <div class="action-buttons">
          <button class="export-btn" @click="exportReport('PDF')" :disabled="exporting">
            {{ exporting ? '导出中...' : '导出PDF' }}
          </button>
          <button class="export-btn" @click="exportReport('WORD')" :disabled="exporting">
            {{ exporting ? '导出中...' : '导出Word' }}
          </button>
          <button class="export-btn" @click="exportReport('EXCEL')" :disabled="exporting">
            {{ exporting ? '导出中...' : '导出Excel' }}
          </button>
          <button class="export-btn" @click="previewReport" :disabled="exporting">
            预览
          </button>
          <button class="export-btn" @click="printReport" :disabled="exporting">
            打印
          </button>
        </div>
      </header>

      <!-- 图表展示 -->
      <div class="charts-container">
        <div v-for="(content, index) in filters.statisticsContent" :key="index" class="chart-wrapper">
          <h4>{{ getContentLabel(content) }}</h4>
          <v-chart :option="getChartOption(content)" style="height: 400px;" autoresize />
        </div>
      </div>

      <!-- 数据表格 -->
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>名称</th>
              <th>数量</th>
              <th v-for="content in filters.statisticsContent" :key="content">
                {{ getContentLabel(content) }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in statisticsResult.chartData" :key="index">
              <td>{{ item.name }}</td>
              <td>{{ item.count }}</td>
              <td v-for="content in filters.statisticsContent" :key="content">
                {{ formatNumber(item[content.toLowerCase()]) }}
              </td>
            </tr>
            <tr v-if="!statisticsResult.chartData || statisticsResult.chartData.length === 0">
              <td :colspan="2 + filters.statisticsContent.length" class="empty">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- 预览模态框 -->
    <div v-if="showPreview" class="preview-modal" @click.self="showPreview = false">
      <div class="preview-content">
        <div class="preview-header">
          <h3>报告预览</h3>
          <button class="close-btn" @click="showPreview = false">×</button>
        </div>
        <div class="preview-body">
          <iframe v-if="previewUrl" :src="previewUrl" class="preview-iframe"></iframe>
          <div v-else class="preview-placeholder">预览加载中...</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { BarChart, LineChart, PieChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components';
import VChart from 'vue-echarts';

use([
  CanvasRenderer,
  BarChart,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
]);

const apiBase = import.meta.env.VITE_API_BASE_URL || '';

const filters = ref({
  statisticsType: 'BY_DEVICE',
  aggregationType: 'AVG',
  statisticsContent: ['WEIGHT', 'THICKNESS'],
  startTime: '',
  endTime: '',
  deviceCodes: [],
  productIds: [],
  batchNos: []
});

const deviceOptions = ref([]);
const productOptions = ref([]);
const statisticsResult = ref(null);
const analyzing = ref(false);
const exporting = ref(false);
const showPreview = ref(false);
const previewUrl = ref('');

const getContentLabel = (content) => {
  const map = {
    WEIGHT: '重量(mg)',
    THICKNESS: '厚度(mm)',
    HARDNESS: '硬度(N)',
    DIAMETER: '直径(mm)',
    FRIABILITY: '脆碎度(%)'
  };
  return map[content] || content;
};

const formatNumber = (value) => {
  if (value === null || value === undefined) return '-';
  return Number(value).toFixed(2);
};

const getChartOption = (content) => {
  if (!statisticsResult.value || !statisticsResult.value.chartData) {
    return {};
  }

  const data = statisticsResult.value.chartData;
  const contentKey = content.toLowerCase();
  const values = data.map(item => item[contentKey] || 0);
  const names = data.map(item => item.name);

  return {
    title: {
      text: getContentLabel(content),
      left: 'center',
      textStyle: { color: '#e2e8f0' }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { color: '#94a3b8', rotate: 45 }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#94a3b8' }
    },
    series: [
      {
        name: getContentLabel(content),
        type: 'bar',
        data: values,
        itemStyle: {
          color: '#60a5fa'
        }
      }
    ],
    backgroundColor: 'transparent'
  };
};

const fetchDevices = async () => {
  try {
    const { data } = await axios.get(`${apiBase}/api/device/getall`);
    deviceOptions.value = data?.data ?? [];
  } catch (error) {
    console.error('获取设备列表失败:', error);
  }
};

const fetchProducts = async () => {
  try {
    // 假设有产品列表接口，如果没有可以从检测数据中提取
    const { data } = await axios.get(`${apiBase}/api/inspect-data/showrecent-all`);
    const products = new Map();
    (data?.data ?? []).forEach(item => {
      if (item.product) {
        products.set(item.product.productId, item.product);
      }
    });
    productOptions.value = Array.from(products.values());
  } catch (error) {
    console.error('获取产品列表失败:', error);
  }
};

const performAnalysis = async () => {
  if (filters.value.statisticsContent.length === 0) {
    alert('请至少选择一项统计内容');
    return;
  }

  analyzing.value = true;
  try {
    const request = {
      ...filters.value,
      startTime: filters.value.startTime ? new Date(filters.value.startTime).toISOString().slice(0, 19).replace('T', ' ') : null,
      endTime: filters.value.endTime ? new Date(filters.value.endTime).toISOString().slice(0, 19).replace('T', ' ') : null
    };

    const { data } = await axios.post(`${apiBase}/api/statistics/analyze`, request);
    statisticsResult.value = data?.data;
  } catch (error) {
    console.error('统计分析失败:', error);
    alert('统计分析失败: ' + (error.response?.data?.msg || error.message));
  } finally {
    analyzing.value = false;
  }
};

const exportReport = async (format) => {
  if (!statisticsResult.value) {
    alert('请先执行统计分析');
    return;
  }

  exporting.value = true;
  try {
    const request = {
      statisticsRequest: {
        ...filters.value,
        startTime: filters.value.startTime ? new Date(filters.value.startTime).toISOString().slice(0, 19).replace('T', ' ') : null,
        endTime: filters.value.endTime ? new Date(filters.value.endTime).toISOString().slice(0, 19).replace('T', ' ') : null
      },
      exportFormat: format,
      saveToServer: true
    };

    const { data } = await axios.post(`${apiBase}/api/statistics/export`, request, {
      responseType: 'blob'
    });

    // 创建下载链接
    const blob = new Blob([data], { type: getMimeType(format) });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `统计分析报告_${new Date().toISOString().slice(0, 10)}.${format.toLowerCase()}`;
    link.click();
    window.URL.revokeObjectURL(url);

    alert('导出成功');
  } catch (error) {
    console.error('导出失败:', error);
    alert('导出失败: ' + (error.response?.data?.msg || error.message));
  } finally {
    exporting.value = false;
  }
};

const getMimeType = (format) => {
  const map = {
    PDF: 'application/pdf',
    WORD: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    EXCEL: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  };
  return map[format] || 'application/octet-stream';
};

const previewReport = async () => {
  if (!statisticsResult.value) {
    alert('请先执行统计分析');
    return;
  }

  // 生成预览URL（这里可以生成PDF预览）
  showPreview.value = true;
  // TODO: 实现预览功能
  previewUrl.value = '';
};

const printReport = async () => {
  if (!statisticsResult.value) {
    alert('请先执行统计分析');
    return;
  }

  try {
    const request = {
      ...filters.value,
      startTime: filters.value.startTime ? new Date(filters.value.startTime).toISOString().slice(0, 19).replace('T', ' ') : null,
      endTime: filters.value.endTime ? new Date(filters.value.endTime).toISOString().slice(0, 19).replace('T', ' ') : null
    };

    await axios.post(`${apiBase}/api/statistics/print`, request);
    alert('打印请求已发送');
  } catch (error) {
    console.error('打印失败:', error);
    alert('打印失败: ' + (error.response?.data?.msg || error.message));
  }
};

const resetFilters = () => {
  filters.value = {
    statisticsType: 'BY_DEVICE',
    aggregationType: 'AVG',
    statisticsContent: ['WEIGHT', 'THICKNESS'],
    startTime: '',
    endTime: '',
    deviceCodes: [],
    productIds: [],
    batchNos: []
  };
  statisticsResult.value = null;
};

onMounted(() => {
  fetchDevices();
  fetchProducts();
});
</script>

<style scoped>
.statistics-analysis h2 {
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

.filter-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.9rem;
  color: #94a3b8;
}

.select-input {
  padding: 0.5rem;
  border-radius: 6px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: #ffffff;
  color: var(--text-gray);
}

.select-input.multiple {
  min-height: 100px;
}

.datetime-input {
  padding: 0.5rem;
  border-radius: 6px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: #ffffff;
  color: var(--text-gray);
}

.time-range {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #e2e8f0;
  cursor: pointer;
}

.form-actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.primary-btn,
.secondary-btn,
.export-btn {
  padding: 0.5rem 1rem;
  border-radius: 6px;
  border: 1px solid var(--primary-color);
  background: rgba(122, 184, 196, 0.1);
  color: var(--primary-color);
  cursor: pointer;
}

.primary-btn {
  background: #ffffff;
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.primary-btn:disabled {
  background: rgba(59, 130, 246, 0.2);
  border-color: rgba(59, 130, 246, 0.6);
  opacity: 0.8;
}

.secondary-btn {
  background: transparent;
}

.export-btn {
  padding: 0.4rem 0.8rem;
  font-size: 0.85rem;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.charts-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.chart-wrapper {
  background: #ffffff;
  border-radius: 12px;
  padding: 1rem;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.chart-wrapper h4 {
  margin: 0 0 0.5rem;
  color: #e2e8f0;
}

.table-wrapper {
  overflow-x: auto;
  margin-top: 1rem;
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

td {
  color: var(--text-gray);
}

.empty {
  text-align: center;
  color: #6b7280;
}

.preview-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.preview-content {
  background: rgba(15, 23, 42, 0.95);
  border-radius: 16px;
  width: 90%;
  max-width: 1200px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid rgba(148, 163, 184, 0.35);
}

.preview-header h3 {
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  color: #e2e8f0;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
}

.preview-body {
  flex: 1;
  overflow: auto;
  padding: 1rem;
}

.preview-iframe {
  width: 100%;
  height: 70vh;
  border: none;
}

.preview-placeholder {
  text-align: center;
  color: #94a3b8;
  padding: 2rem;
}
</style>

