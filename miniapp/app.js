const WebSocketService = require('./utils/websocket');
const NotificationManager = require('./utils/notification');

App({
  globalData: {
    userInfo: null,
    token: null,
    isElderMode: false,
    apiBaseUrl: 'https://your-api-domain.com/api' // 生产环境API地址
  },

  onLaunch() {
    // 根据环境切换API地址
    this.setApiBaseUrl();
    this.checkElderMode();
    this.checkLoginStatus();
    this.initServices();
  },

  onShow() {
    if (this.globalData.token) {
      WebSocketService.connect();
    }
  },

  onHide() {
    WebSocketService.disconnect();
  },

  setApiBaseUrl() {
    try {
      const accountInfo = wx.getAccountInfoSync();
      const envVersion = accountInfo.miniProgram.envVersion;
      
      let apiBaseUrl = 'https://your-api-domain.com/api'; // 生产环境
      
      if (envVersion === 'develop') {
        apiBaseUrl = 'http://localhost:8080/api'; // 开发环境
      } else if (envVersion === 'trial') {
        apiBaseUrl = 'https://test-api.yourdomain.com/api'; // 体验版
      }
      
      this.globalData.apiBaseUrl = apiBaseUrl;
      console.log('当前环境:', envVersion, 'API地址:', apiBaseUrl);
    } catch (error) {
      console.error('获取环境信息失败:', error);
    }
  },

  initServices() {
    NotificationManager.init();
    
    WebSocketService.onMessage((message) => {
      this.handleWebSocketMessage(message);
    });

    WebSocketService.onConnection('onOpen', () => {
      console.log('WebSocket connected successfully');
    });

    WebSocketService.onConnection('onError', (err) => {
      console.error('WebSocket connection error:', err);
    });
  },

  handleWebSocketMessage(message) {
    if (message.type === 'PING') {
      WebSocketService.send(JSON.stringify({ type: 'PONG' }));
      return;
    }

    this.updateUnreadBadge(message);
    this.showPushNotification(message);
  },

  updateUnreadBadge(message) {
    if (message.type === 'SYSTEM' || message.type === 'ORDER' || message.type === 'PAYMENT') {
      wx.showTabBarRedDot({
        index: 3
      });
    }
  },

  showPushNotification(message) {
    const notificationEnabled = wx.getStorageSync('notificationEnabled');
    if (!notificationEnabled) {
      return;
    }

    if (message.urgent || message.type === 'ORDER') {
      wx.showToast({
        title: message.content || '新消息',
        icon: 'none',
        duration: 2000
      });
    }
  },

  checkElderMode() {
    const elderMode = wx.getStorageSync('elderMode');
    if (elderMode) {
      this.globalData.isElderMode = true;
    }
  },

  checkLoginStatus() {
    const token = wx.getStorageSync('token');
    if (token) {
      this.globalData.token = token;
    }
  },

  setElderMode(enabled) {
    this.globalData.isElderMode = enabled;
    wx.setStorageSync('elderMode', enabled);
  },

  getUserInfo() {
    return this.globalData.userInfo;
  },

  setUserInfo(userInfo) {
    this.globalData.userInfo = userInfo;
    wx.setStorageSync('userInfo', userInfo);
  },

  login(token, userInfo) {
    this.globalData.token = token;
    this.globalData.userInfo = userInfo;
    wx.setStorageSync('token', token);
    wx.setStorageSync('userInfo', userInfo);
    WebSocketService.connect();
  },

  logout() {
    this.globalData.token = null;
    this.globalData.userInfo = null;
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
    WebSocketService.disconnect();
  }
});
