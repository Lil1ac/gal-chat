<template>
  <div :class="['message', isUser ? 'user-message' : 'ai-message']">
    <!-- AI消息在左边 -->
    <div v-if="!isUser" class="message-content ai-content">
      <div class="avatar-container">
        <div class="avatar">
          <img
            src="../assets/misutsu.png"
            alt="本间心铃"
            class="avatar-image"
          />
        </div>
      </div>
      <div class="message-bubble ai-bubble">
        <div class="message-header">
          <span class="character-name">本间心铃</span>
        </div>
        <div class="message-text">
          <template v-for="(part, index) in parsedMessage" :key="index">
            <span v-if="part.type === 'text'">{{ part.content }}</span>
            <span v-else-if="part.type === 'thought'" class="thought-text">{{ part.content }}</span>
          </template>
          <LoadingDots v-if="showLoading" />
        </div>
      </div>
    </div>
    
    <!-- 用户消息在右边 -->
    <div v-else class="message-content user-content">
      <div class="message-bubble user-bubble">
        <div class="message-text">{{ message }}</div>
      </div>
      <div class="avatar-container">
        <div class="avatar user-avatar">
          <div class="avatar-placeholder">You</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import LoadingDots from './LoadingDots.vue';
import textUtils from '../utils/text.js';
const props = defineProps({
  message: {
    type: String,
    required: true
  },
  isUser: {
    type: Boolean,
    required: true
  },
  isSending: {
    type: Boolean,
    default: false
  }
});

// 简化的加载点显示逻辑
const showLoading = computed(() => {
  // 只有AI消息且正在发送时才显示加载点
  return !props.isUser && props.isSending && props.message !== undefined;
});

const parsedMessage = computed(() => textUtils.parseMessage(props.message));
</script>

<style scoped>
.message {
  display: flex;
  width: 100%;
}

.ai-message {
  justify-content: flex-start;
}

.user-message {
  justify-content: flex-end;
}

.message-content {
  display: flex;
  gap: 0.5rem;
  max-width: 85%;
}

.ai-content {
  flex-direction: row;
}

.user-content {
  flex-direction: row;
}

.avatar-container {
  display: flex;
  align-self: flex-start; /* 确保头像始终在顶部 */
}

.avatar {
  flex-shrink: 0;
}

.avatar-image {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--character-name-color);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-placeholder {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 50%;
  background-color: var(--user-message-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.75rem;
  font-weight: bold;
  border: 2px solid var(--user-message-bg);
}

.message-bubble {
  border-radius: 18px;
  padding: 0.75rem 1rem;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}


.user-bubble {
  background-color: var(--user-message-bg);
  color: white;
}
.thought-text {
  display: block;
  font-style: italic;
  color: #888;
  margin-top: 0.2em;
  margin-bottom: 0.5em;
  padding-left: 1em;
  border-left: 2px solid #ccc;
}

.message-header {
  margin-bottom: 0.25rem;
}

.character-name {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--character-name-color);
}

.message-text {
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
  display: inline-block;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message-content {
    max-width: 90%;
  }
  
  .avatar-image,
  .avatar-placeholder {
    width: 2rem;
    height: 2rem;
  }
}
</style>