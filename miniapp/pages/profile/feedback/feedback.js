const app = getApp();

Page({
  data: {
    types: [
      { id: 1, name: '功能建议', icon: 'lightbulb' },
      { id: 2, name: '投诉问题', icon: 'warning' },
      { id: 3, name: 'bug反馈', icon: 'bug' },
      { id: 4, name: '其他', icon: 'info' }
    ],
    selectedType: null,
    content: '',
    images: [],
    rating: 5,
    contact: '',
    submitting: false
  },

  onLoad() {
    const userInfo = app.globalData.userInfo;
    if (userInfo && userInfo.phone) {
      this.setData({ contact: userInfo.phone });
    }
  },

  selectType(e) {
    const typeId = e.currentTarget.dataset.id;
    this.setData({ selectedType: typeId });
  },

  onContentInput(e) {
    this.setData({ content: e.detail.value });
  },

  onContactInput(e) {
    this.setData({ contact: e.detail.value });
  },

  chooseImage() {
    if (this.data.images.length >= 3) {
      wx.showToast({ title: '最多上传3张图片', icon: 'none' });
      return;
    }

    wx.chooseImage({
      count: 3 - this.data.images.length,
      success: (res) => {
        this.setData({
          images: [...this.data.images, ...res.tempFilePaths]
        });
      }
    });
  },

  removeImage(e) {
    const index = e.currentTarget.dataset.index;
    const images = this.data.images.filter((_, i) => i !== index);
    this.setData({ images });
  },

  setRating(e) {
    const rating = e.currentTarget.dataset.rating;
    this.setData({ rating });
  },

  submitFeedback() {
    if (!this.data.selectedType) {
      wx.showToast({ title: '请选择反馈类型', icon: 'none' });
      return;
    }

    if (this.data.content.trim().length < 10) {
      wx.showToast({ title: '反馈内容至少10个字', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });

    wx.request({
      url: `${app.globalData.apiBaseUrl}/feedback/submit`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      data: {
        type: this.data.selectedType,
        content: this.data.content,
        images: this.data.images.join(','),
        rating: this.data.rating,
        contact: this.data.contact
      },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({
            title: '提交成功',
            icon: 'success'
          });
          setTimeout(() => {
            wx.navigateBack();
          }, 1500);
        } else {
          wx.showToast({
            title: res.data.message || '提交失败',
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
        this.setData({ submitting: false });
      }
    });
  }
});
