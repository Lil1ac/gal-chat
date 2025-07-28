import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api';

/**
 * 使用 SSE 方式调用聊天接口
 * @param {number} memoryId 聊天室ID
 * @param {string} message 用户消息
 * @param {Function} onMessage 接收消息的回调函数
 * @param {Function} onError 错误处理回调函数
 * @param {Function} onClose 连接关闭回调函数
 * @returns {EventSource} 返回 EventSource 对象，用于手动关闭连接
 */
export function chatWithSSE(memoryId, message, onMessage, onError, onClose) {
    const params = new URLSearchParams({
        memoryId: memoryId,
        message: message
    });

    const eventSource = new EventSource(`${API_BASE_URL}/ai/chat?${params}`);

    eventSource.onmessage = (event) => {
        const data = event.data?.trim();
        if (data) {
            onMessage?.(data);
        }
    };

    eventSource.onerror = (error) => {
        console.log('SSE 连接状态:', eventSource.readyState, '错误事件:', error);

        if (eventSource.readyState === EventSource.CLOSED) {
            console.log('SSE 连接正常结束');
            onClose?.();
        } else if (eventSource.readyState === EventSource.CONNECTING) {
            // 浏览器自动重连中，不当错误处理
            onClose?.();
        } else {
            console.error('SSE 连接错误:', error);
            onError?.(error);
            onClose?.();
        }

        if (eventSource.readyState !== EventSource.CLOSED) {
            eventSource.close();
        }
    };



    eventSource.onclose = () => {
        console.log('SSE 连接已关闭');
        onClose?.();
    };

    return eventSource;
}

export async function checkServiceHealth() {
    try {
        const response = await axios.get(`${API_BASE_URL}/health`, { timeout: 5000 });
        return response.status === 200;
    } catch (error) {
        console.error('服务健康检查失败:', error);
        return false;
    }
}
