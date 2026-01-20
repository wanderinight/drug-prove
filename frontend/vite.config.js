import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
   server: {
     // 前端开发服务器端口（浏览器访问这个）
     port: 8082,
     proxy: {
       // 所有以 /api 开头的请求转发到 Spring Boot
       '/api': {
         // 这里必须和 application.properties 里的 server.port 一致
         target: 'http://localhost:9091',
         changeOrigin: true
       }
     }
   }
});

