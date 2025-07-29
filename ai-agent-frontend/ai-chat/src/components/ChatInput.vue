<template>
    <div class="chat-input-container">
        <div class="input-wrapper">
            <textarea ref="textareaRef" v-model="input" @input="adjustHeight" @keyup.enter.exact.prevent="handleEnter"
                placeholder="输入消息..." class="chat-input" :disabled="disabled"></textarea>
            <button @click="handleSend" class="send-button" :disabled="disabled">
                <svg v-if="!disabled" width="24" height="24" viewBox="0 0 24 24" fill="none"
                    xmlns="http://www.w3.org/2000/svg">
                    <path d="M22 2L11 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                        stroke-linejoin="round" />
                    <path d="M22 2L15 22L11 13L2 9L22 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                        stroke-linejoin="round" />
                </svg>
                <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" />
                    <path d="M8 12H16" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                </svg>
            </button>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from "vue";

const emit = defineEmits(["send"]);
const input = ref("");
const props = defineProps({
    disabled: {
        type: Boolean,
        default: false
    }
});
let lastSentTime = 0;

const textareaRef = ref(null);


const adjustHeight = () => {
    const el = textareaRef.value;
    if (!el) return;
    el.style.height = "auto";
    const newHeight = Math.min(el.scrollHeight, 150);
    el.style.height = newHeight + "px";
};

const handleEnter = (event) => {
    send();
};

const handleSend = () => {
    const now = Date.now();
    if (now - lastSentTime < 500) return;
    lastSentTime = now;
    send();
};

const send = () => {
    if (!input.value.trim() || props.disabled) return;
    emit("send", input.value.trim());
    input.value = "";
    // 重置高度
    adjustHeight();
};

onMounted(() => {
    adjustHeight();
});
</script>

<style scoped>
.chat-input-container {
    padding: 0.5rem 0.5rem;
}

.input-wrapper {
    display: flex;
    gap: 0.75rem;
    align-items: flex-end;
    background-color: white;
    border: 1px solid var(--border-color);
    border-radius: 8px;
    padding: 0.5rem;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    transition: box-shadow 0.2s ease;
}

.input-wrapper:focus-within {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    border-color: var(--primary-color);
}

.chat-input {
    flex: 1;
    border: none;
    padding: 0.5rem 0.6rem;
    font-size: 1rem;
    outline: none;
    background: transparent;
    resize: none;
    line-height: 1.4;
    min-height: 20px;
    max-height: 150px;
    overflow-y: auto;
    transition: height 0.15s ease;
}

.chat-input::placeholder {
    color: #aaa;
}

.chat-input:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.send-button {
    border: none;
    background-color: var(--primary-color);
    color: white;
    cursor: pointer;
    width: 32px;
    height: 32px;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background-color 0.2s ease;
    flex-shrink: 0;
}

.send-button:hover:not(:disabled) {
    background-color: var(--secondary-color);
}

.send-button:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .chat-input-container {
        padding: 0.75rem 0.75rem 1rem;
    }

    .input-wrapper {
        padding: 0.25rem;
    }

    .chat-input {
        padding: 0.6rem 0.4rem;
        font-size: 0.9rem;
    }

    .send-button {
        width: 30px;
        height: 30px;
    }
}
</style>