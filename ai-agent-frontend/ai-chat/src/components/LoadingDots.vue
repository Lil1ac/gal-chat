<template>
  <div class="loading-dots">
    <div class="dot"></div>
    <div class="dot"></div>
    <div class="dot"></div>
  </div>
</template>

<script setup>
// 这是一个简单的加载点组件，用于表示正在流式输出
</script>

<style scoped>
.loading-dots {
  display: inline-flex;
  align-items: center;
  height: 1.5rem;
  margin-left: 4px;
}

.dot {
  width: 8px;
  height: 8px;
  background-color: var(--character-name-color);
  border-radius: 50%;
  margin: 0 2px;
  animation: pulse 1.5s infinite ease-in-out;
}

.dot:nth-child(1) {
  animation-delay: -0.32s;
}

.dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes pulse {
  0%, 80%, 100% {
    transform: scale(0.25);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>