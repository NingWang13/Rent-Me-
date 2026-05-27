const app = getApp();

Page({
  data: {
    largeFont: false,
    highContrast: false,
    simplifyMode: false,
    reduceMotion: false,
    fontSize: 28
  },

  onLoad() {
    this.loadSettings();
  },

  onShow() {
    this.loadSettings();
  },

  loadSettings() {
    const settings = wx.getStorageSync('accessibilitySettings') || {};
    this.setData({
      largeFont: settings.largeFont || false,
      highContrast: settings.highContrast || false,
      simplifyMode: settings.simplifyMode || false,
      reduceMotion: settings.reduceMotion || false,
      fontSize: settings.largeFont ? 36 : 28
    });
  },

  toggleLargeFont() {
    const newValue = !this.data.largeFont;
    this.setData({
      largeFont: newValue,
      fontSize: newValue ? 36 : 28
    });
    this.saveSettings();
  },

  toggleHighContrast() {
    const newValue = !this.data.highContrast;
    this.setData({ highContrast: newValue });
    this.saveSettings();
  },

  toggleSimplifyMode() {
    const newValue = !this.data.simplifyMode;
    this.setData({ simplifyMode: newValue });
    this.saveSettings();
  },

  toggleReduceMotion() {
    const newValue = !this.data.reduceMotion;
    this.setData({ reduceMotion: newValue });
    this.saveSettings();
  },

  saveSettings() {
    const settings = {
      largeFont: this.data.largeFont,
      highContrast: this.data.highContrast,
      simplifyMode: this.data.simplifyMode,
      reduceMotion: this.data.reduceMotion
    };
    wx.setStorageSync('accessibilitySettings', settings);

    app.globalData.accessibilitySettings = settings;
  },

  resetSettings() {
    wx.showModal({
      title: '恢复默认设置',
      content: '确定要恢复所有设置为默认吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('accessibilitySettings');
          this.setData({
            largeFont: false,
            highContrast: false,
            simplifyMode: false,
            reduceMotion: false,
            fontSize: 28
          });
          app.globalData.accessibilitySettings = {};
        }
      }
    });
  }
});
