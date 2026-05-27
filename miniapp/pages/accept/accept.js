const app = getApp();

Page({
  data: {
    orders: [],
    categories: [
      { id: 1, name: '全部', selected: true },
      { id: 2, name: '学习类', selected: false },
      { id: 3, name: '陪伴类', selected: false },
      { id: 4, name: '协助类', selected: false },
      { id: 5, name: '圆梦类', selected: false },
      { id: 6, name: '技能类', selected: false }
    ],
    currentCategory: 1,
    loading: false,
    availableCount: 0,
    acceptedCount: 0,
    completedCount: 0
  },

  onLoad() {
    this.loadOrders();
    this.loadStats();
  },

  onPullDownRefresh() {
    this.loadOrders();
    this.loadStats();
  },

  loadStats() {
    wx.request({
      url: `${app.globalData.apiBaseUrl}/order/stats`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          const stats = res.data.data;
          this.setData({
            availableCount: stats.available || 0,
            acceptedCount: stats.accepted || 0,
            completedCount: stats.completed || 0
          });
        }
      }
    });
  },

  loadOrders() {
    this.setData({ loading: true });
    const category = this.data.currentCategory === 1 ? null : this.data.currentCategory;

    wx.request({
      url: `${app.globalData.apiBaseUrl}/order/available`,
      method: 'GET',
      data: {
        category: category,
        page: 1,
        size: 20
      },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            orders: res.data.data.list || []
          });
        }
      },
      complete: () => {
        this.setData({ loading: false });
        wx.stopPullDownRefresh();
      }
    });
  },

  onSelectCategory(e) {
    const categoryId = e.currentTarget.dataset.id;
    const categories = this.data.categories.map(cat => ({
      ...cat,
      selected: cat.id === categoryId
    }));
    this.setData({
      categories,
      currentCategory: categoryId
    });
    this.loadOrders();
  },

  onAcceptOrder(e) {
    const orderId = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认接单',
      content: '确定要接这个订单吗？接单后请及时完成服务。',
      success: (res) => {
        if (res.confirm) {
          this.acceptOrder(orderId);
        }
      }
    });
  },

  acceptOrder(orderId) {
    wx.showLoading({ title: '接单中...' });
    wx.request({
      url: `${app.globalData.apiBaseUrl}/order/${orderId}/accept`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({
            title: '接单成功',
            icon: 'success'
          });
          this.loadOrders();
        } else {
          wx.showToast({
            title: res.data.message || '接单失败',
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        });
      },
      complete: () => {
        wx.hideLoading();
      }
    });
  }
});
