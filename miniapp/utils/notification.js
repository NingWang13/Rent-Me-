class NotificationManager {
  constructor() {
    this.permissionStatus = null;
  }

  async init() {
    await this.checkPermission();
    this.setupMessageListener();
  }

  async checkPermission() {
    try {
      const setting = await wx.getSetting();
      const subSetting = setting.subscriptionsSetting;
      
      if (subSetting && subSetting.mainSwitch) {
        this.permissionStatus = 'granted';
      } else {
        this.permissionStatus = 'denied';
      }
    } catch (e) {
      this.permissionStatus = 'unknown';
    }
    
    return this.permissionStatus;
  }

  async requestPermission() {
    try {
      const res = await wx.requestSubscribeMessage({
        tmplIds: ['ORDER_STATUS_CHANGE', 'PAYMENT_SUCCESS', 'SYSTEM_NOTIFICATION']
      });

      if (res['ORDER_STATUS_CHANGE'] === 'accept' || 
          res['PAYMENT_SUCCESS'] === 'accept' || 
          res['SYSTEM_NOTIFICATION'] === 'accept') {
        this.permissionStatus = 'granted';
        wx.setStorageSync('notificationEnabled', true);
        return true;
      } else {
        this.permissionStatus = 'denied';
        wx.setStorageSync('notificationEnabled', false);
        return false;
      }
    } catch (e) {
      console.error('Request notification permission failed:', e);
      this.permissionStatus = 'denied';
      wx.setStorageSync('notificationEnabled', false);
      return false;
    }
  }

  setupMessageListener() {
    wx.onMessage((res) => {
      if (res && res.data) {
        try {
          const data = JSON.parse(res.data);
          if (data.type === 'subscribe_msg') {
            this.handleSubscriptionMessage(data);
          }
        } catch (e) {
          console.error('Parse message failed:', e);
        }
      }
    });
  }

  handleSubscriptionMessage(data) {
    if (data.action === 'permission_granted') {
      this.permissionStatus = 'granted';
      wx.setStorageSync('notificationEnabled', true);
    } else if (data.action === 'permission_denied') {
      this.permissionStatus = 'denied';
      wx.setStorageSync('notificationEnabled', false);
    }
  }

  getPermissionStatus() {
    return this.permissionStatus;
  }

  isNotificationEnabled() {
    return wx.getStorageSync('notificationEnabled') === true;
  }

  showPermissionGuide() {
    return new Promise((resolve) => {
      wx.showModal({
        title: '开启消息通知',
        content: '开启后可以及时收到订单状态变更、支付结果等重要通知',
        confirmText: '去开启',
        cancelText: '暂不开启',
        success: async (res) => {
          if (res.confirm) {
            const granted = await this.requestPermission();
            resolve(granted);
          } else {
            resolve(false);
          }
        }
      });
    });
  }

  async sendOrderStatusNotification(orderId, status) {
    if (!this.isNotificationEnabled()) {
      return;
    }

    try {
      await wx.requestSubscribeMessage({
        tmplIds: ['ORDER_STATUS_CHANGE']
      });
    } catch (e) {
      console.error('Send order notification failed:', e);
    }
  }

  async sendPaymentNotification(orderId, amount) {
    if (!this.isNotificationEnabled()) {
      return;
    }

    try {
      await wx.requestSubscribeMessage({
        tmplIds: ['PAYMENT_SUCCESS']
      });
    } catch (e) {
      console.error('Send payment notification failed:', e);
    }
  }

  async sendSystemNotification(title, content) {
    if (!this.isNotificationEnabled()) {
      return;
    }

    try {
      await wx.requestSubscribeMessage({
        tmplIds: ['SYSTEM_NOTIFICATION']
      });
    } catch (e) {
      console.error('Send system notification failed:', e);
    }
  }
}

module.exports = new NotificationManager();
