const app = getApp();

Page({
  data: {
    isLogin: false,
    activeTab: 0,
    tabs: [
      { name: '全部', status: null, count: 0 },
      { name: '待付款', status: 1, count: 0 },
      { name: '进行中', status: 2, count: 0 },
      { name: '待评价', status: 3, count: 0 },
      { name: '已完成', status: 4, count: 0 }
    ],
    orders: [],
    page: 1,
    size: 10,
    hasMore: true,
    loading: false,
    loadingMore: false,
    emptyActions: [
      { text: '去心愿墙看看', url: '/pages/wish/wish', type: 'primary', openType: 'switchTab', event: 'goWish' },
      { text: '发布新心愿', url: '/pages/wish/wish', type: '', openType: 'switchTab', event: 'publishWish' }
    ]
  },

  onLoad(options) {
    if (options.status) {
      const status = parseInt(options.status);
      const tabIndex = this.data.tabs.findIndex(t => t.status === status);
      if (tabIndex !== -1) {
        this.setData({ activeTab: tabIndex });
      }
    }
    this.checkLoginStatus();
  },

  onShow() {
    this.checkLoginStatus();
  },

  onPullDownRefresh() {
    this.loadOrders(true);
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loadingMore) {
      this.loadMore();
    }
  },

  checkLoginStatus() {
    const token = wx.getStorageSync('token');
    this.setData({ isLogin: !!token });
    if (this.data.isLogin) {
      this.loadOrders(true);
      this.loadOrderCounts();
    }
  },

  loadOrderCounts() {
    wx.request({
      url: `${app.globalData.apiBaseUrl}/order/counts`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          const counts = res.data.data;
          const tabs = [...this.data.tabs];
          tabs[1].count = counts.pendingPay || 0;
          tabs[2].count = counts.inProgress || 0;
          tabs[3].count = counts.pendingReview || 0;
          tabs[0].count = (counts.pendingPay || 0) + (counts.inProgress || 0) + (counts.pendingReview || 0);
          this.setData({ tabs });
        }
      }
    });
  },

  loadOrders(reset = false) {
    if (this.data.loading) return;

    this.setData({
      loading: !reset,
      loadingMore: reset ? false : this.data.loadingMore
    });

    const currentTab = this.data.tabs[this.data.activeTab];
    const page = reset ? 1 : this.data.page;

    wx.request({
      url: `${app.globalData.apiBaseUrl}/order/list`,
      method: 'GET',
      data: {
        status: currentTab.status,
        page: page,
        size: this.data.size
      },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          const list = res.data.data.list || [];
          this.setData({
            orders: reset ? list : [...this.data.orders, ...list],
            hasMore: list.length >= this.data.size,
            page: reset ? 1 : this.data.page
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '加载失败',
          icon: 'none'
        });
      },
      complete: () => {
        this.setData({ loading: false, loadingMore: false });
        wx.stopPullDownRefresh();
      }
    });
  },

  loadMore() {
    this.setData({
      loadingMore: true,
      page: this.data.page + 1
    });
    this.loadOrders(false);
  },

  switchTab(e) {
    const index = e.currentTarget.dataset.index;
    if (index === this.data.activeTab) return;

    this.setData({
      activeTab: index,
      orders: [],
      page: 1,
      hasMore: true
    });
    this.loadOrders(true);
  },

  goToLogin() {
    wx.navigateTo({
      url: '/pages/auth/login/login'
    });
  },

  goToWishList() {
    wx.switchTab({
      url: '/pages/wish/wish'
    });
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/order/detail/detail?id=${id}`
    });
  },

  cancelOrder(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确定要取消该订单吗？',
      success: (res) => {
        if (res.confirm) {
          this.requestCancelOrder(id);
        }
      }
    });
  },

  requestCancelOrder(id) {
    wx.request({
      url: `${app.globalData.apiBaseUrl}/order/${id}/cancel`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({
            title: '取消成功',
            icon: 'success'
          });
          this.loadOrders(true);
          this.loadOrderCounts();
        } else {
          wx.showToast({
            title: res.data.message || '取消失败',
            icon: 'none'
          });
        }
      }
    });
  },

  confirmComplete(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确认已完成服务吗？',
      success: (res) => {
        if (res.confirm) {
          this.requestConfirmComplete(id);
        }
      }
    });
  },

  requestConfirmComplete(id) {
    wx.request({
      url: `${app.globalData.apiBaseUrl}/order/${id}/complete`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({
            title: '确认成功',
            icon: 'success'
          });
          this.loadOrders(true);
          this.loadOrderCounts();
        } else {
          wx.showToast({
            title: res.data.message || '操作失败',
            icon: 'none'
          });
        }
      }
    });
  },

  goToReview(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/order/review/review?id=${id}`
    });
  },

  onEmptyAction(e) {
    const action = e.detail;
    if (action.event === 'goWish') {
      wx.switchTab({ url: '/pages/wish/wish' });
    } else if (action.event === 'publishWish') {
      wx.switchTab({ url: '/pages/wish/wish' });
    }
  }
});