const app = getApp();

Page({
  data: {
    wishes: [],
    categories: [
      { id: 0, name: '全部', selected: true },
      { id: 1, name: '学习类', selected: false },
      { id: 2, name: '陪伴类', selected: false },
      { id: 3, name: '协助类', selected: false },
      { id: 4, name: '圆梦类', selected: false },
      { id: 5, name: '技能类', selected: false }
    ],
    currentCategory: 0,
    searchKeyword: '',
    page: 1,
    pageSize: 10,
    noMore: false,
    loading: false,
    elderMode: false,
    showPublishForm: false,
    publishForm: {
      title: '',
      content: '',
      category: 1,
      rewardCredit: '10'
    },
    showCreditModal: false,
    creditReward: 10,
    emptyActions: [
      { text: '发布我的心愿', type: 'primary', event: 'publishWish' }
    ],
    currentUserId: null
  },

  onLoad() {
    this.checkElderMode();
    this.loadWishes(true);
    this.setCurrentUser();
  },

  onShow() {
    this.checkElderMode();
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 1 });
    }
  },

  onPullDownRefresh() {
    this.loadWishes(true);
  },

  onReachBottom() {
    if (!this.data.noMore && !this.data.loading) {
      this.loadWishes(false);
    }
  },

  setCurrentUser() {
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo) {
      this.setData({
        currentUserId: userInfo.id
      });
    }
  },

  checkElderMode() {
    const elderMode = wx.getStorageSync('elderMode');
    this.setData({
      elderMode: elderMode || false
    });
  },

  loadWishes(refresh) {
    if (this.data.loading) return;

    const page = refresh ? 1 : this.data.page;
    this.setData({ loading: true });

    const params = {
      page: page,
      size: this.data.pageSize,
      category: this.data.currentCategory || undefined,
      keyword: this.data.searchKeyword || undefined
    };

    wx.request({
      url: `${app.globalData.apiBaseUrl}/wish/list`,
      method: 'GET',
      data: params,
      header: {
        'Authorization': `Bearer ${wx.getStorageSync('token') || ''}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          const newWishes = res.data.data.list || [];
          const wishes = refresh ? newWishes : [...this.data.wishes, ...newWishes];

          this.setData({
            wishes: wishes,
            page: page + 1,
            noMore: newWishes.length < this.data.pageSize
          });
        }
      },
      complete: () => {
        this.setData({ loading: false });
        wx.stopPullDownRefresh();
      }
    });
  },

  onSearchInput(e) {
    this.setData({
      searchKeyword: e.detail.value
    });
  },

  onSearch() {
    this.loadWishes(true);
  },

  clearSearch() {
    this.setData({
      searchKeyword: ''
    });
    this.loadWishes(true);
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
    this.loadWishes(true);
  },

  onClaimWish(e) {
    const wishId = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认认领',
      content: '确定要认领这个心愿吗？',
      success: (res) => {
        if (res.confirm) {
          this.claimWish(wishId);
        }
      }
    });
  },

  claimWish(wishId) {
    wx.request({
      url: `${app.globalData.apiBaseUrl}/wish/${wishId}/claim`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${wx.getStorageSync('token')}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({
            title: '认领成功',
            icon: 'success'
          });
          this.loadWishes(true);
        } else {
          wx.showToast({
            title: res.data.message || '认领失败',
            icon: 'none'
          });
        }
      }
    });
  },

  togglePublishForm() {
    if (!wx.getStorageSync('token')) {
      wx.navigateTo({ url: '/pages/auth/login' });
      return;
    }
    this.setData({
      showPublishForm: !this.data.showPublishForm
    });
  },

  onTitleInput(e) {
    this.setData({
      'publishForm.title': e.detail.value
    });
  },

  onContentInput(e) {
    this.setData({
      'publishForm.content': e.detail.value
    });
  },

  onSelectPublishCategory(e) {
    this.setData({
      'publishForm.category': parseInt(e.currentTarget.dataset.type)
    });
  },

  onSubmitPublish() {
    const { title, content, category, rewardCredit } = this.data.publishForm;

    if (!title.trim()) {
      wx.showToast({ title: '请输入心愿标题', icon: 'none' });
      return;
    }

    if (!content.trim()) {
      wx.showToast({ title: '请输入心愿内容', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '发布中...' });

    wx.request({
      url: `${app.globalData.apiBaseUrl}/wish`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${wx.getStorageSync('token')}`,
        'Content-Type': 'application/json'
      },
      data: {
        title: title.trim(),
        content: content.trim(),
        category: category,
        rewardCredit: parseInt(rewardCredit)
      },
      success: (res) => {
        wx.hideLoading();
        if (res.data.code === 200) {
          this.grantPublishCredit();
          this.resetPublishForm();
          this.loadWishes(true);
        } else {
          wx.showToast({ title: res.data.message || '发布失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: '网络异常，请重试', icon: 'none' });
      }
    });
  },

  grantPublishCredit() {
    const rewardCredit = 10;
    wx.request({
      url: `${app.globalData.apiBaseUrl}/credit/add`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${wx.getStorageSync('token')}`,
        'Content-Type': 'application/json'
      },
      data: {
        amount: rewardCredit,
        type: 'wish_publish',
        description: '发布心愿奖励'
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            creditReward: rewardCredit,
            showCreditModal: true
          });
          this.updateLocalCredits(rewardCredit);
        }
      }
    });
  },

  updateLocalCredits(amount) {
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo) {
      userInfo.creditScore = (userInfo.creditScore || 0) + amount;
      wx.setStorageSync('userInfo', userInfo);
    }
  },

  resetPublishForm() {
    this.setData({
      'publishForm.title': '',
      'publishForm.content': '',
      'publishForm.category': 1,
      'publishForm.rewardCredit': '10',
      showPublishForm: false
    });
  },

  closeCreditModal() {
    this.setData({
      showCreditModal: false
    });
  },

  preventMove() {},

  onEmptyAction(e) {
    const action = e.detail;
    if (action.event === 'publishWish') {
      this.togglePublishForm();
    }
  },

  viewWishDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/wish/detail?id=${id}`
    });
  }
});
