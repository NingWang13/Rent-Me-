const app = getApp();

Page({
  data: {
    step: 1,
    password: '',
    confirmPassword: '',
    loading: false,
    countdown: 0
  },

  onLoad() {
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo && userInfo.paymentPasswordSet) {
      wx.showToast({
        title: '您已设置过支付密码',
        icon: 'none'
      });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    }
  },

  onInputPassword(e) {
    this.setData({
      password: e.detail.value
    });
  },

  onInputConfirmPassword(e) {
    this.setData({
      confirmPassword: e.detail.value
    });
  },

  nextStep() {
    if (this.data.password.length !== 6) {
      wx.showToast({
        title: '请输入6位支付密码',
        icon: 'none'
      });
      return;
    }

    if (!/^\d+$/.test(this.data.password)) {
      wx.showToast({
        title: '支付密码必须为纯数字',
        icon: 'none'
      });
      return;
    }

    this.setData({
      step: 2
    });
  },

  confirmPassword() {
    if (this.data.confirmPassword !== this.data.password) {
      wx.showToast({
        title: '两次密码不一致',
        icon: 'none'
      });
      return;
    }

    this.setPaymentPassword();
  },

  setPaymentPassword() {
    this.setData({ loading: true });

    wx.request({
      url: `${app.globalData.apiBaseUrl}/user/${wx.getStorageSync('userInfo').id}/payment-password`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'Content-Type': 'application/json'
      },
      data: {
        password: this.data.password
      },
      success: (res) => {
        if (res.data.code === 200) {
          const userInfo = wx.getStorageSync('userInfo');
          userInfo.paymentPasswordSet = true;
          wx.setStorageSync('userInfo', userInfo);

          wx.showModal({
            title: '设置成功',
            content: '支付密码设置成功',
            showCancel: false,
            success: () => {
              wx.navigateBack();
            }
          });
        } else {
          wx.showToast({
            title: res.data.message || '设置失败',
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
        this.setData({ loading: false });
      }
    });
  },

  goBack() {
    if (this.data.step === 2) {
      this.setData({
        step: 1,
        confirmPassword: ''
      });
    } else {
      wx.navigateBack();
    }
  }
});
