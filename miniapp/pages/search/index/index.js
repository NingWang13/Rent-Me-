const app = getApp();

Page({
  data: {
    keyword: '',
    activeCategory: null,
    categories: [
      { id: null, name: '全部' },
      { id: 1, name: '代购服务' },
      { id: 2, name: '上门服务' },
      { id: 3, name: '技能交换' },
      { id: 4, name: '跑腿帮忙' },
      { id: 5, name: '陪伴照顾' },
      { id: 6, name: '其他互助' }
    ],
    orders: [],
    page: 1,
    size: 10,
    hasMore: true,
    loading: false,
    searchType: 'orders'
  },

  onLoad(options) {
    if (options.keyword) {
      this.setData({ keyword: options.keyword });
      this.search();
    }
    this.loadHotTags();
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadMore();
    }
  },

  onSearch(e) {
    const keyword = e.detail.value || this.data.keyword;
    this.setData({ keyword, page: 1, orders: [], hasMore: true });
    this.search();
  },

  onInput(e) {
    this.setData({ keyword: e.detail.value });
  },

  clearKeyword() {
    this.setData({ keyword: '', orders: [], page: 1, hasMore: true });
  },

  selectCategory(e) {
    const categoryId = e.currentTarget.dataset.id;
    this.setData({ activeCategory: categoryId, page: 1, orders: [], hasMore: true });
    this.search();
  },

  search() {
    this.setData({ loading: true });

    wx.request({
      url: `${app.globalData.apiBaseUrl}/search/orders`,
      method: 'GET',
      data: {
        keyword: this.data.keyword,
        category: this.data.activeCategory,
        page: this.data.page,
        size: this.data.size
      },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            orders: res.data.data.list,
            hasMore: res.data.data.page < res.data.data.totalPages
          });
        }
      },
      complete: () => {
        this.setData({ loading: false });
      }
    });
  },

  loadMore() {
    if (this.data.loading || !this.data.hasMore) return;

    this.setData({ loading: true, page: this.data.page + 1 });

    wx.request({
      url: `${app.globalData.apiBaseUrl}/search/orders`,
      method: 'GET',
      data: {
        keyword: this.data.keyword,
        category: this.data.activeCategory,
        page: this.data.page,
        size: this.data.size
      },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          const newOrders = res.data.data.list;
          this.setData({
            orders: [...this.data.orders, ...newOrders],
            hasMore: res.data.data.page < res.data.data.totalPages
          });
        }
      },
      complete: () => {
        this.setData({ loading: false });
      }
    });
  },

  loadHotTags() {
    wx.request({
      url: `${app.globalData.apiBaseUrl}/search/hot-tags`,
      method: 'GET',
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({ hotTags: res.data.data });
        }
      }
    });
  },

  onTagTap(e) {
    const tag = e.currentTarget.dataset.tag;
    this.setData({ keyword: tag, page: 1, orders: [], hasMore: true });
    this.search();
  }
});
