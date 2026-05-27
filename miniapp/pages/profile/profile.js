const app = getApp();

Page({
  data: {
    isLogin: false,
    userInfo: null,
    stats: {
      wishCount: 0,
      orderCount: 0,
      groupCount: 0,
      pendingWishCount: 0,
      pendingOrderCount: 0
    },
    loading: false
  },

  onLoad() {
    this.checkLoginStatus();
  },

  onShow() {
    this.checkLoginStatus();
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 2 });
    }
    if (this.data.isLogin) {
      this.loadUserStats();
    }
  },

  checkLoginStatus() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    this.setData({
      isLogin: !!token,
      userInfo: userInfo || null
    });
  },

  loadUserStats() {
    this.setData({ loading: true });

    wx.request({
      url: `${app.globalData.apiBaseUrl}/user/stats`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${wx.getStorageSync('token')}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            stats: res.data.data || {}
          });
        }
      },
      complete: () => {
        this.setData({ loading: false });
      }
    });
  },

  goToLogin() {
    wx.navigateTo({
      url: '/pages/auth/login'
    });
  },

  goToUserProfile() {
    wx.navigateTo({
      url: '/pages/user/profile'
    });
  },

  goToMyWishes() {
    wx.navigateTo({
      url: '/pages/user/my-wishes'
    });
  },

  goToMyOrders() {
    wx.switchTab({
      url: '/pages/order/list'
    });
  },

  goToCreditRecords() {
    wx.navigateTo({
      url: '/pages/user/credit-records'
    });
  },

  goToMyGroups() {
    wx.navigateTo({
      url: '/pages/my-groups/my-groups'
    });
  },

  goToAccessibility() {
    wx.navigateTo({
      url: '/pages/profile/accessibility/accessibility'
    });
  },

  goToFeedback() {
    wx.navigateTo({
      url: '/pages/profile/feedback/feedback'
    });
  },

  goToSettings() {
    wx.navigateTo({
      url: '/pages/settings/settings'
    });
  },

  logout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          // 清除本地存储
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          wx.removeStorageSync('refreshToken');

          // 重置全局数据
          app.globalData.token = null;
          app.globalData.userInfo = null;

          this.setData({
            isLogin: false,
            userInfo: null,
            stats: {
              wishCount: 0,
              orderCount: 0,
              groupCount: 0,
              pendingWishCount: 0,
              pendingOrderCount: 0
            }
          });

          wx.showToast({
            title: '已退出登录',
            icon: 'success'
          });
        }
      }
    });
  }
});
