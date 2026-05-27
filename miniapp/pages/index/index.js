const app = getApp();

Page({
  data: {
    userInfo: null,
    isLogin: false,
    userType: 1,
    credits: 0,
    elderMode: false,
    loading: false,
    banners: [
      { id: 1, image: 'https://via.placeholder.com/750x300/1890ff/ffffff?text=Banner+1', url: '' },
      { id: 2, image: 'https://via.placeholder.com/750x300/52c41a/ffffff?text=Banner+2', url: '' }
    ],
    quickActions: [
      { id: 1, name: '发布心愿', icon: '✦', url: '/pages/wish/publish' },
      { id: 2, name: '接单大厅', icon: '☰', url: '/pages/accept/accept' },
      { id: 3, name: '我的订单', icon: '⚑', url: '/pages/order/list' },
      { id: 4, name: '活动中心', icon: '♫', url: '/pages/activity/list' }
    ],
    latestWishes: [],
    completedCount: 0,
    masterApprenticeCount: 0,
    emptyActions: [
      { text: '发布心愿', url: '/pages/wish/publish', type: 'primary', openType: 'navigate', event: 'publishWish' }
    ]
  },

  onLoad() {
    this.checkLogin();
    this.checkElderMode();
    this.loadLatestWishes();
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 0 });
    }
    if (this.data.isLogin) {
      this.loadUserInfo();
      this.loadUserStats();
    }
  },

  onPullDownRefresh() {
    this.loadLatestWishes();
    if (this.data.isLogin) {
      this.loadUserInfo();
      this.loadUserStats();
    }
    wx.stopPullDownRefresh();
  },

  checkLogin() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (token && userInfo) {
      this.setData({
        isLogin: true,
        userInfo: userInfo,
        userType: userInfo.userType || 1,
        credits: userInfo.creditScore || 0
      });
    }
  },

  checkElderMode() {
    const elderMode = wx.getStorageSync('elderMode');
    this.setData({
      elderMode: elderMode || false
    });
  },

  loadUserInfo() {
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo) {
      this.setData({
        userInfo: userInfo,
        credits: userInfo.creditScore || 0
      });
    }
  },

  loadUserStats() {
    wx.request({
      url: `${app.globalData.apiBaseUrl}/user/stats`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${wx.getStorageSync('token')}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            completedCount: res.data.data.completedCount || 0,
            masterApprenticeCount: res.data.data.masterApprenticeCount || 0
          });
        }
      }
    });
  },

  loadLatestWishes() {
    this.setData({ loading: true });
    
    wx.request({
      url: `${app.globalData.apiBaseUrl}/wish/latest`,
      method: 'GET',
      data: {
        limit: 10
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            latestWishes: res.data.data || [],
            loading: false
          });
        } else {
          this.setData({ loading: false });
        }
      },
      fail: () => {
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

  onQuickAction(e) {
    const url = e.currentTarget.dataset.url;
    if (!this.data.isLogin && url.indexOf('auth') === -1) {
      this.goToLogin();
      return;
    }
    
    if (url.indexOf('wish') > -1 || url.indexOf('order') > -1) {
      wx.switchTab({ url });
    } else {
      wx.navigateTo({ url });
    }
  },

  switchElderMode() {
    const newMode = !this.data.elderMode;
    this.setData({
      elderMode: newMode
    });
    app.setElderMode(newMode);
  },

  viewMoreWishes() {
    wx.switchTab({
      url: '/pages/wish/wish'
    });
  },

  viewWishDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/wish/detail?id=${id}`
    });
  },

  onEmptyAction(e) {
    const action = e.detail;
    if (action.event === 'publishWish') {
      if (!this.data.isLogin) {
        this.goToLogin();
        return;
      }
      wx.navigateTo({ url: '/pages/wish/publish' });
    }
  }
});
