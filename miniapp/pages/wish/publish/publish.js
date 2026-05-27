const app = getApp();
const { post } = require('../../utils/request');

Page({
  data: {
    title: '',
    content: '',
    category: 1,
    rewardCredit: '10'
  },

  onTitleInput(e) {
    this.setData({
      title: e.detail.value
    });
  },

  onContentInput(e) {
    this.setData({
      content: e.detail.value
    });
  },

  onSelectCategory(e) {
    const type = parseInt(e.currentTarget.dataset.type);
    this.setData({
      category: type
    });
  },

  onRewardInput(e) {
    this.setData({
      rewardCredit: e.detail.value
    });
  },

  async onSubmit() {
    const { title, content, category, rewardCredit } = this.data;

    if (!title.trim()) {
      wx.showToast({ title: '请输入心愿标题', icon: 'none' });
      return;
    }

    if (!content.trim()) {
      wx.showToast({ title: '请输入心愿内容', icon: 'none' });
      return;
    }

    if (!rewardCredit || parseInt(rewardCredit) <= 0) {
      wx.showToast({ title: '请输入有效的奖励积分', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '发布中...' });

    try {
      const result = await post('/wish', {
        title: title.trim(),
        content: content.trim(),
        category: category,
        rewardCredit: parseInt(rewardCredit)
      }, { showLoading: false });

      wx.hideLoading();

      if (result.code === 200) {
        wx.showToast({
          title: '发布成功',
          icon: 'success',
          duration: 1500
        });

        setTimeout(() => {
          wx.switchTab({ url: '/pages/wish/wish' });
        }, 1500);
      } else {
        wx.showToast({
          title: result.message || '发布失败',
          icon: 'none'
        });
      }
    } catch (error) {
      wx.hideLoading();
      console.error('发布心愿失败:', error);
      wx.showToast({
        title: error.message || '发布失败，请重试',
        icon: 'none'
      });
    }
  },

  onLoad() {
    // 检查登录状态
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.redirectTo({ url: '/pages/auth/login/login' });
    }
  }
});

