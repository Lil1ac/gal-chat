<template>
  <div id="app">
    <HomeView />
  </div>
</template>

<script setup>
import HomeView from './views/HomeView.vue'
</script>

<style>
:root {
  --primary-color: #ff69b4;
  --secondary-color: #ffb6c1;
  --text-primary: #333;
  --text-secondary: #666;
  --background-light: #fff5f5;
  --background-chat: #ffffff;
  --border-color: #e0e0e0;
  --user-message-bg: #4a90e2;
  --ai-message-bg: #f5f5f5;
  --character-name-color: #ff69b4;
  --player-name-color: #4a90e2;
}

html, body {
  margin: 0;
  padding: 0;
}

#app {
  height: 100vh;
  margin: 0;
  padding: 0;
  background-color: var(--background-light);
}
</style>