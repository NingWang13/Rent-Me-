const app = getApp();

Page({
  data: {
    isLogin: false,
    activeTab: 0,
    tabs: [
      { name: '全部', status: null, count: 0 },
      { name: '待付款', status: 0, count: 0 },
      { name: '已支付', status: 1, count: 0 },
      { name: '配送中', status: 2, count: 0 },
      { name: '已完成', status: 3, count: 0 },
      { name: '已取消', status: 4, count: 0 }
    ],
    orders: [],
    page: 1,
    size: 10,
    hasMore: true,
    loading: false,
    loadingMore: false,
    total: 0
  },

  onLoad(options) {
    if (options.status !== undefined) {
      const status = options.status === '' ? null : parseInt(options.status);
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
      url: `${app.globalData.apiBaseUrl}/api/v1/orders/counts`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          const counts = res.data.data;
          const tabs = [...this.data.tabs];
          tabs[1].count = counts.status0 || 0;
          tabs[2].count = counts.status1 || 0;
          tabs[3].count = counts.status2 || 0;
          tabs[4].count = counts.status3 || 0;
          tabs[0].count = counts.total || 0;
          this.setData({ tabs });
        }
      }
    });
  },

  loadOrders(reset = false) {
    if (this.data.loading) return;

    this.setData({ loading: !reset });

    const currentTab = this.data.tabs[this.data.activeTab];
    const page = reset ? 1 : this.data.page;

    wx.request({
      url: `${app.globalData.apiBaseUrl}/api/v1/orders`,
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
          const data = res.data.data;
          const list = (data.list || []).map(item => {
            item.statusText = this.getStatusText(item.status);
            item.createTime = this.formatTime(item.createTime);
            return item;
          });
          this.setData({
            orders: reset ? list : [...this.data.orders, ...list],
            total: data.total || 0,
            hasMore: list.length >= this.data.size,
            page: reset ? 1 : this.data.page
          });
        }
      },
      fail: () => {
        wx.showToast({ title: '加载失败', icon: 'none' });
      },
      complete: () => {
        this.setData({ loading: false, loadingMore: false });
        wx.stopPullDownRefresh();
      }
    });
  },

  loadMore() {
    this.setData({ loadingMore: true, page: this.data.page + 1 });
    this.loadOrders(false);
  },

  switchTab(e) {
    const index = e.currentTarget.dataset.index;
    if (index === this.data.activeTab) return;

    this.setData({ activeTab: index, orders: [], page: 1, hasMore: true });
    this.loadOrders(true);
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/order/detail/detail?id=${id}` });
  },

  goToLogin() {
    wx.navigateTo({ url: '/pages/auth/login/login' });
  },

  cancelOrder(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确定要取消该订单吗？',
      success: (res) => {
        if (res.confirm) this.requestCancelOrder(id);
      }
    });
  },

  requestCancelOrder(id) {
    this.reportClientLog('CANCEL_ORDER', { orderId: id });
    wx.request({
      url: `${app.globalData.apiBaseUrl}/api/v1/orders/${id}/cancel`,
      method: 'PUT',
      header: { 'Authorization': `Bearer ${app.globalData.token}` },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({ title: '取消成功', icon: 'success' });
          this.loadOrders(true);
          this.loadOrderCounts();
        } else {
          wx.showToast({ title: res.data.message || '取消失败', icon: 'none' });
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
        if (res.confirm) this.requestConfirmComplete(id);
      }
    });
  },

  requestConfirmComplete(id) {
    this.reportClientLog('CONFIRM_COMPLETE', { orderId: id });
    wx.request({
      url: `${app.globalData.apiBaseUrl}/api/v1/orders/${id}/complete`,
      method: 'PUT',
      header: { 'Authorization': `Bearer ${app.globalData.token}` },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({ title: '确认成功', icon: 'success' });
          this.loadOrders(true);
          this.loadOrderCounts();
        } else {
          wx.showToast({ title: res.data.message || '操作失败', icon: 'none' });
        }
      }
    });
  },

  reportClientLog(action, detail) {
    const logData = {
      action,
      detail,
      timestamp: Date.now(),
      page: 'order/list',
      userId: app.globalData.userId
    };
    try {
      wx.request({
        url: `${app.globalData.apiBaseUrl}/api/v1/logs/client`,
        method: 'POST',
        data: logData,
        header: { 'Authorization': `Bearer ${app.globalData.token}` },
        fail: () => {}
      });
    } catch (e) {}
  },

  getStatusText(status) {
    const map = { 0: '待付款', 1: '已支付', 2: '配送中', 3: '已完成', 4: '已取消' };
    return map[status] || '未知';
  },

  formatTime(timeStr) {
    if (!timeStr) return '';
    return timeStr.replace('T', ' ').substring(0, 16);
  }
});
