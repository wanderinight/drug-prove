<template>
  <div class="user-info-strip">
    <div class="info-row">
      <div class="info-left">
        <span class="info-item">登录者：{{ staffInfo.name }}</span>
        <span class="info-item">部门：{{ staffInfo.department || '-' }}</span>
        <span class="info-item">权限：{{ staffInfo.roleName || '-' }}</span>
      </div>
      <div class="info-right">
        <span class="info-item">日期：{{ today }}</span>
        <button class="link" @click="handleRefresh" :disabled="loading">
          {{ loading ? '刷新中...' : '刷新总览' }}
        </button>
        <button class="link" @click="handleLogout">退出登录</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const props = defineProps({
  onRefresh: {
    type: Function,
    default: null
  }
});

const emit = defineEmits(['refresh', 'logout']);

const router = useRouter();
const apiBase = import.meta.env.VITE_API_BASE_URL || '';
const loading = ref(false);

const staffInfo = reactive({
  name: localStorage.getItem('staffName') || '未登录',
  department: '',
  roleName: ''
});

const today = computed(() => {
  const date = new Date();
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' });
});

const fetchStaffInfo = async () => {
  const staffCode = localStorage.getItem('staffCode');
  if (!staffCode) return;
  try {
    const { data } = await axios.get(`${apiBase}/api/auth/staff-role-dept`, {
      params: { staffcode: staffCode }
    });
    const payload = data?.data;
    if (payload) {
      staffInfo.name = payload.staffname || staffInfo.name;
      staffInfo.department = payload.department || '';
      staffInfo.roleName = payload.role?.roleName || '';
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
  }
};

const handleRefresh = async () => {
  if (loading.value) return;
  loading.value = true;
  try {
    await fetchStaffInfo();
    if (props.onRefresh) {
      props.onRefresh();
    }
    emit('refresh');
  } finally {
    loading.value = false;
  }
};

const handleLogout = () => {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('staffName');
  localStorage.removeItem('staffCode');
  emit('logout');
  router.replace({ name: 'login' });
};

onMounted(() => {
  fetchStaffInfo();
});

defineExpose({ refresh: handleRefresh });
</script>

<style scoped>
.user-info-strip {
  margin-bottom: 0.6rem;
}

.info-row {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  font-size: 0.95rem;
}

.info-left,
.info-right {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.info-item {
  color: #e2e8f0;
}

.link {
  border: none;
  background: none;
  color: #38bdf8;
  cursor: pointer;
  padding: 0;
  transition: color 0.2s;
}

.link:hover:not(:disabled) {
  color: #0ea5e9;
}

.link:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>

