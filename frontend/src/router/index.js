import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import MainView from '../views/MainView.vue';
import HomeView from '../views/HomeView.vue';
import DeviceCenterView from '../views/DeviceCenterView.vue';
import MonitorCenterView from '../views/MonitorCenterView.vue';
import StatisticsCenterView from '../views/StatisticsCenterView.vue';
import ReportCenterView from '../views/ReportCenterView.vue';
import StatisticsAnalysisView from '../views/StatisticsAnalysisView.vue';
import UserCenterView from '../views/UserCenterView.vue';

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView
  },
  {
    path: '/main',
    name: 'main',
    component: MainView,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/main/home' },
      { 
        path: 'home', 
        name: 'home', 
        component: HomeView,
        meta: { statsType: 'home' } // 首页：设备总数、今日报警数量、待校准设备数量
      },
      { 
        path: 'device', 
        name: 'device-center', 
        component: DeviceCenterView,
        meta: { statsType: 'monitor' } // 设备管理：设备总数、监控中数量、监控中断数量、未监控数量
      },
      { 
        path: 'monitor', 
        name: 'monitor-center', 
        component: MonitorCenterView,
        meta: { statsType: 'monitor' } // 监控报警：设备总数、监控中数量、监控中断数量、未监控数量
      },
      { 
        path: 'statistics', 
        name: 'statistics-center', 
        component: StatisticsCenterView,
        meta: { statsType: 'monitor' } // 计量校准：设备总数、监控中数量、监控中断数量、未监控数量
      },
      { 
        path: 'report', 
        name: 'report-center', 
        component: ReportCenterView,
        meta: { statsType: 'monitor' } // 测量报告：设备总数、监控中数量、监控中断数量、未监控数量
      },
      { 
        path: 'analysis', 
        name: 'statistics-analysis', 
        component: StatisticsAnalysisView,
        meta: { statsType: 'none' } // 统计分析中心：只显示登录者信息
      },
      { 
        path: 'user', 
        name: 'user-center', 
        component: UserCenterView,
        meta: { statsType: 'none' } // 用户中心：只显示登录者信息
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  // 只对需要鉴权的路由做校验（/main 及其子路由）
  if (!to.matched.some(r => r.meta?.requiresAuth)) {
    return next();
  }

  const token = localStorage.getItem('accessToken');
  if (!token) {
    return next({ name: 'login' });
  }
  return next();
});

export default router;

