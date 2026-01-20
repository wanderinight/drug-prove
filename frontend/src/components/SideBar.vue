<template>
  <nav class="nav">
    <div class="logo">
      <span class="dot" />
      <span>药检平台</span>
    </div>

    <ul>
      <li>
        <RouterLink to="/main/home" class="item" active-class="active">
          首页
        </RouterLink>
      </li>
      <li>
        <RouterLink to="/main/device" class="item" active-class="active">
          设备管理中心
        </RouterLink>
      </li>
      <li>
        <RouterLink to="/main/monitor" class="item" active-class="active">
          监控报警中心
        </RouterLink>
      </li>
      <li>
        <RouterLink to="/main/statistics" class="item" active-class="active">
          计量校准中心
        </RouterLink>
      </li>
      <li>
        <RouterLink to="/main/report" class="item" active-class="active">
          测量报告中心
        </RouterLink>
      </li>
      <li>
        <RouterLink to="/main/analysis" class="item" active-class="active">
          统计分析中心
        </RouterLink>
      </li>
      <li>
        <RouterLink to="/main/user" class="item" active-class="active">
          用户中心
        </RouterLink>
      </li>
    </ul>
  </nav>
</template>

<script setup>
import { RouterLink } from 'vue-router';
</script>

<style scoped>
.nav {
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 1.5rem 1rem;
  background-color: var(--sidebar-bg);
  color: var(--sidebar-text);
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: radial-gradient(circle, var(--accent-color), var(--primary-color));
}

ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.item {
  display: block;
  padding: 0.6rem 0.8rem;
  border-radius: 0.75rem;
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.75);
  text-decoration: none;
}

.item:hover {
  background: rgba(255, 255, 255, 0.16);
  color: #ffffff;
}

.active {
  background: #7db8c4;
  color: var(--sidebar-text);
}
</style>


