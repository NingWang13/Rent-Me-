const app = getApp();

Page({
  data: {
    isLogin: false,
    activeTab: 0,
    tabs: [
      { name: '全部', status: null, count: 0 },
      { name: '待开始', status: 1, count: 0 },
      { name: '进行中', status: 2, count: 0 },
      { name: '已完成', status: 3, count: 0 },
      { name: '已取消', status: 4, count: 0 }
    ],
    services: [],
    page: 1,
    size: 10,
    hasMore: true,
    loading: false,
    loadingMore: false
  },

  onLoad(options) {
    if (options.type) {
      const type = parseInt(options.type);
      const tabIndex = this.data.tabs.findIndex(t => t.status === type);
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
    this.loadServices(true);
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
      this.loadServices(true);
      this.loadServiceCounts();
    }
  },

  loadServiceCounts() {
    wx.request({
      url: `${app.globalData.apiBaseUrl}/service/counts`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          const counts = res.data.data || {};
          const tabs = [...this.data.tabs];
          tabs[1].count = counts.pending || 0;
          tabs[2].count = counts.inProgress || 0;
          tabs[3].count = counts.completed || 0;
          tabs[4].count = counts.cancelled || 0;
          tabs[0].count = (counts.pending || 0) + (counts.inProgress || 0) + (counts.completed || 0) + (counts.cancelled || 0);
          this.setData({ tabs });
        }
      }
    });
  },

  loadServices(reset = false) {
    if (this.data.loading) return;

    this.setData({
      loading: !reset,
      loadingMore: reset ? false : this.data.loadingMore
    });

    const currentTab = this.data.tabs[this.data.activeTab];
    const page = reset ? 1 : this.data.page;

    wx.request({
      url: `${app.globalData.apiBaseUrl}/service/list`,
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
          const services = list.map(this.formatServiceData);
          this.setData({
            services: reset ? services : [...this.data.services, ...services],
            hasMore: list.length >= this.data.size,
            page: page
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

  formatServiceData(service) {
    const statusMap = {
      1: { text: '待开始', class: 'pending' },
      2: { text: '进行中', class: 'in-progress' },
      3: { text: '已完成', class: 'completed' },
      4: { text: '已取消', class: 'cancelled' }
    };

    const typeMap = {
      1: '学习辅导',
      2: '陪伴照顾',
      3: '代跑腿',
      4: '技术支持',
      5: '其他服务'
    };

    const status = statusMap[service.status] || statusMap[1];
    const serviceTypeName = typeMap[service.serviceType] || '其他服务';

    const steps = [
      { label: '创建', completed: true, current: service.status === 1 },
      { label: '开始', completed: service.status > 1, current: service.status === 2 },
      { label: '进行', completed: service.status > 2, current: service.status === 2 },
      { label: '完成', completed: service.status === 3, current: service.status === 3 }
    ];

    return {
      ...service,
      statusText: status.text,
      serviceTypeName: serviceTypeName,
      progressSteps: steps
    };
  },

  loadMore() {
    this.setData({
      loadingMore: true,
      page: this.data.page + 1
    });
    this.loadServices(false);
  },

  switchTab(e) {
    const index = e.currentTarget.dataset.index;
    if (index === this.data.activeTab) return;

    this.setData({
      activeTab: index,
      services: [],
      page: 1,
      hasMore: true
    });
    this.loadServices(true);
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
      url: `/pages/service/detail/detail?id=${id}`
    });
  },

  reportProgress(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/service/report/report?serviceId=${id}`
    });
  },

  completeService(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确认已完成该服务吗？',
      success: (res) => {
        if (res.confirm) {
          this.requestCompleteService(id);
        }
      }
    });
  },

  requestCompleteService(id) {
    wx.request({
      url: `${app.globalData.apiBaseUrl}/service/${id}/complete`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({
            title: '操作成功',
            icon: 'success'
          });
          this.loadServices(true);
          this.loadServiceCounts();
        } else {
          wx.showToast({
            title: res.data.message || '操作失败',
            icon: 'none'
          });
        }
      }
    });
  }
});