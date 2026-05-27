const app = getApp();

class WebSocketService {
  constructor() {
    this.socketTask = null;
    this.isConnecting = false;
    this.reconnectTimer = null;
    this.heartbeatTimer = null;
    this.reconnectCount = 0;
    this.maxReconnectCount = 5;
    this.reconnectInterval = 3000;
    this.heartbeatInterval = 30000;
    this.messageHandlers = [];
    this.connectionHandlers = {
      onOpen: [],
      onClose: [],
      onError: []
    };
  }

  connect() {
    if (this.socketTask || this.isConnecting) {
      return;
    }

    const token = wx.getStorageSync('token');
    if (!token) {
      return;
    }

    this.isConnecting = true;
    const wsUrl = `${app.globalData.apiBaseUrl.replace('http', 'ws')}/ws/message?token=${token}`;

    this.socketTask = wx.connectSocket({
      url: wsUrl,
      success: () => {
        console.log('WebSocket connecting...');
      }
    });

    this.socketTask.onOpen(() => {
      console.log('WebSocket connected');
      this.isConnecting = false;
      this.reconnectCount = 0;
      this.startHeartbeat();
      this.triggerHandlers('onOpen');
    });

    this.socketTask.onMessage((res) => {
      this.handleMessage(res.data);
    });

    this.socketTask.onClose(() => {
      console.log('WebSocket closed');
      this.isConnecting = false;
      this.stopHeartbeat();
      this.triggerHandlers('onClose');
      this.tryReconnect();
    });

    this.socketTask.onError((err) => {
      console.error('WebSocket error:', err);
      this.isConnecting = false;
      this.stopHeartbeat();
      this.triggerHandlers('onError', err);
    });
  }

  disconnect() {
    this.stopHeartbeat();
    this.clearReconnectTimer();
    this.isConnecting = false;

    if (this.socketTask) {
      this.socketTask.close({
        success: () => {
          console.log('WebSocket disconnected');
        }
      });
      this.socketTask = null;
    }
  }

  handleMessage(data) {
    try {
      const message = JSON.parse(data);
      this.notifyHandlers(message);
      this.handlePushNotification(message);
    } catch (e) {
      console.error('Failed to parse WebSocket message:', e);
    }
  }

  handlePushNotification(message) {
    const notificationEnabled = wx.getStorageSync('notificationEnabled');
    if (!notificationEnabled) {
      return;
    }

    if (message.type === 'SYSTEM' || message.type === 'ORDER' || message.type === 'PAYMENT') {
      wx.showTabBarRedDot({
        index: 3
      });

      if (message.urgent) {
        wx.showToast({
          title: message.content || '新消息',
          icon: 'none',
          duration: 2000
        });
      }
    }
  }

  onMessage(handler) {
    if (typeof handler === 'function') {
      this.messageHandlers.push(handler);
    }
  }

  offMessage(handler) {
    const index = this.messageHandlers.indexOf(handler);
    if (index > -1) {
      this.messageHandlers.splice(index, 1);
    }
  }

  onConnection(event, handler) {
    if (this.connectionHandlers[event] && typeof handler === 'function') {
      this.connectionHandlers[event].push(handler);
    }
  }

  offConnection(event, handler) {
    if (this.connectionHandlers[event]) {
      const index = this.connectionHandlers[event].indexOf(handler);
      if (index > -1) {
        this.connectionHandlers[event].splice(index, 1);
      }
    }
  }

  notifyHandlers(message) {
    this.messageHandlers.forEach(handler => {
      try {
        handler(message);
      } catch (e) {
        console.error('Message handler error:', e);
      }
    });
  }

  triggerHandlers(event, data) {
    if (this.connectionHandlers[event]) {
      this.connectionHandlers[event].forEach(handler => {
        try {
          handler(data);
        } catch (e) {
          console.error('Connection handler error:', e);
        }
      });
    }
  }

  startHeartbeat() {
    this.stopHeartbeat();
    this.heartbeatTimer = setInterval(() => {
      if (this.socketTask) {
        this.socketTask.send({
          data: JSON.stringify({ type: 'PING' }),
          fail: (err) => {
            console.error('Heartbeat send failed:', err);
          }
        });
      }
    }, this.heartbeatInterval);
  }

  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  tryReconnect() {
    if (this.reconnectCount >= this.maxReconnectCount) {
      console.log('Max reconnect attempts reached');
      return;
    }

    this.clearReconnectTimer();
    this.reconnectTimer = setTimeout(() => {
      this.reconnectCount++;
      console.log(`Reconnecting... (${this.reconnectCount}/${this.maxReconnectCount})`);
      this.connect();
    }, this.reconnectInterval * this.reconnectCount);
  }

  clearReconnectTimer() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
  }

  send(data) {
    if (this.socketTask) {
      this.socketTask.send({
        data: typeof data === 'string' ? data : JSON.stringify(data),
        fail: (err) => {
          console.error('WebSocket send failed:', err);
        }
      });
    }
  }
}

module.exports = new WebSocketService();
