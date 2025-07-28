<template>
    <div class="chat-room">
        <div class="chat-header">
            <h2>樱之刻 - 本间心铃</h2>
        </div>
        <div class="chat-messages" ref="chatContainer">
            <div v-if="messages.length === 0" class="welcome-msg">
                本间心铃是 枕社 旗下游戏《樱之刻》及其衍生作品的登场角色~
            </div>
            <div v-for="msg in messages" :key="msg.id" class="message-wrapper">
                <ChatMessage :message="msg.content" :isUser="msg.isUser" />
            </div>
            <div v-if="isAiTyping" class="message-wrapper">
                <ChatMessage :message="currentAiResponse" :isUser="false" :isSending="true" />
            </div>
        </div>
        <div class="chat-input-area">
            <ChatInput @send="handleSend" :disabled="isAiTyping" />
        </div>
        <transition name="fade">
            <div v-if="connectionError" class="connection-error">
                <span>⚠️ 连接服务器失败，请检查后端服务是否启动</span>
            </div>
        </transition>
    </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onBeforeUnmount } from "vue";
import ChatMessage from "../components/ChatMessage.vue";
import ChatInput from "../components/ChatInput.vue";
import { chatWithSSE } from "../api/chatApi.js";

const messages = ref([]);
const chatContainer = ref(null);
const isAiTyping = ref(false);
const currentAiResponse = ref("");
const connectionError = ref(false);
let currentEventSource = null;
const memoryId = ref(null);
const isUserScrolling = ref(false);

function generateMemoryId() {
    return Math.floor(Date.now() / 1000);
}
function scrollToBottom() {
    nextTick(() => {
        if (chatContainer.value && !isUserScrolling.value) {
            chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
        }
    });
}
function cleanupEventSource() {
    if (currentEventSource) {
        currentEventSource.close();
        currentEventSource = null;
    }
}
function handleSend(content) {
    if (isAiTyping.value) return;
    messages.value.push({ id: Date.now() + Math.random(), content, isUser: true });
    scrollToBottom();

    isAiTyping.value = true;
    currentAiResponse.value = "";
    connectionError.value = false;

    cleanupEventSource();
    currentEventSource = chatWithSSE(
        memoryId.value,
        content,
        (chunk) => {
            currentAiResponse.value += chunk;
            scrollToBottom();
        },
        (error) => {
            console.error("SSE 错误", error);
            connectionError.value = true;
            finishAiResponse();
            setTimeout(() => (connectionError.value = false), 5000);
        },
        finishAiResponse
    );
}
function finishAiResponse() {
    if (currentAiResponse.value.trim()) {
        messages.value.push({
            id: Date.now() + Math.random(),
            content: currentAiResponse.value.trim(),
            isUser: false,
        });
    }
    isAiTyping.value = false;
    currentAiResponse.value = "";
    connectionError.value = false;
    cleanupEventSource();
}
onMounted(() => {
    memoryId.value = generateMemoryId();
    chatContainer.value.addEventListener("scroll", () => {
        const container = chatContainer.value;
        const threshold = 30;
        isUserScrolling.value =
            container.scrollTop + container.clientHeight < container.scrollHeight - threshold;
    });
});
onBeforeUnmount(cleanupEventSource);
</script>

<style scoped>
.chat-room {
    display: flex;
    flex-direction: column;
    height: 100vh;
    width: 100%;
    max-width: 800px;
    margin: 0 auto;
    background: linear-gradient(135deg, #f8e1f4 0%, #ffe6f8 45%, #ffd0ec 100%);
    font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
    box-shadow:
        inset 0 0 100px 30px rgba(255, 182, 193, 0.15),
        0 10px 40px rgba(255, 105, 180, 0.2);
    border-radius: 16px;
    overflow: hidden;
    user-select: text;
}

.chat-header {
    backdrop-filter: blur(18px);
    background: rgba(255, 105, 180, 0.8);
    color: #fff;
    padding: 1.0rem 1rem;
    text-align: center;
    font-weight: 700;
    font-size: 1.6rem;
    letter-spacing: 1.2px;
    text-shadow: 0 0 8px rgba(255, 182, 193, 0.9);
    box-shadow: 0 6px 12px rgba(255, 105, 180, 0.4);
    border-bottom: 1px solid rgba(255, 255, 255, 0.3);
    transition: background 0.3s ease;
}

.chat-header:hover {
    background: rgba(255, 105, 180, 0.95);
    box-shadow: 0 8px 20px rgba(255, 105, 180, 0.6);
}

.chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 1.6rem 1.4rem;
    background: rgba(255, 255, 255, 0.45);
    backdrop-filter: blur(22px);
    box-shadow: inset 0 0 30px 10px rgba(255, 182, 193, 0.12);
    scroll-behavior: smooth;
    scrollbar-width: thin;
    scrollbar-color: #ff8fb0 #ffd0ec;
}

.chat-messages::-webkit-scrollbar {
    width: 10px;
}

.chat-messages::-webkit-scrollbar-track {
    background: #ffe4f1;
    border-radius: 10px;
}

.chat-messages::-webkit-scrollbar-thumb {
    background: #ff7ba6;
    border-radius: 10px;
    box-shadow: inset 0 0 5px rgba(255, 105, 180, 0.5);
}

.welcome-msg {
    text-align: center;
    padding: 3rem 1rem;
    color: #b96b8b;
    font-size: 1.05rem;
    font-weight: 600;
    font-style: italic;
    user-select: none;
    animation: fadeInUp 0.8s ease forwards;
}

.message-wrapper {
    margin-bottom: 1.2rem;
    filter: drop-shadow(0 0 2px rgba(255, 105, 180, 0.3));
    animation: fadeInUp 0.5s ease forwards;
    overflow: hidden;
    border-radius: 16px;
}

.chat-input-area {
    padding: 0.5rem 1.0rem;
    background: rgba(255, 255, 255, 0.85);
    border-top: 1px solid rgba(255, 105, 180, 0.25);
    backdrop-filter: blur(16px);
    border-radius: 0 0 16px 16px;
    box-shadow: inset 0 0 10px rgba(255, 182, 193, 0.2);
    transition: background-color 0.3s ease;
}

.chat-input-area:hover {
    background-color: rgba(255, 255, 255, 0.95);
}

.connection-error {
    width: 100%;
    text-align: center;
    padding: 0.6rem 0;
    color: #ff3b3b;
    font-weight: 600;
    font-size: 0.95rem;
    background: linear-gradient(90deg, rgba(255, 59, 59, 0.15), rgba(255, 10, 10, 0.05));
    border-top: 1.5px solid rgba(255, 59, 59, 0.3);
    text-shadow: 0 0 6px rgba(255, 59, 59, 0.7);
    user-select: none;
}

.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.6s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(12px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}
</style>
