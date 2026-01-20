<template>
  <section class="layout">
    <aside class="sidebar">
      <SideBar />
    </aside>
    <main class="content">
      <div class="fixed-panel">
        <UserInfoStrip
          ref="userInfoStrip"
          @refresh="handleUserInfoRefresh"
          @logout="handleLogout"
        />
        <!-- 根据路由 meta 动态显示不同的统计信息 -->
        <HomeStatsStrip
          v-if="currentStatsType === 'home'"
          ref="homeStatsStrip"
          class="inline-strip"
        />
        <MonitorStatsStrip
          v-if="currentStatsType === 'monitor'"
          ref="monitorStatsStrip"
          class="inline-strip"
        />
      </div>

      <section class="page-body">
        <router-view />
      </section>
    </main>
  </section>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import SideBar from '../components/SideBar.vue';
import UserInfoStrip from '../components/UserInfoStrip.vue';
import HomeStatsStrip from '../components/HomeStatsStrip.vue';
import MonitorStatsStrip from '../components/MonitorStatsStrip.vue';

const router = useRouter();
const route = useRoute();
const userInfoStrip = ref(null);
const homeStatsStrip = ref(null);
const monitorStatsStrip = ref(null);

// 根据当前路由的 meta 信息确定显示的统计类型
const currentStatsType = computed(() => {
  // 获取当前激活的子路由
  const matched = route.matched;
  const childRoute = matched[matched.length - 1];
  return childRoute?.meta?.statsType || 'none';
});

const handleUserInfoRefresh = () => {
  // 刷新用户信息和相关统计组件
  if (currentStatsType.value === 'home' && homeStatsStrip.value) {
    homeStatsStrip.value.refreshStats();
  } else if (currentStatsType.value === 'monitor' && monitorStatsStrip.value) {
    monitorStatsStrip.value.refreshStats();
  }
};

const handleLogout = () => {
  router.replace({ name: 'login' });
};
</script>

<style scoped>
.layout {
  height: 100vh;
  display: flex;
  background: transparent;
  color: var(--text-gray);
  overflow: hidden; /* 整体不随页面滚动，只让右侧内容滚动 */
}

.sidebar {
  width: 220px;
  background: transparent;
  border-right: 1px solid rgba(0, 0, 0, 0.06);
}

.content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto; /* 右侧内容区域内部滚动 */
}

.fixed-panel {
  position: sticky;
  top: 0;
  z-index: 10;
  background: #7db8c4;
  padding: 1rem 0.75rem;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  border-radius: 0;
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
  color: var(--text-gray);
}

.link {
  border: none;
  background: none;
  color: var(--primary-color);
  cursor: pointer;
  padding: 0;
}

.inline-strip {
  margin-bottom: 0.6rem;
}

.page-body {
  flex: 1;
  /* 去掉大卡片的样式，让内容直接显示 */
  padding: 1.5rem 2rem;
  background: transparent;
  border: none;
  box-shadow: none;
}
</style>

