const app = getApp();

Page({
  data: {
    isLogin: false,
    userInfo: null,
    elderMode: false,
    orderCounts: {
      pendingPay: 0,
      paid: 0,
      shipping: 0,
      completed: 0,
      cancelled: 0
    },
    myWishesCount: 0,
    myServicesCount: 0,
    loading: false
  },

  onLoad() {
    this.initPage();
  },

  onShow() {
    this.checkLoginStatus();
    if (this.data.isLogin) {
      this.loadUserInfo();
      this.loadOrderCounts();
      this.loadMyCounts();
    }
  },

  initPage() {
    this.checkLoginStatus();
    this.checkElderMode();
  },

  checkLoginStatus() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    this.setData({
      isLogin: !!(token && userInfo),
      userInfo: userInfo || null
    });
  },

  checkElderMode() {
    const elderMode = wx.getStorageSync('elderMode');
    this.setData({
      elderMode: elderMode || false
    });
  },

  loadUserInfo() {
    if (!this.data.isLogin) return;

    wx.request({
      url: `${app.globalData.apiBaseUrl}/user/info`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          const userInfo = res.data.data;
          this.setData({ userInfo });
          app.setUserInfo(userInfo);
        }
      },
      fail: (err) => {
        console.error('加载用户信息失败', err);
      }
    });
  },

  loadOrderCounts() {
    if (!this.data.isLogin) return;

    wx.request({
      url: `${app.globalData.apiBaseUrl}/api/v1/orders/counts`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          const counts = res.data.data || {};
          this.setData({
            orderCounts: {
              pendingPay: counts.status0 || 0,
              paid: counts.status1 || 0,
              shipping: counts.status2 || 0,
              completed: counts.status3 || 0,
              cancelled: counts.status4 || 0
            }
          });
        }
      },
      fail: (err) => {
        console.error('加载订单数量失败', err);
      }
    });
  },

  loadMyCounts() {
    if (!this.data.isLogin) return;

    wx.request({
      url: `${app.globalData.apiBaseUrl}/user/my-counts`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            myWishesCount: res.data.data.wishesCount || 0,
            myServicesCount: res.data.data.servicesCount || 0
          });
        }
      },
      fail: (err) => {
        console.error('加载我的数量失败', err);
      }
    });
  },

  goToEditProfile() {
    if (!this.data.isLogin) {
      this.goToLogin();
      return;
    }
    wx.navigateTo({
      url: '/pages/user/edit-profile/edit-profile'
    });
  },

  goToCredits() {
    if (!this.data.isLogin) {
      this.goToLogin();
      return;
    }
    wx.navigateTo({
      url: '/pages/user/credits/credits'
    });
  },

  goToFollowers() {
    if (!this.data.isLogin) {
      this.goToLogin();
      return;
    }
    wx.navigateTo({
      url: '/pages/user/followers/followers'
    });
  },

  goToFollowing() {
    if (!this.data.isLogin) {
      this.goToLogin();
      return;
    }
    wx.navigateTo({
      url: '/pages/user/following/following'
    });
  },

  goToLogin() {
    wx.navigateTo({
      url: '/pages/auth/login/login'
    });
  },

  goToOrderList(e) {
    if (!this.data.isLogin) {
      this.goToLogin();
      return;
    }
    const status = e.currentTarget.dataset.status;
    wx.navigateTo({
      url: `/pages/order/list/list?status=${status || ''}`
    });
  },

  goToMyWishes() {
    if (!this.data.isLogin) {
      this.goToLogin();
      return;
    }
    wx.navigateTo({
      url: '/pages/wish/my-wishes/my-wishes'
    });
  },

  goToMyServices() {
    if (!this.data.isLogin) {
      this.goToLogin();
      return;
    }
    wx.navigateTo({
      url: '/pages/service/my-services/my-services'
    });
  },

  goToCreditRecords() {
    if (!this.data.isLogin) {
      this.goToLogin();
      return;
    }
    wx.navigateTo({
      url: '/pages/user/credit-records/credit-records'
    });
  },

  goToPaymentPassword() {
    if (!this.data.isLogin) {
      this.goToLogin();
      return;
    }
    wx.navigateTo({
      url: '/pages/auth/payment-password/payment-password'
    });
  },

  goToIdentityVerify() {
    if (!this.data.isLogin) {
      this.goToLogin();
      return;
    }
    wx.navigateTo({
      url: '/pages/auth/verify/verify'
    });
  },

  goToAccessibility() {
    wx.navigateTo({
      url: '/pages/profile/accessibility/accessibility'
    });
  },

  goToNotificationSettings() {
    wx.navigateTo({
      url: '/pages/settings/notification/notification'
    });
  },

  goToFeedback() {
    wx.navigateTo({
      url: '/pages/profile/feedback/feedback'
    });
  },

  goToAbout() {
    wx.showModal({
      title: '关于我们',
      content: '三代互助社区 V1.0\n致力于构建温馨的互助社区',
      showCancel: false
    });
  },

  switchElderMode() {
    wx.navigateTo({
      url: '/pages/profile/accessibility/accessibility'
    });
  },

  onLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          app.logout();
          this.setData({
            isLogin: false,
            userInfo: null,
            orderCounts: {
              pendingPay: 0,
              paid: 0,
              shipping: 0,
              completed: 0,
              cancelled: 0
            },
            myWishesCount: 0,
            myServicesCount: 0
          });
          wx.showToast({
            title: '已退出登录',
            icon: 'success'
          });
        }
      }
    });
  },

  onPullDownRefresh() {
    if (this.data.isLogin) {
      this.loadUserInfo();
      this.loadOrderCounts();
      this.loadMyCounts();
    }
    wx.stopPullDownRefresh();
  }
});