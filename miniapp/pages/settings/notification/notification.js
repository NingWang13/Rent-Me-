const NotificationManager = require('../../../utils/notification');

Page({
  data: {
    notificationEnabled: false,
    orderNotification: true,
    paymentNotification: true,
    systemNotification: true,
    wishNotification: true,
    inAppNotification: true,
    subscribeNotification: true
  },

  onLoad() {
    this.loadSettings();
  },

  loadSettings() {
    const notificationEnabled = wx.getStorageSync('notificationEnabled') || false;
    const orderNotification = wx.getStorageSync('orderNotification') !== false;
    const paymentNotification = wx.getStorageSync('paymentNotification') !== false;
    const systemNotification = wx.getStorageSync('systemNotification') !== false;
    const wishNotification = wx.getStorageSync('wishNotification') !== false;
    const inAppNotification = wx.getStorageSync('inAppNotification') !== false;
    const subscribeNotification = wx.getStorageSync('subscribeNotification') !== false;

    this.setData({
      notificationEnabled,
      orderNotification,
      paymentNotification,
      systemNotification,
      wishNotification,
      inAppNotification,
      subscribeNotification
    });
  },

  async toggleNotification(e) {
    const enabled = e.detail.value;
    this.setData({ notificationEnabled: enabled });
    wx.setStorageSync('notificationEnabled', enabled);

    if (enabled) {
      const granted = await NotificationManager.requestPermission();
      if (!granted) {
        this.setData({ notificationEnabled: false });
        wx.setStorageSync('notificationEnabled', false);
        wx.showToast({
          title: '权限获取失败',
          icon: 'none'
        });
      }
    }
  },

  toggleOrderNotification(e) {
    const enabled = e.detail.value;
    this.setData({ orderNotification: enabled });
    wx.setStorageSync('orderNotification', enabled);
  },

  togglePaymentNotification(e) {
    const enabled = e.detail.value;
    this.setData({ paymentNotification: enabled });
    wx.setStorageSync('paymentNotification', enabled);
  },

  toggleSystemNotification(e) {
    const enabled = e.detail.value;
    this.setData({ systemNotification: enabled });
    wx.setStorageSync('systemNotification', enabled);
  },

  toggleWishNotification(e) {
    const enabled = e.detail.value;
    this.setData({ wishNotification: enabled });
    wx.setStorageSync('wishNotification', enabled);
  },

  toggleInAppNotification(e) {
    const enabled = e.detail.value;
    this.setData({ inAppNotification: enabled });
    wx.setStorageSync('inAppNotification', enabled);
  },

  toggleSubscribeNotification(e) {
    const enabled = e.detail.value;
    this.setData({ subscribeNotification: enabled });
    wx.setStorageSync('subscribeNotification', enabled);
  },

  resetSettings() {
    wx.showModal({
      title: '提示',
      content: '确定要恢复默认设置吗？',
      success: (res) => {
        if (res.confirm) {
          wx.setStorageSync('notificationEnabled', false);
          wx.setStorageSync('orderNotification', true);
          wx.setStorageSync('paymentNotification', true);
          wx.setStorageSync('systemNotification', true);
          wx.setStorageSync('wishNotification', true);
          wx.setStorageSync('inAppNotification', true);
          wx.setStorageSync('subscribeNotification', true);
          this.loadSettings();
          wx.showToast({
            title: '已恢复默认',
            icon: 'success'
          });
        }
      }
    });
  }
});
