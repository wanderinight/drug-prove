<template>
  <div class="login-card">
    <header>
      <p class="eyebrow">药品检测管理平台</p>
      <h1>欢迎登陆</h1>
      <p class="subtitle">请输入工号和密码，开始你的工作</p>
    </header>

    <form @submit.prevent="handleSubmit">
      <label>
        工号
        <input
          v-model.trim="form.staffcode"
          type="text"
          placeholder="请输入账号"
          :disabled="loading"
          required
        />
      </label>

      <label>
        密码
        <input
          v-model="form.password"
          type="password"
          placeholder="请输入密码"
          :disabled="loading"
          required
        />
      </label>

      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

      <button type="submit" :disabled="loading">
        {{ loading ? '登录中...' : '立即登录' }}
      </button>
    </form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const form = reactive({
  staffcode: '',
  password: ''
});

const loading = ref(false);
const errorMessage = ref('');
const apiBase = import.meta.env.VITE_API_BASE_URL || '';

const handleSubmit = async () => {
  if (loading.value) return;
  loading.value = true;
  errorMessage.value = '';

  try {
    const { data } = await axios.post(`${apiBase}/api/auth/login`, {
      staffcode: form.staffcode,
      password: form.password
    });

    // 后端 Result 一般结构：{ code, msg, data }
    if (data?.code && String(data.code) !== '200') {
      errorMessage.value = data.msg || data.message || '登录失败，请检查账号或密码';
      return;
    }

    const payload = data?.data;
    if (!payload) {
      errorMessage.value = '登录返回数据为空，请联系管理员检查后端接口。';
      return;
    }

    localStorage.setItem('accessToken', payload.accessToken ?? '');
    localStorage.setItem('refreshToken', payload.refreshToken ?? '');
    localStorage.setItem('staffName', payload.staff?.staffname ?? '');
    localStorage.setItem('staffCode', payload.staff?.staffcode ?? '');

    await router.push('/main/home');
  } catch (error) {
    errorMessage.value =
      error?.response?.data?.message ??
      error?.response?.data?.msg ??
      '登录失败，请检查账号或密码';
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-card {
  width: min(420px, 100%);
  padding: 3rem 2.75rem;
  border-radius: 24px;
  background: #3a96a8;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 10px 60px rgba(15, 23, 42, 0.6);
  color: #e2e8f0;
  backdrop-filter: blur(30px);
}

header {
  text-align: left;
  margin-bottom: 2rem;
}

.eyebrow {
  font-size: 0.95rem;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: #38bdf8;
  margin-bottom: 0.6rem;
}

h1 {
  margin: 0;
  font-size: 2rem;
  font-weight: 600;
  color: #f8fafc;
}

.subtitle {
  margin-top: 0.5rem;
  color: #94a3b8;
}

form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

label {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.95rem;
  color: #cbd5f5;
}

input {
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.4);
  padding: 0.9rem 1rem;
  font-size: 1rem;
  background: rgba(15, 23, 42, 0.5);
  color: #f8fafc;
  transition: border-color 0.2s, box-shadow 0.2s;
}

input:focus {
  outline: none;
  border-color: #38bdf8;
  box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.2);
}

button {
  margin-top: 0.5rem;
  border: none;
  border-radius: 16px;
  padding: 1rem;
  font-size: 1rem;
  font-weight: 600;
  background: linear-gradient(135deg, #38bdf8, #6366f1);
  color: #0f172a;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.2s;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

button:not(:disabled):hover {
  transform: translateY(-1px);
}

.error {
  margin: 0;
  color: #f87171;
  font-size: 0.9rem;
}
</style>

