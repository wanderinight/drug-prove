<template>
  <div class="report-center">
    <h2>测量报告中心</h2>
    <p class="hint">按照设备 / 批次 / 产品 / 时间组合筛选，生成 PDF 并支持预览、保存、打印。</p>
<!--    <DeviceStatusStrip title="监控网络概览" />-->

    <section class="section">
      <header class="section-header">
        <div>
          <h3>组合查询条件</h3>
          <p class="sub-hint">设备支持多选，批次与产品可输入多个值（逗号或换行分隔）</p>
        </div>
        <div class="header-actions">
          <button class="ghost-btn" :disabled="syncing" @click="syncReports">
            {{ syncing ? '同步中...' : '同步报告' }}
          </button>
          <button class="ghost-btn" @click="resetFilters">重置条件</button>
        </div>
      </header>

      <div class="layout">
        <div class="form-panel">
          <label>
            报告标题
            <input v-model="filters.reportTitle" type="text" placeholder="示例：XX车间-设备组合报告" />
          </label>

          <label>
            设备（多选）
            <div class="select-wrapper">
              <select multiple v-model="filters.deviceCodes">
                <option
                  v-for="device in deviceOptions"
                  :key="device.deviceCode"
                  :value="device.deviceCode"
                >
                  {{ device.deviceName || device.deviceCode }} ({{ device.deviceCode }})
                </option>
              </select>
            </div>
          </label>

          <label>
            批次（多个值用逗号、空格或换行分隔）
            <textarea 
              v-model="filters.batchNos" 
              rows="2" 
              placeholder="如：BATCH001, BATCH002 或 20260105 搜索PDF报告"
            ></textarea>
            <div class="batch-hint">
              <small>输入批次号进行精确匹配</small>
            </div>
          </label>

          <label>
            产品（多个值用逗号、空格或换行分隔）
            <textarea v-model="filters.productIds" rows="2" placeholder="如：P001, P002"></textarea>
          </label>

          <div class="grid-two">
            <label>
              起始时间
              <input type="datetime-local" v-model="filters.startTime" />
            </label>
            <label>
              结束时间
              <input type="datetime-local" v-model="filters.endTime" />
            </label>
          </div>

          <label class="checkbox-row">
            <input type="checkbox" v-model="filters.saveFile" />
            <span>生成时保存到服务器（路径：{{ storagePathHint }}）</span>
          </label>

          <div class="actions">
            <button class="primary" :disabled="loading" @click="previewReport">
              {{ loading && actionType === 'preview' ? '预览中...' : '预览 PDF' }}
            </button>
            <button class="secondary" :disabled="loading" @click="generateReport">
              {{ loading && actionType === 'save' ? '生成中...' : '生成并保存' }}
            </button>
            <button class="ghost" :disabled="loading" @click="printReport">
              打印（预留）
            </button>
            <button class="secondary" :disabled="loading" @click="searchPdfReports">
              检索历史报告
            </button>
          </div>
          <div class="search-hint" style="margin-top: 0.5rem; color: #64748b; font-size: 0.8rem;">
            * “检索历史报告”将根据“起始时间”和“结束时间”查找已生成的PDF
          </div>

          <p v-if="message" class="status">{{ message }}</p>
          <ul class="summary" v-if="fileName || savedPath || recordCount">
            <li v-if="fileName">文件名：{{ fileName }}</li>
            <li v-if="savedPath">保存路径：{{ savedPath }}</li>
            <li v-if="recordCount">数据条目：{{ recordCount }}</li>
          </ul>
        </div>

        <div class="preview-panel">
          <div class="preview-header">
            <h4>PDF 预览 / 搜索结果</h4>
            <span v-if="criteriaDescription" class="criteria">{{ criteriaDescription }}</span>
          </div>
          
          <!-- PDF预览 -->
          <div v-if="previewSrc" class="preview-body">
            <iframe :src="previewSrc" title="report-preview"></iframe>
          </div>
          
          <!-- PDF搜索结果 -->
          <div v-else-if="searchResults.length > 0" class="search-results">
            <h5>找到 {{ searchResults.length }} 个PDF报告：</h5>
            <div class="pdf-list">
              <div 
                v-for="report in searchResults" 
                :key="report.fileName"
                class="pdf-item"
                @click="viewPdf(report)"
              >
                <div class="pdf-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                    <polyline points="14 2 14 8 20 8" />
                  </svg>
                </div>
                <div class="pdf-info">
                  <div class="pdf-name">{{ report.fileName }}</div>
                  <div class="pdf-meta">
                    <span class="meta-item">日期: {{ formatDate(report.date) }}</span>
                    <span class="meta-item">时间: {{ formatTime(report.time) }}</span>
                    <span class="meta-item">大小: {{ report.fileSizeFormatted }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div v-else class="placeholder">
            生成或搜索后可在此查看 PDF 或 报告列表
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';

const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9091';
const storagePathHint = './resouce/report';

// 响应式数据
const filters = ref({
  reportTitle: '组合条件报告',
  deviceCodes: [],
  batchNos: '',
  productIds: '',
  startTime: '',
  endTime: '',
  saveFile: true
});

const deviceOptions = ref([]);
const previewSrc = ref('');
const recordCount = ref(0);
const savedPath = ref('');
const fileName = ref('');
const criteriaDescription = ref('');
const message = ref('');
const loading = ref(false);
const actionType = ref('');
const searchResults = ref([]);
const searching = ref(false);
const syncing = ref(false);

// 将原本的 isBatchSearch 逻辑移除或简化，因为现在明确拆分了功能

// 将多行 / 多分隔符输入解析为数组
const parseListInput = (value) => {
  if (!value) return [];
  return value
    .split(/[\s,，\n]+/)
    .map((item) => item.trim())
    .filter((item) => item.length > 0);
};

// 日期与时间格式化（用于右侧列表展示）
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  // 后端已提供 yyyyMMdd，如 20260105
  if (/^\d{8}$/.test(dateStr)) {
    return `${dateStr.slice(0, 4)}-${dateStr.slice(4, 6)}-${dateStr.slice(6, 8)}`;
  }
  return dateStr;
};

const formatTime = (timeStr) => {
  if (!timeStr) return '';
  // 后端已提供 HHmmss，如 103552
  if (/^\d{6}$/.test(timeStr)) {
    return `${timeStr.slice(0, 2)}:${timeStr.slice(2, 4)}:${timeStr.slice(4, 6)}`;
  }
  return timeStr;
};

// 按日期范围检索PDF报告（使用 startTime 和 endTime）
const searchPdfReports = async () => {
  const start = filters.value.startTime;
  const end = filters.value.endTime;

  if (!start) {
    message.value = '请至少选择“起始时间”以检索历史报告';
    return;
  }

  // 格式化为 YYYYMMDD
  const formatToYMD = (isoStr) => {
    if (!isoStr) return '';
    const d = new Date(isoStr);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}${m}${day}`;
  };

  const startDateStr = formatToYMD(start);
  const endDateStr = end ? formatToYMD(end) : startDateStr; // 如果没选结束时间，默认查单日

  const dateRange = `${startDateStr}-${endDateStr}`;

  searching.value = true;
  loading.value = true;
  previewSrc.value = ''; // 切换为列表展示
  actionType.value = 'search';

  try {
    const response = await fetch(
      `${apiBase}/api/report/search?dateRange=${encodeURIComponent(dateRange)}`
    );
    const data = await response.json();

    if (data?.code === '200') {
      searchResults.value = data.data || [];
      if (!searchResults.value.length) {
        message.value = `未找到 ${startDateStr} 至 ${endDateStr} 的PDF报告`;
      } else {
        message.value = `找到 ${searchResults.value.length} 个PDF报告`;
      }
    } else {
      searchResults.value = [];
      message.value = data?.msg || '检索失败';
    }
  } catch (error) {
    console.error('检索PDF报告失败', error);
    searchResults.value = [];
    message.value = '检索失败，请稍后重试';
  } finally {
    searching.value = false;
    loading.value = false;
  }
};

const syncReports = async () => {
  syncing.value = true;
  message.value = '正在从服务器同步PDF报告...';
  try {
    const response = await fetch(`${apiBase}/pdf/sync`, { method: 'POST' });
    const data = await response.json();
    if (data?.code === '200') {
       // data.data 可能是同步的数量
       const count = data.data !== null ? data.data : 0;
       message.value = `同步完成，新增 ${count} 个报告`;
       // 如果当前有搜索结果，可能需要刷新，这里暂时不自动刷新
    } else {
       message.value = data?.msg || '同步失败';
    }
  } catch (error) {
    console.error('同步失败', error);
    message.value = '同步请求失败';
  } finally {
    syncing.value = false;
  }
};

// 点击右侧列表中的某个PDF，在右侧 iframe 中预览
const viewPdf = (report) => {
  if (!report || !report.fileName) return;
  previewSrc.value = `${apiBase}${report.previewUrl || `/report/${report.fileName}`}`;
};

const buildPayload = (saveFile) => {
  return {
    reportTitle: filters.value.reportTitle,
    deviceCodes: filters.value.deviceCodes,
    batchNos: parseListInput(filters.value.batchNos),
    productIds: parseListInput(filters.value.productIds),
    startTime: filters.value.startTime ? new Date(filters.value.startTime).toISOString() : null,
    endTime: filters.value.endTime ? new Date(filters.value.endTime).toISOString() : null,
    saveFile: saveFile && filters.value.saveFile
  };
};

const handleResponse = (result, showSavedPath = false) => {
  if (!result) return;
  if (result.previewBase64) {
    previewSrc.value = `data:application/pdf;base64,${result.previewBase64}`;
  }
  fileName.value = result.fileName || '';
  recordCount.value = result.recordCount || 0;
  criteriaDescription.value = result.criteria || '';
  savedPath.value = showSavedPath ? result.savedPath || '' : '';
};

const previewReport = async () => {
  // 按组合条件生成临时PDF（原有逻辑）
  actionType.value = 'preview';
  await submitGenerate(false);
};

const generateReport = async () => {
  actionType.value = 'save';
  await submitGenerate(true);
};

const submitGenerate = async (saveFile) => {
  message.value = '';
  loading.value = true;
  try {
    const payload = buildPayload(saveFile);
    const response = await fetch(`${apiBase}/api/inspect-data/report/generate`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload)
    });
    
    const data = await response.json();
    
    if (data?.code === '200') {
      handleResponse(data.data, saveFile && filters.value.saveFile);
      message.value = saveFile && filters.value.saveFile ? '生成并保存成功' : '生成成功，可预览';
    } else {
      message.value = data?.msg || '生成失败';
    }
  } catch (error) {
    console.error(error);
    message.value = '生成失败，请稍后重试';
  } finally {
    loading.value = false;
  }
};

const resetFilters = () => {
  filters.value.reportTitle = '组合条件报告';
  filters.value.deviceCodes = [];
  filters.value.batchNos = '';
  filters.value.productIds = '';
  filters.value.startTime = '';
  filters.value.endTime = '';
  filters.value.saveFile = true;
  message.value = '';
  previewSrc.value = '';
  searchResults.value = [];
};

const printReport = () => {
  // 打印功能预留
  alert('打印功能预留');
};

const fetchDevices = async () => {
  try {
    const response = await fetch(`${apiBase}/api/device/getall`);
    const data = await response.json();
    deviceOptions.value = data?.data || [];
  } catch (error) {
    console.error('获取设备列表失败', error);
    deviceOptions.value = [];
  }
};

onMounted(() => {
  fetchDevices();
});
</script>

<style scoped>
.report-center {
  padding: 1rem;
  color: var(--text-gray);
}

h2 {
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
  margin-bottom: 1rem;
  gap: 1rem;
  flex-wrap: wrap;
}

.section-header h3 {
  margin: 0;
}

.sub-hint {
  margin: 0.2rem 0 0;
  color: #94a3b8;
  font-size: 0.85rem;
}

.layout {
  display: grid;
  grid-template-columns: minmax(320px, 420px) 1fr;
  gap: 1.5rem;
}

@media (max-width: 1024px) {
  .layout {
    grid-template-columns: 1fr;
  }
}

.form-panel {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}

.form-panel label {
  display: flex;
  flex-direction: column;
  font-size: 0.85rem;
  color: #94a3b8;
}

.form-panel input,
.form-panel textarea,
.form-panel select {
  margin-top: 0.35rem;
  padding: 0.45rem 0.55rem;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: #ffffff;
  color: var(--text-gray);
  font-size: 0.9rem;
}

.form-panel textarea {
  resize: vertical;
}

.select-wrapper select {
  min-height: 110px;
}

.batch-hint {
  margin-top: 0.25rem;
  color: #64748b;
  font-size: 0.75rem;
}

.batch-hint small {
  color: #94a3b8;
}

.grid-two {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.75rem;
}

.checkbox-row {
  flex-direction: row;
  align-items: center;
  gap: 0.4rem;
}

.checkbox-row input {
  margin: 0;
}

.actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  margin-top: 1rem;
}

button {
  border: none;
  border-radius: 8px;
  padding: 0.45rem 1rem;
  cursor: pointer;
  font-size: 0.9rem;
  transition: opacity 0.2s;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.primary {
  background: var(--primary-color);
  color: #ffffff;
}

.secondary {
  background: rgba(122, 184, 196, 0.12);
  color: var(--primary-color);
  border: 1px solid var(--accent-color);
}

.ghost {
  background: transparent;
  color: var(--primary-color);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.ghost-btn {
  background: transparent;
  border: 1px solid rgba(148, 163, 184, 0.4);
  color: #cbd5f5;
  padding: 0.3rem 0.9rem;
  border-radius: 999px;
  cursor: pointer;
  white-space: nowrap;
}

.header-actions {
  display: flex;
  gap: 0.5rem;
}

.status {
  color: #facc15;
  font-size: 0.85rem;
  margin: 0.5rem 0 0;
  padding: 0.5rem;
  background: rgba(250, 204, 21, 0.1);
  border-radius: 4px;
}

.summary {
  list-style: none;
  padding: 0;
  margin: 0.5rem 0 0;
  font-size: 0.85rem;
  color: #9ca3af;
}

.summary li {
  padding: 0.25rem 0;
}

.preview-panel {
  border-radius: 14px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  min-height: 500px;
}

.preview-header {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.preview-header h4 {
  margin: 0;
}

.criteria {
  color: #94a3b8;
  font-size: 0.8rem;
}

.preview-body {
  flex: 1;
  background: rgba(2, 6, 23, 0.7);
  border-radius: 12px;
  border: 1px dashed rgba(94, 234, 212, 0.3);
  overflow: hidden;
}

.preview-body iframe {
  width: 100%;
  height: 100%;
  border: none;
  background: #fff;
}

.search-results {
  flex: 1;
  overflow-y: auto;
}

.search-results h5 {
  margin: 0 0 1rem;
  color: var(--text-gray);
  font-size: 0.9rem;
}

.pdf-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.pdf-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.pdf-item:hover {
  background: rgba(30, 41, 59, 0.7);
  border-color: rgba(148, 163, 184, 0.3);
}

.pdf-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  color: #ef4444;
}

.pdf-info {
  flex: 1;
  min-width: 0;
}

.pdf-name {
  font-size: 0.85rem;
  color: var(--text-gray);
  margin-bottom: 0.25rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pdf-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.75rem;
  color: #94a3b8;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  font-size: 0.9rem;
  text-align: center;
  padding: 2rem;
  background: rgba(2, 6, 23, 0.7);
  border-radius: 12px;
  border: 1px dashed rgba(94, 234, 212, 0.3);
}
</style>
