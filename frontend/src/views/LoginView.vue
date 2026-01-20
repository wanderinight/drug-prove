<template>
  <section class="app-shell">
    <LoginCard />
  </section>
</template>

<script setup>
import LoginCard from '../components/LoginCard.vue';
</script>

<style scoped>
.app-shell {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: rgb(255, 255, 255);
}
</style>

